package com.web3auth.singlefactorauth.types

import org.torusresearch.torusutils.types.SessionData
import java.io.Serializable

data class SessionData(
    val privKey: String,
    val publicAddress: String,
    val signatures: SessionData? = null,
    val userInfo: UserInfo? = null,
    val sessionNamespace: String = "sfa"
) : Serializable