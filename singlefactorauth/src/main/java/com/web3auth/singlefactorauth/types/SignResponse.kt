package com.web3auth.singlefactorauth.types

data class SignResponse(
    val success: Boolean,
    val result: String?,
    val error: String?
)