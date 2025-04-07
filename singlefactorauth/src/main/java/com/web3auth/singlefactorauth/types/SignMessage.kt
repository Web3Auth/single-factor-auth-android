package com.web3auth.singlefactorauth.types

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class SignMessage(
    @Keep val loginId: String,
    @Keep val sessionId: String,
    @Keep val platform: String = "android",
    @Keep val request: RequestData,
    @Keep val appState: String? = null,
    @Keep val sessionNamespace: String = "sfa"
) : Serializable

@Keep
data class RequestData(
    @Keep val method: String,
    @Keep val params: String
) : Serializable