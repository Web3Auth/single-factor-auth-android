package com.web3auth.singlefactorauth

import android.content.Context
import com.google.gson.GsonBuilder
import com.web3auth.session_manager_android.SessionManager
import com.web3auth.singlefactorauth.types.ErrorCode
import com.web3auth.singlefactorauth.types.LoginParams
import com.web3auth.singlefactorauth.types.SFAError
import com.web3auth.singlefactorauth.types.SingleFactorAuthArgs
import com.web3auth.singlefactorauth.types.TorusSFAKey
import com.web3auth.singlefactorauth.types.TorusSubVerifierInfo
import org.json.JSONObject
import org.torusresearch.fetchnodedetails.FetchNodeDetails
import org.torusresearch.fetchnodedetails.types.NodeDetails
import org.torusresearch.fetchnodedetails.types.Web3AuthNetwork
import org.torusresearch.torusutils.TorusUtils
import org.torusresearch.torusutils.types.VerifierParams
import org.torusresearch.torusutils.types.VerifyParams
import org.torusresearch.torusutils.types.common.TorusKey
import org.torusresearch.torusutils.types.common.TorusOptions
import org.torusresearch.torusutils.types.common.TorusPublicKey
import org.web3j.crypto.Hash
import java.util.concurrent.CompletableFuture

class SingleFactorAuth(sfaParams: SingleFactorAuthArgs, ctx: Context) {
    private var nodeDetailManager: FetchNodeDetails =
        FetchNodeDetails(sfaParams.getNetwork())
    private val torusUtils: TorusUtils
    private var sessionManager: SessionManager
    private val gson = GsonBuilder().disableHtmlEscaping().create()
    private var network: Web3AuthNetwork

    init {
        val torusOptions = sfaParams.getClientId()?.let {
            TorusOptions(
                it, sfaParams.getNetwork(),
                sfaParams.getNetworkUrl(), sfaParams.getServerTimeOffset(), true
            )
        }
        network = sfaParams.getNetwork()
        torusUtils = TorusUtils(torusOptions)
        sessionManager = SessionManager(ctx)
    }

    fun initialize(): CompletableFuture<TorusSFAKey> {
        val sfaKeyCompletableFuture: CompletableFuture<TorusSFAKey> =
            CompletableFuture<TorusSFAKey>()
        val sessionResponse = sessionManager.authorizeSession(false)
        sessionResponse.whenComplete { response, error ->
            if (error == null) {
                val tempJson = JSONObject(response)
                val torusKey =
                    gson.fromJson(tempJson.toString(), TorusSFAKey::class.java)
                sfaKeyCompletableFuture.complete(torusKey)
            } else {
                sfaKeyCompletableFuture.complete(null)
            }
        }
        return sfaKeyCompletableFuture
    }

    private fun getTorusNodeEndpoints(nodeDetails: NodeDetails): Array<String?> {
        return if (this.network.toString().contains("sapphire")) {
            nodeDetails.torusNodeSSSEndpoints
        } else {
            nodeDetails.torusNodeEndpoints
        }
    }

    fun getTorusKey(
        loginParams: LoginParams
    ): TorusKey? {
        val nodeDetails: NodeDetails =
            nodeDetailManager.getNodeDetails(loginParams.verifier, loginParams.verifierId)
                .get()

        val pubDetails: TorusPublicKey = torusUtils.getUserTypeAndAddress(
            getTorusNodeEndpoints(nodeDetails), loginParams.verifier,
            loginParams.verifierId, null
        )

        if (pubDetails.metadata.isUpgraded) {
            throw Exception(SFAError.getError(ErrorCode.USER_ALREADY_ENABLED_MFA))
        }

        loginParams.subVerifierInfoArray?.let { it ->
            val aggregateIdTokenSeeds: ArrayList<String> = ArrayList()
            val subVerifierIds: ArrayList<String> = ArrayList()
            val verifyParams: ArrayList<VerifyParams> = ArrayList()

            for(value: TorusSubVerifierInfo in it) {
                aggregateIdTokenSeeds.add(value.idToken)
                val verifyParam = VerifyParams(loginParams.verifierId, value.idToken)
                verifyParams.add(verifyParam)
                subVerifierIds.add(value.verifier)
            }

            aggregateIdTokenSeeds.sort()
            val verifierParams = VerifierParams(loginParams.verifierId, null,
                subVerifierIds.toTypedArray(), verifyParams.toTypedArray()
            )

            val aggregateIdToken = Hash.sha3String(java.lang.String.join(29.toChar().toString(), aggregateIdTokenSeeds)).replace("0x", "")
            return torusUtils.retrieveShares(getTorusNodeEndpoints(nodeDetails),loginParams.verifier, verifierParams, aggregateIdToken, null)
        } ?: run{
            val verifierParams = VerifierParams(loginParams.verifierId, null, null, null)
            return torusUtils.retrieveShares(getTorusNodeEndpoints(nodeDetails),loginParams.verifier, verifierParams, loginParams.idToken, null)
        }
    }

    fun getKey(
        loginParams: LoginParams,
    ): TorusSFAKey? {
        val torusKey = getTorusKey(loginParams)

        val torusSFAKey = torusKey?.finalKeyData?.let { it ->
            it.walletAddress?.let { it1 ->
                it.privKey?.let {it2 ->
                    TorusSFAKey(
                        it2,
                        it1
                    )
                }
            }
        }

        val json = JSONObject()
        if (torusSFAKey != null) {
            json.put("privateKey", torusSFAKey.getPrivateKey())
            json.put("publicAddress", torusSFAKey.getPublicAddress())
        }

        sessionManager.createSession(json.toString(),86400,true).get()
        return torusSFAKey
    }
}