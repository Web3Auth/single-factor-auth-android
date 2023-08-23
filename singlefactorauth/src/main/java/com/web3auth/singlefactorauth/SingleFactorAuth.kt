package com.web3auth.singlefactorauth

import android.content.Context
import com.google.gson.GsonBuilder
import com.web3auth.session_manager_android.SessionManager
import com.web3auth.singlefactorauth.types.*
import org.json.JSONObject
import org.torusresearch.fetchnodedetails.FetchNodeDetails
import org.torusresearch.fetchnodedetails.types.NodeDetails
import org.torusresearch.torusutils.TorusUtils
import org.torusresearch.torusutils.helpers.Utils
import org.torusresearch.torusutils.types.*
import org.web3j.crypto.Hash
import java.math.BigInteger
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class SingleFactorAuth(singleFactorAuthArgs: SingleFactorAuthArgs) {
    private var nodeDetailManager: FetchNodeDetails
    private val torusUtils: TorusUtils
    private lateinit var sessionManager: SessionManager
    private val gson = GsonBuilder().disableHtmlEscaping().create()
    private var _singleFactorAuthArgs = singleFactorAuthArgs

    init {
        nodeDetailManager = if (Utils.isEmpty(singleFactorAuthArgs.networkUrl)) {
            FetchNodeDetails(
                singleFactorAuthArgs.getNetwork(),
                SingleFactorAuthArgs.CONTRACT_MAP[singleFactorAuthArgs.getNetwork()]
            )
        } else {
            FetchNodeDetails(
                singleFactorAuthArgs.networkUrl,
                SingleFactorAuthArgs.CONTRACT_MAP[singleFactorAuthArgs.getNetwork()]
            )
        }
        val opts = TorusCtorOptions("single-factor-auth-android")
        opts.isEnableOneKey = true
        opts.network = singleFactorAuthArgs.getNetwork().toString()
        opts.signerHost =
            SingleFactorAuthArgs.SIGNER_MAP[singleFactorAuthArgs.getNetwork()] + "/api/sign"
        opts.allowHost =
            SingleFactorAuthArgs.SIGNER_MAP[singleFactorAuthArgs.getNetwork()] + "/api/allow"
        //clientId is mandatory for SAPPHIRE networks.
        opts.clientId = singleFactorAuthArgs.getClientId()
        torusUtils = TorusUtils(opts)
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    fun getKey(
        loginParams: LoginParams,
        context: Context? = null,
        sessionTime: Long = 86400
    ): CompletableFuture<TorusKey> {
        val torusKeyCompletableFuture: CompletableFuture<TorusKey> =
            CompletableFuture<TorusKey>()

        val details: NodeDetails =
            nodeDetailManager.getNodeDetails(loginParams.verifier, loginParams.verifierId)
                .get()
        val pubDetails: TorusPublicKey = torusUtils.getUserTypeAndAddress(
            details.torusNodeEndpoints,
            details.torusNodePub,
            VerifierArgs(loginParams.verifier, loginParams.verifierId),
        ).get()
        if (pubDetails.getMetadata().isUpgraded) {
            val response: CompletableFuture<TorusKey> = CompletableFuture<TorusKey>()
            response.completeExceptionally(Exception(SFAError.getError(ErrorCode.USER_ALREADY_ENABLED_MFA)))
            return response
        }
        val retrieveSharesResponse = getRetrieveSharesResponse(loginParams, details, pubDetails)
        if (retrieveSharesResponse.getFinalKeyData().getPrivKey() == null) {
            torusKeyCompletableFuture.completeExceptionally(
                Exception(
                    SFAError.getError(
                        ErrorCode.PRIVATE_KEY_NOT_FOUND
                    )
                )
            )
        }
        torusKeyCompletableFuture.complete(
            TorusKey(
                BigInteger(retrieveSharesResponse.getFinalKeyData().getPrivKey(), 16),
                retrieveSharesResponse.getFinalKeyData().getEvmAddress(),
                retrieveSharesResponse,
                retrieveSharesResponse.getFinalKeyData(),
                retrieveSharesResponse.getFinalKeyData(),
                retrieveSharesResponse.getMetadata(),
                null
            )
        )

        if (context != null) {
            sessionManager = SessionManager(context)
            val json = JSONObject()
            json.put(
                "privateKey",
                BigInteger(retrieveSharesResponse.getFinalKeyData().getPrivKey(), 16).toString()
            )
            json.put("publicAddress", retrieveSharesResponse.getFinalKeyData().getEvmAddress())
            json.put("retrieveSharesResponse", retrieveSharesResponse)
            sessionManager.createSession(
                json.toString(), sessionTime
            )
        }

        return torusKeyCompletableFuture
    }

    fun initialize(context: Context? = null): CompletableFuture<TorusKey> {
        val torusKeyCompletableFuture: CompletableFuture<TorusKey> =
            CompletableFuture<TorusKey>()
        if(context == null) {
            torusKeyCompletableFuture.completeExceptionally(Exception(SFAError.getError(ErrorCode.CONTEXT_NOT_FOUND)))
        } else {
            sessionManager = SessionManager(context)
            val sessionResponse: CompletableFuture<String> =
                sessionManager.authorizeSession(false)
            sessionResponse.whenComplete { response, error ->
                if (error == null) {
                    val tempJson = JSONObject(response)
                    val torusKey =
                        gson.fromJson(tempJson.toString(), TorusKey::class.java)
                    torusKeyCompletableFuture.complete(torusKey)
                } else {
                    torusKeyCompletableFuture.complete(null)
                }
            }
        }
        return torusKeyCompletableFuture
    }

    private fun getRetrieveSharesResponse(
        loginParams: LoginParams,
        details: NodeDetails,
        pubDetails: TorusPublicKey
    ): RetrieveSharesResponse {
        if (pubDetails.getMetadata().typeOfUser.equals(TypeOfUser.v1)) {
            torusUtils.getOrSetNonce(
                pubDetails.getMetadata().getPubNonce().x,
                pubDetails.getMetadata().getPubNonce().y,
                false
            ).get()
        }
        val retrieveSharesResponse: RetrieveSharesResponse
        if (loginParams.subVerifierInfoArray != null && loginParams.subVerifierInfoArray?.isNotEmpty() == true) {
            val subVerifierInfoArray: Array<TorusSubVerifierInfo> =
                loginParams.subVerifierInfoArray!!
            val aggregateVerifierParams = AggregateVerifierParams()
            aggregateVerifierParams.verify_params = (
                    arrayOfNulls(
                        subVerifierInfoArray.size
                    )
                    )
            aggregateVerifierParams.sub_verifier_ids = arrayOfNulls(subVerifierInfoArray.size)
            val aggregateIdTokenSeeds: MutableList<String> = ArrayList()
            var aggregateVerifierId = ""
            for (i in subVerifierInfoArray.indices) {
                val userInfo: TorusSubVerifierInfo = subVerifierInfoArray[i]
                val finalToken: String = userInfo.idToken
                aggregateVerifierParams.setVerifyParamItem(
                    AggregateVerifierParams.VerifierParams(
                        loginParams.verifierId,
                        finalToken
                    ), i
                )
                aggregateVerifierParams.setSubVerifierIdItem(userInfo.verifier, i)
                aggregateIdTokenSeeds.add(finalToken)
                aggregateVerifierId = loginParams.verifierId
            }
            aggregateIdTokenSeeds.sort()
            val aggregateTokenString =
                java.lang.String.join(29.toChar().toString(), aggregateIdTokenSeeds)
            val aggregateIdToken: String = Hash.sha3String(aggregateTokenString).substring(2)
            aggregateVerifierParams.verifier_id = aggregateVerifierId
            val aggregateVerifierParamsHashMap = HashMap<String, Any>()
            aggregateVerifierParamsHashMap["verify_params"] =
                aggregateVerifierParams.verify_params
            aggregateVerifierParamsHashMap["sub_verifier_ids"] =
                aggregateVerifierParams.sub_verifier_ids
            aggregateVerifierParamsHashMap["verifier_id"] = aggregateVerifierParams.verifier_id
            val nodeDetails =
                nodeDetailManager.getNodeDetails(loginParams.verifier, aggregateVerifierId)
                    .get()
            retrieveSharesResponse = torusUtils.retrieveShares(
                getTorusNodeEndpoints(nodeDetails),
                nodeDetails.torusIndexes,
                loginParams.verifier,
                aggregateVerifierParamsHashMap,
                aggregateIdToken
            ).get()
        } else {
            val verifierParams = HashMap<String, Any>()
            verifierParams["verifier_id"] = loginParams.verifierId
            retrieveSharesResponse = torusUtils.retrieveShares(
                getTorusNodeEndpoints(details),
                details.torusIndexes,
                loginParams.verifier,
                verifierParams,
                loginParams.idToken
            ).get()
        }
        return retrieveSharesResponse
    }

    private fun getTorusNodeEndpoints(nodeDetails: NodeDetails): Array<String?>? {
        return if (_singleFactorAuthArgs.getNetwork().toString().contains("sapphire")) {
            nodeDetails.torusNodeSSSEndpoints
        } else {
            nodeDetails.torusNodeEndpoints
        }
    }
}