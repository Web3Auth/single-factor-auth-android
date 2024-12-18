package com.web3auth.singlefactorauth.types

data class InitOptions(
    val clientId: String,
    val network: String,
    val redirectUrl: String? = null,
    val whiteLabel: String? = null,
    val buildEnv: String? = null,
    val sessionTime: Int? = null,
    val originData: String? = null
)