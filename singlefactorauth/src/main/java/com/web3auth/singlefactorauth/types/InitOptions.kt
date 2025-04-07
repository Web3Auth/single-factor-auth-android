package com.web3auth.singlefactorauth.types

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class InitOptions(
    @Keep val clientId: String,
    @Keep val network: String,
    @Keep val redirectUrl: String? = null,
    @Keep val whiteLabel: String? = null,
    @Keep val buildEnv: String? = null,
    @Keep val sessionTime: Int? = null,
    @Keep val originData: String? = null
) : Serializable