package com.web3auth.singlefactorauth.types

import java.math.BigInteger

data class SFAKey(
    val privateKey: BigInteger? = BigInteger.ZERO,
    val publicAddress: String? = null,
)