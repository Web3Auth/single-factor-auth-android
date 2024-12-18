package com.web3auth.singlefactorauth.types

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SessionData(
    @SerializedName("privKey") val privateKey: String,
    val publicAddress: String,
    val signatures: List<String>? = null,
    val userInfo: UserInfo? = null,
    val sessionNamespace: String = "sfa"
) : Serializable