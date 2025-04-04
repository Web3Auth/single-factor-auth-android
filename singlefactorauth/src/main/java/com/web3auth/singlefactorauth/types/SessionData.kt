package com.web3auth.singlefactorauth.types

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
data class SessionData(
    @Keep @SerializedName("privKey") val privateKey: String,
    @Keep val publicAddress: String,
    @Keep val signatures: List<String>? = null,
    @Keep val userInfo: UserInfo? = null,
    @Keep val sessionNamespace: String = "sfa"
) : Serializable