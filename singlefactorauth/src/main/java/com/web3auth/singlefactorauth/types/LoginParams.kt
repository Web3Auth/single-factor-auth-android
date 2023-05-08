package com.web3auth.singlefactorauth.types

class LoginParams {
    val verifier: String
    val verifierId: String
    val idToken: String
    var subVerifierInfoArray: Array<TorusSubVerifierInfo>? = null

    constructor(verifier: String, verifierId: String, idToken: String) {
        this.verifier = verifier
        this.verifierId = verifierId
        this.idToken = idToken
    }

    constructor(
        verifier: String,
        verifierId: String,
        idToken: String,
        subVerifierInfoArray: Array<TorusSubVerifierInfo>
    ) {
        this.verifier = verifier
        this.verifierId = verifierId
        this.idToken = idToken
        this.subVerifierInfoArray = subVerifierInfoArray
    }
}