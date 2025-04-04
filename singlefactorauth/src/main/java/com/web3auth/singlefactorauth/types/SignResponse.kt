package com.web3auth.singlefactorauth.types

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class SignResponse(
    @Keep val success: Boolean,
    @Keep val result: String?,
    @Keep val error: String?
) : Serializable