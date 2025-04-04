package com.web3auth.singlefactorauth.types

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class TorusGenericContainer(
    @Keep val params: Map<String, String>
) : Serializable