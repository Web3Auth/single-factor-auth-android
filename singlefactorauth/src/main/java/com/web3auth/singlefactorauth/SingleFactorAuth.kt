package com.web3auth.singlefactorauth

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.GsonBuilder
import com.web3auth.session_manager_android.SessionManager
import com.web3auth.singlefactorauth.types.AggregateVerifierParams
import com.web3auth.singlefactorauth.types.ErrorCode
import com.web3auth.singlefactorauth.types.LoginParams
import com.web3auth.singlefactorauth.types.SFAError
import com.web3auth.singlefactorauth.types.SFAKey
import com.web3auth.singlefactorauth.types.SFAParams
import com.web3auth.singlefactorauth.types.TorusSubVerifierInfo
import org.json.JSONObject
import org.torusresearch.fetchnodedetails.FetchNodeDetails
import org.torusresearch.fetchnodedetails.types.NodeDetails
import org.torusresearch.torusutils.TorusUtils
import org.torusresearch.torusutils.helpers.MetadataUtils
import org.torusresearch.torusutils.types.VerifierParams
import org.torusresearch.torusutils.types.VerifyParams
import org.torusresearch.torusutils.types.common.TorusKey
import org.torusresearch.torusutils.types.common.TorusOptions
import org.torusresearch.torusutils.types.common.TorusPublicKey
import org.torusresearch.torusutils.types.common.TypeOfUser
import org.web3j.crypto.Hash
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class SingleFactorAuth(sfaParams: SFAParams) {
    private var nodeDetailManager: FetchNodeDetails =
        FetchNodeDetails(sfaParams.getNetwork())
    private val torusUtils: TorusUtils
    private lateinit var sessionManager: SessionManager
    private val gson = GsonBuilder().disableHtmlEscaping().create()
    private var _sfaParams = sfaParams

    init {
        val opts = TorusOptions(
            sfaParams.clientid, sfaParams.getNetwork(),
            null, 0, true
        )
        torusUtils = TorusUtils(opts)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(ExecutionException::class, InterruptedException::class)
    fun getKey(
        loginParams: LoginParams,
        context: Context? = null,
        sessionTime: Long = 86400
    ): CompletableFuture<SFAKey> {
        val torusKeyCompletableFuture: CompletableFuture<SFAKey> =
            CompletableFuture<SFAKey>()

        val nodeDetails: NodeDetails =
            nodeDetailManager.getNodeDetails(loginParams.verifier, loginParams.verifierId)
                .get()
        val pubDetails: TorusPublicKey = torusUtils.getUserTypeAndAddress(
            getTorusNodeEndpoints(nodeDetails), loginParams.verifier,
            loginParams.verifierId, null
        )
        if (pubDetails.metadata.isUpgraded) {
            val response: CompletableFuture<SFAKey> = CompletableFuture<SFAKey>()
            response.completeExceptionally(Exception(SFAError.getError(ErrorCode.USER_ALREADY_ENABLED_MFA)))
            return response
        }
        val retrieveSharesResponse = getRetrieveSharesResponse(loginParams, nodeDetails, pubDetails)
        if (retrieveSharesResponse.finalKeyData.privKey == null) {
            torusKeyCompletableFuture.completeExceptionally(
                Exception(
                    SFAError.getError(
                        ErrorCode.PRIVATE_KEY_NOT_FOUND
                    )
                )
            )
        }
        torusKeyCompletableFuture.complete(
            SFAKey(
                retrieveSharesResponse.finalKeyData.privKey,
                retrieveSharesResponse.finalKeyData.walletAddress
            )
        )

        if (context != null) {
            sessionManager = SessionManager(context)
            val json = JSONObject()
            json.put("privateKey", retrieveSharesResponse.finalKeyData.privKey)
            json.put("publicAddress", retrieveSharesResponse.finalKeyData.walletAddress)
            sessionManager.createSession(
                json.toString(), sessionTime, true
            )
        }

        return torusKeyCompletableFuture
    }

    fun initialize(context: Context? = null): CompletableFuture<SFAKey> {
        val torusKeyCompletableFuture: CompletableFuture<SFAKey> =
            CompletableFuture<SFAKey>()
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
                        gson.fromJson(tempJson.toString(), SFAKey::class.java)
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
        nodeDetails: NodeDetails,
        pubDetails: TorusPublicKey
    ): TorusKey {
        if (pubDetails.metadata.typeOfUser.equals(TypeOfUser.v1)) {
            MetadataUtils.getOrSetNonce(
                "", pubDetails.metadata.pubNonce.x, pubDetails.metadata.pubNonce.y,
                0, null, false, null
            )
        }
        val retrieveSharesResponse: TorusKey
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
                    VerifyParams(
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
            val verifierParams = VerifierParams(
                aggregateVerifierParams.verifier_id, null,
                aggregateVerifierParams.sub_verifier_ids, aggregateVerifierParams.verify_params
            )
            val nodeDetails =
                nodeDetailManager.getNodeDetails(loginParams.verifier, aggregateVerifierId)
                    .get()
            retrieveSharesResponse = torusUtils.retrieveShares(
                getTorusNodeEndpoints(nodeDetails),
                loginParams.verifier,
                verifierParams,
                aggregateIdToken,
                null
            )
        } else {
            val verifierParams = VerifierParams(loginParams.verifierId, null, null, null)
            retrieveSharesResponse = torusUtils.retrieveShares(
                getTorusNodeEndpoints(nodeDetails),
                loginParams.verifier,
                verifierParams,
                loginParams.idToken,
                null
            )
        }
        return retrieveSharesResponse
    }

    private fun getTorusNodeEndpoints(nodeDetails: NodeDetails): Array<String?> {
        return if (_sfaParams.getNetwork().toString().contains("sapphire")) {
            nodeDetails.torusNodeSSSEndpoints
        } else {
            nodeDetails.torusNodeEndpoints
        }
    }
}