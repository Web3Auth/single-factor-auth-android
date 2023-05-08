package com.web3auth.singlefactorauth.types

import java.math.BigInteger

class TorusKey(val privateKey: BigInteger, val publicAddress: String) {

    override fun toString(): String {
        return "TorusKey{" +
                "privateKey='" + privateKey + '\'' +
                ", publicAddress='" + publicAddress + '\'' +
                '}'
    }
}