package com.web3auth.singlefactorauth.types

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class UserInfo(
    @Keep val email: String,
    @Keep val name: String,
    @Keep val profileImage: String,
    @Keep val aggregateVerifier: String? = null,
    @Keep val verifier: String,
    @Keep val verifierId: String,
    @Keep val typeOfLogin: LoginType,
    @Keep val ref: String? = null,
    // @Keep val extraVerifierParams: PassKeyExtraParams? = null, // Uncomment when needed
    @Keep val accessToken: String? = null,
    @Keep val idToken: String? = null,
    @Keep val extraParams: String? = null,
    @Keep val extraParamsPassed: String? = null,
    @Keep val state: TorusGenericContainer
) : Serializable