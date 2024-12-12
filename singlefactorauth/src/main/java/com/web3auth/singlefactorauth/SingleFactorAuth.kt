package com.web3auth.singlefactorauth

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.auth0.android.jwt.JWT
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.web3auth.session_manager_android.SessionManager
import com.web3auth.singlefactorauth.api.ApiHelper
import com.web3auth.singlefactorauth.api.ApiService
import com.web3auth.singlefactorauth.types.ChainConfig
import com.web3auth.singlefactorauth.types.ErrorCode
import com.web3auth.singlefactorauth.types.InitOptions
import com.web3auth.singlefactorauth.types.LoginParams
import com.web3auth.singlefactorauth.types.LoginType
import com.web3auth.singlefactorauth.types.REDIRECT_URL
import com.web3auth.singlefactorauth.types.RequestData
import com.web3auth.singlefactorauth.types.SFAError
import com.web3auth.singlefactorauth.types.SessionData
import com.web3auth.singlefactorauth.types.SignMessage
import com.web3auth.singlefactorauth.types.SignResponse
import com.web3auth.singlefactorauth.types.TorusGenericContainer
import com.web3auth.singlefactorauth.types.TorusSubVerifierInfo
import com.web3auth.singlefactorauth.types.UserInfo
import com.web3auth.singlefactorauth.types.WEBVIEW_URL
import com.web3auth.singlefactorauth.types.Web3AuthOptions
import com.web3auth.singlefactorauth.types.WebViewResultCallback
import com.web3auth.singlefactorauth.types.merge
import com.web3auth.singlefactorauth.types.mergeMaps
import com.web3auth.singlefactorauth.types.toBase64URLString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
import java.util.Locale
import java.util.concurrent.CompletableFuture

class SingleFactorAuth(
    web3AuthOptions: Web3AuthOptions,
    ctx: Context,
) : ContextWrapper(ctx), WebViewResultCallback {
    private var nodeDetailManager: FetchNodeDetails =
        FetchNodeDetails(web3AuthOptions.web3AuthNetwork)
    private val torusUtils: TorusUtils
    private var sessionManager: SessionManager
    private val gson = GsonBuilder().disableHtmlEscaping().create()
    private var network: Web3AuthNetwork
    private var state: SessionData? = null
    private var web3AuthOption = web3AuthOptions
    private lateinit var signMsgCF: CompletableFuture<SignResponse>

    init {
        val torusOptions = TorusOptions(
            web3AuthOptions.clientId, web3AuthOptions.web3AuthNetwork, null,
            web3AuthOptions.serverTimeOffset, true
        )
        network = web3AuthOptions.web3AuthNetwork
        torusUtils = TorusUtils(torusOptions)
        sessionManager = SessionManager(ctx, web3AuthOptions.sessionTime, ctx.packageName)
    }

    fun initialize(ctx: Context): CompletableFuture<Nothing?> {
        return CompletableFuture.supplyAsync {
            val savedSessionId = SessionManager.getSessionIdFromStorage()
            sessionManager.setSessionId(savedSessionId)

            if (savedSessionId.isEmpty()) {
                return@supplyAsync null
            }

            val dataFuture = sessionManager.authorizeSession(ctx.packageName, ctx)
            dataFuture.whenComplete { response, error ->
                if (error != null) {
                    throw Exception("Failed to authorize session", error)
                }
            }

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

            state = SessionData(
                privateKey = privateKey,
                publicAddress = publicAddress,
                signatures = finalSignatures,
                userInfo = finalUserInfo
            )
            state
        }.thenApplyAsync({ _ ->
            null
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
                    typeOfLogin = LoginType.jwt,
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

    fun logout(context: Context): CompletableFuture<Void> {
        val logoutCF = CompletableFuture<Void>()
        sessionManager.invalidateSession(context).whenComplete { res, err ->
            if (res) {
                SessionManager.deleteSessionIdFromStorage()
                this.state = null
                logoutCF.complete(null)
            } else {
                logoutCF.completeExceptionally(err)
            }
        }
        return logoutCF
    }

    private fun fetchProjectConfig(): CompletableFuture<Boolean> {
        val projectConfigCompletableFuture: CompletableFuture<Boolean> = CompletableFuture()
        val web3AuthApi =
            ApiHelper.getInstance(web3AuthOption.web3AuthNetwork.name)
                .create(ApiService::class.java)
        if (!ApiHelper.isNetworkAvailable(baseContext)) {
            throw Exception(
                SFAError.getError(ErrorCode.RUNTIME_ERROR)
            )
        }
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val result = web3AuthApi.fetchProjectConfig(
                    web3AuthOption.clientId,
                    web3AuthOption.web3AuthNetwork.name.lowercase()
                )
                if (result.isSuccessful && result.body() != null) {
                    val response = result.body()
                    web3AuthOption.originData =
                        web3AuthOption.originData.mergeMaps(response?.whitelist?.signed_urls)
                    if (response?.whitelabel != null) {
                        if (web3AuthOption.whiteLabel == null) {
                            web3AuthOption.whiteLabel = response.whitelabel
                        } else {
                            web3AuthOption.whiteLabel =
                                web3AuthOption.whiteLabel?.merge(response.whitelabel)
                        }
                    }
                    projectConfigCompletableFuture.complete(true)
                } else {
                    projectConfigCompletableFuture.completeExceptionally(
                        Exception(
                            SFAError.getError(
                                ErrorCode.RUNTIME_ERROR
                            )
                        )
                    )
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                projectConfigCompletableFuture.completeExceptionally(
                    Exception(
                        SFAError.getError(
                            ErrorCode.SOMETHING_WENT_WRONG
                        )
                    )
                )
            }
        }
        return projectConfigCompletableFuture
    }

    private fun getInitOptions(): InitOptions {
        return InitOptions(
            clientId = web3AuthOption.clientId,
            network = web3AuthOption.web3AuthNetwork.name.lowercase(Locale.ROOT),
            redirectUrl = web3AuthOption.redirectUrl.toString(),
            whiteLabel = web3AuthOption.whiteLabel?.let { gson.toJson(it) },
            buildEnv = web3AuthOption.buildEnv?.name?.lowercase(Locale.ROOT),
            sessionTime = web3AuthOption.sessionTime,
            originData = web3AuthOption.originData?.let { gson.toJson(it) }
        )
    }

    /**
     * Launches the wallet services asynchronously.
     *
     * @param chainConfig The configuration details of the blockchain network.
     * @param path The path where the wallet services will be launched. Default value is "wallet".
     * @return A CompletableFuture<Void> representing the asynchronous operation.
     */
    fun showWalletUI(
        chainConfig: ChainConfig,
        path: String? = "wallet"
    ): CompletableFuture<Void> {
        val launchWalletServiceCF: CompletableFuture<Void> = CompletableFuture()
        val sessionId = SessionManager.getSessionIdFromStorage()
        fetchProjectConfig().whenComplete { _, err ->
            if (err == null) {
                if (sessionId.isNotBlank()) {
                    val sdkUrl = Uri.parse(web3AuthOption.walletSdkUrl)

                    val initOptions = JSONObject(gson.toJson(getInitOptions()))
                    initOptions.put(
                        "chainConfig", gson.toJson(chainConfig)
                    )

                    val paramMap = JSONObject()
                    paramMap.put(
                        "options", initOptions
                    )

                    val loginIdCf = getLoginId(paramMap)

                    loginIdCf.whenComplete { loginId, error ->
                        if (error == null) {
                            val walletMap = JsonObject()
                            walletMap.addProperty(
                                "loginId", loginId
                            )
                            walletMap.addProperty("sessionId", sessionId)
                            walletMap.addProperty("platform", "android")
                            walletMap.addProperty("sessionNamespace", "sfa")

                            val walletHash =
                                "b64Params=" + gson.toJson(walletMap).toByteArray(Charsets.UTF_8)
                                    .toBase64URLString()

                            val url =
                                Uri.Builder().scheme(sdkUrl.scheme)
                                    .encodedAuthority(sdkUrl.encodedAuthority)
                                    .encodedPath(sdkUrl.encodedPath).appendPath(path)
                                    .fragment(walletHash).build()
                            //print("wallet launch url: => $url")
                            val intent = Intent(baseContext, WebViewActivity::class.java)
                            intent.putExtra(WEBVIEW_URL, url.toString())
                            baseContext.startActivity(intent)
                            launchWalletServiceCF.complete(null)
                        }
                    }
                } else {
                    launchWalletServiceCF.completeExceptionally(Exception("Please login first to launch wallet"))
                }
            } else {
                launchWalletServiceCF.completeExceptionally(err)
            }
        }
        return launchWalletServiceCF
    }

    /**
     * Signs a message asynchronously.
     *
     * @param chainConfig The configuration details of the blockchain network.
     * @param method The method name of the request.
     * @param requestParams The parameters of the request in JSON array format.
     * @param path The path where the signing service is located. Default value is "wallet/request".
     * @return A CompletableFuture<Void> representing the asynchronous operation.
     */
    fun request(
        chainConfig: ChainConfig,
        method: String,
        requestParams: JsonArray,
        path: String? = "wallet/request",
        appState: String? = null
    ): CompletableFuture<SignResponse> {
        signMsgCF = CompletableFuture()
        WebViewActivity.webViewResultCallback = this

        val sessionId = SessionManager.getSessionIdFromStorage()
        if (sessionId.isNotBlank()) {
            val sdkUrl = Uri.parse(web3AuthOption.walletSdkUrl)
            val initOptions = JSONObject(gson.toJson(getInitOptions()))
            initOptions.put(
                "chainConfig", gson.toJson(chainConfig)
            )
            val paramMap = JSONObject()
            paramMap.put(
                "options", initOptions
            )

            paramMap.put("sessionNamespace", "sfa")

            val loginIdCf = getLoginId(paramMap)

            loginIdCf.whenComplete { loginId, error ->
                if (error == null) {
                    val signMessageMap = SignMessage(
                        loginId = loginId,
                        sessionId = sessionId,
                        request = RequestData(
                            method = method,
                            params = gson.toJson(requestParams)
                        ),
                        appState = appState.let { it },
                        sessionNamespace = "sfa"
                    )

                    val signMessageHash =
                        "b64Params=" + gson.toJson(signMessageMap).toByteArray(Charsets.UTF_8)
                            .toBase64URLString()

                    val url =
                        Uri.Builder().scheme(sdkUrl.scheme)
                            .encodedAuthority(sdkUrl.encodedAuthority)
                            .encodedPath(sdkUrl.encodedPath).appendEncodedPath(path)
                            .fragment(signMessageHash).build()
                    //print("message signing url: => $url")
                    val intent = Intent(baseContext, WebViewActivity::class.java)
                    intent.putExtra(WEBVIEW_URL, url.toString())
                    intent.putExtra(REDIRECT_URL, web3AuthOption.redirectUrl.toString())
                    baseContext.startActivity(intent)
                }
            }
        } else {
            runOnUIThread {
                signMsgCF.completeExceptionally(Exception("Please login first to launch wallet"))
            }
        }
        return signMsgCF
    }

    /**
     * Retrieves the login ID from the provided JSONObject asynchronously.
     *
     * @param jsonObject The JSONObject from which to retrieve the login ID.
     * @return A CompletableFuture<String> representing the asynchronous operation, containing the login ID.
     */
    private fun getLoginId(jsonObject: JSONObject): CompletableFuture<String> {
        val sessionId = SessionManager.generateRandomSessionKey()
        sessionManager.setSessionId(sessionId)
        return sessionManager.createSession(
            jsonObject.toString(),
            baseContext,
        )
    }

    private fun runOnUIThread(action: () -> Unit) {
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(action)
    }

    override fun onSignResponseReceived(signResponse: SignResponse?) {
        if (signResponse != null) {
            signMsgCF.complete(signResponse)
        }
    }

    override fun onWebViewCancelled() {
        signMsgCF.completeExceptionally(Exception("User cancelled the operation."))
    }
}