package com.web3auth.singlefactorauth.types

data class SignMessage(
    val loginId: String,
    val sessionId: String,
    val platform: String = "android",
    val request: RequestData,
    val appState: String? = null,
    val sessionNamespace: String = "sfa"
)

data class RequestData(
    val method: String,
    val params: String
)