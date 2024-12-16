package com.web3auth.singlefactorauth.types

import java.io.Serializable

data class SessionData(
    val privKey: String,
    val publicAddress: String,
    val signatures: List<String>? = null,
    val userInfo: UserInfo? = null,
    val sessionNamespace: String = "sfa"
) : Serializable