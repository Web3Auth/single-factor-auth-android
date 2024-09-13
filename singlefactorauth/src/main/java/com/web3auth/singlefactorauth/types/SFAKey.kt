package com.web3auth.singlefactorauth.types

class SFAKey(
    private val privateKey: String,
    private val publicAddress: String
    ) {

    fun getPrivateKey(): String {
        return privateKey
    }

    fun getPublicAddress(): String {
        return publicAddress
    }

    // This class needs to implement @Serializable
    override fun toString(): String {
        return "{privateKey=$privateKey, publicAddress=$publicAddress}"
    }
}