package com.web3auth.singlefactorauth.types

import org.torusresearch.torusutils.types.FinalKeyData
import org.torusresearch.torusutils.types.RetrieveSharesResponse
import org.torusresearch.torusutils.types.SessionData
import java.math.BigInteger

data class TorusKey(
    val privateKey: BigInteger? = BigInteger.ZERO,
    val publicAddress: String? = null,
    val retrieveSharesResponse: RetrieveSharesResponse? = null,
    val finalKeyData: FinalKeyData? = null,
    val oAuthKeyData: FinalKeyData? = null,
    val metadata: org.torusresearch.torusutils.types.Metadata? = null,
    val sessionData: SessionData? = null
)