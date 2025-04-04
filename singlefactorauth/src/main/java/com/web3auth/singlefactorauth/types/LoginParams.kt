package com.web3auth.singlefactorauth.types

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class LoginParams(
    @Keep val verifier: String,
    @Keep val verifierId: String,
    @Keep val idToken: String,
    @Keep val subVerifierInfoArray: Array<TorusSubVerifierInfo>? = null,
    @Keep val serverTimeOffset: Int? = null,
    @Keep val fallbackUserInfo: UserInfo? = null
) : Serializable