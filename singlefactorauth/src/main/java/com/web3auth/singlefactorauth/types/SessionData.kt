package com.web3auth.singlefactorauth.types

import org.torusresearch.torusutils.types.SessionData
import java.io.Serializable

data class SessionData(
    val privateKey: String,
    val publicAddress: String,
    val signatures: SessionData? = null,
    val userInfo: UserInfo? = null
) : Serializable