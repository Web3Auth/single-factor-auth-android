package com.web3auth.singlefactorauth.types

data class LoginParams(
    val verifier: String,
    val verifierId: String,
    val idToken: String,
    val subVerifierInfoArray: Array<TorusSubVerifierInfo>? = null,
    val serverTimeOffset: Int? = null,
    val fallbackUserInfo: UserInfo? = null
)