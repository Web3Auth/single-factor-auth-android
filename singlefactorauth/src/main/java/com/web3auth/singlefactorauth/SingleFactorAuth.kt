package com.web3auth.singlefactorauth

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.auth0.android.jwt.JWT
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.web3auth.session_manager_android.SessionManager
import com.web3auth.singlefactorauth.types.ErrorCode
import com.web3auth.singlefactorauth.types.LoginParams
import com.web3auth.singlefactorauth.types.LoginType
import com.web3auth.singlefactorauth.types.SFAError
import com.web3auth.singlefactorauth.types.SessionData
import com.web3auth.singlefactorauth.types.TorusGenericContainer
import com.web3auth.singlefactorauth.types.TorusSubVerifierInfo
import com.web3auth.singlefactorauth.types.UserInfo
import com.web3auth.singlefactorauth.types.Web3AuthOptions
import org.json.JSONObject
import org.torusresearch.fetchnodedetails.FetchNodeDetails
import org.torusresearch.fetchnodedetails.types.NodeDetails
import org.torusresearch.fetchnodedetails.types.Web3AuthNetwork
import org.torusresearch.torusutils.TorusUtils
import org.torusresearch.torusutils.types.VerifierParams
import org.torusresearch.torusutils.types.VerifyParams
import org.torusresearch.torusutils.types.common.TorusKey
import org.torusresearch.torusutils.types.common.TorusOptions
import org.web3j.crypto.Hash
import java.util.concurrent.CompletableFuture

class SingleFactorAuth(
    web3AuthOptions: Web3AuthOptions,
    ctx: Context,
) {
    private var nodeDetailManager: FetchNodeDetails =
        FetchNodeDetails(web3AuthOptions.getNetwork())
    private val torusUtils: TorusUtils
    private var sessionManager: SessionManager
    private val gson = GsonBuilder().disableHtmlEscaping().create()
    private var network: Web3AuthNetwork
    private var state: SessionData? = null

    init {
        val torusOptions = TorusOptions(
            web3AuthOptions.getClientId(), web3AuthOptions.getNetwork(), null,
            web3AuthOptions.getServerTimeOffset(), true
        )
        network = web3AuthOptions.getNetwork()
        torusUtils = TorusUtils(torusOptions)
        sessionManager = SessionManager(ctx, web3AuthOptions.getSessionTime(), ctx.packageName)
    }

    fun initialize(ctx: Context): CompletableFuture<SessionData?> {
        return CompletableFuture.supplyAsync {
            val savedSessionId = SessionManager.getSessionIdFromStorage()
            sessionManager.setSessionId(savedSessionId)

            if (savedSessionId.isEmpty()) {
                throw IllegalStateException("No session ID found in storage.")
            }

            val dataFuture = sessionManager.authorizeSession(ctx.packageName, ctx)
            dataFuture.whenComplete { response, error ->
                if (error != null) {
                    throw Exception("Failed to authorize session", error)
                }
            }

            // Extract data and parse
            val data = JSONObject(dataFuture.get())
            val privateKey = data.getString("privateKey")
            val publicAddress = data.getString("publicAddress")
            val userInfoJson = data.getString("userInfo")
            val signaturesJson = data.getString("signatures")

            val finalUserInfo = Gson().fromJson(userInfoJson, UserInfo::class.java)
            val finalSignatures = Gson().fromJson(
                signaturesJson,
                org.torusresearch.torusutils.types.SessionData::class.java
            )

            // Set state and return
            state = SessionData(
                privateKey = privateKey,
                publicAddress = publicAddress,
                signatures = finalSignatures,
                userInfo = finalUserInfo
            )
            state
        }.thenApplyAsync({ sessionData ->
            sessionData
        }, { Handler(Looper.getMainLooper()).post(it) }).exceptionally { ex ->
            throw Exception("Initialization failed", ex)
        }
    }

    fun getSessionData(): SessionData? {
        return this.state
    }

    fun isConnected(): Boolean {
        return this.state != null
    }

    fun getTorusKey(
        loginParams: LoginParams
    ): TorusKey {
        lateinit var retrieveSharesResponse: TorusKey

        val nodeDetails: NodeDetails =
            nodeDetailManager.getNodeDetails(loginParams.verifier, loginParams.verifierId)
                .get()

        loginParams.subVerifierInfoArray?.let {
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
            retrieveSharesResponse = torusUtils.retrieveShares(
                nodeDetails.torusNodeEndpoints,
                loginParams.verifier,
                verifierParams,
                aggregateIdToken,
                null
            )
        } ?: run{
            val verifierParams = VerifierParams(loginParams.verifierId, null, null, null)
            retrieveSharesResponse = torusUtils.retrieveShares(
                nodeDetails.torusNodeEndpoints,
                loginParams.verifier,
                verifierParams,
                loginParams.idToken,
                null
            )
        }

        val isUpgraded = retrieveSharesResponse.metadata?.isUpgraded

        if (isUpgraded == true) {
            throw Exception(SFAError.getError(ErrorCode.USER_ALREADY_ENABLED_MFA))
        }

        return retrieveSharesResponse
    }

    fun connect(
        loginParams: LoginParams,
        ctx: Context
    ): SessionData {
        val torusKey = getTorusKey(loginParams)

        val publicAddress = torusKey.finalKeyData?.walletAddress
        val privateKey = if (torusKey.finalKeyData?.privKey?.isEmpty() == true) {
            torusKey.getoAuthKeyData().privKey
        } else {
            torusKey.finalKeyData?.privKey
        }

        var decodedUserInfo: UserInfo?

        try {
            val jwt = decodeJwt(loginParams.idToken)
            jwt.let {
                decodedUserInfo = UserInfo(
                    email = it.getClaim("email").asString() ?: "",
                    name = it.getClaim("name").asString() ?: "",
                    profileImage = it.getClaim("picture").asString() ?: "",
                    verifier = loginParams.verifier,
                    verifierId = loginParams.verifierId,
                    typeOfLogin = LoginType.JWT,
                    state = TorusGenericContainer(params = mapOf())
                )
            }
        } catch (e: Exception) {
            decodedUserInfo = loginParams.fallbackUserInfo
        }

        val sessionData = SessionData(
            privateKey = privateKey.toString(),
            publicAddress = publicAddress.toString(),
            signatures = torusKey.sessionData,
            userInfo = decodedUserInfo
        )

        val sessionId = SessionManager.generateRandomSessionKey()
        sessionManager.setSessionId(sessionId)
        sessionManager.createSession(gson.toJson(sessionData), ctx).whenComplete { result, err ->
            if (err == null) {
                SessionManager.saveSessionIdToStorage(result)
                sessionManager.setSessionId(result)
            }
        }

        this.state = sessionData
        return sessionData
    }

    private fun decodeJwt(token: String): JWT {
        return try {
            JWT(token)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to decode JWT token", e)
        }
    }

    fun logout(context: Context) {
        sessionManager.invalidateSession(context).whenComplete { res, _ ->
            if (res) {
                SessionManager.deleteSessionIdFromStorage()
                this.state = null
            }
        }
    }
}