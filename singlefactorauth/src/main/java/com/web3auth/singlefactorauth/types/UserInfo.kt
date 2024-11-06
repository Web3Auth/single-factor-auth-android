package com.web3auth.singlefactorauth.types

import java.io.Serializable

data class UserInfo(
    val email: String,
    val name: String,
    val profileImage: String,
    val aggregateVerifier: String? = null,
    val verifier: String,
    val verifierId: String,
    val typeOfLogin: LoginType,
    val ref: String? = null,
    // val extraVerifierParams: PassKeyExtraParams? = null, // Uncomment when needed
    val accessToken: String? = null,
    val idToken: String? = null,
    val extraParams: String? = null,
    val extraParamsPassed: String? = null,
    val state: TorusGenericContainer
) : Serializable