package com.web3auth.singlefactorauth

import com.auth0.jwt.algorithms.Algorithm
import com.web3auth.singlefactorauth.types.LoginParams
import com.web3auth.singlefactorauth.types.SingleFactorAuthArgs
import com.web3auth.singlefactorauth.types.TorusKey
import com.web3auth.singlefactorauth.types.TorusSubVerifierInfo
import com.web3auth.singlefactorauth.utils.JwtUtils
import com.web3auth.singlefactorauth.utils.PemUtils
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.torusresearch.fetchnodedetails.types.TorusNetwork
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECPublicKeySpec
import java.util.concurrent.ExecutionException

class CyanTest {

    lateinit var singleFactorAuth: SingleFactorAuth
    private lateinit var singleFactorAuthArgs: SingleFactorAuthArgs
    lateinit var loginParams: LoginParams
    lateinit var algorithmRs: Algorithm
    var TEST_VERIFIER = "torus-test-health"
    var TEST_AGGREGRATE_VERIFIER = "torus-test-health-aggregate"
    var TORUS_TEST_EMAIL = "hello@tor.us"

    @DisplayName("Test getTorusKey")
    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun shouldGetTorusKey() {
        singleFactorAuthArgs = SingleFactorAuthArgs(TorusNetwork.CYAN)
        singleFactorAuth = SingleFactorAuth(singleFactorAuthArgs)
        val privateKey = PemUtils.readPrivateKeyFromFile(
            "src/test/java/com/web3Auth/singlefactorauth/keys/key.pem",
            "EC"
        ) as ECPrivateKey
        val publicKey = KeyFactory.getInstance("EC").generatePublic(
            ECPublicKeySpec(
                privateKey.params.generator,
                privateKey.params
            )
        ) as ECPublicKey
        algorithmRs = Algorithm.ECDSA256(publicKey, privateKey)
        val idToken: String = JwtUtils.generateIdToken(TORUS_TEST_EMAIL, algorithmRs)
        loginParams = LoginParams(TEST_VERIFIER, TORUS_TEST_EMAIL, idToken)
        val torusKey: TorusKey = singleFactorAuth.getKey(loginParams).get()
        val requiredPrivateKey =
            BigInteger("44ca9a8ac5167ff11e0b48731f7bfde141fbbb0711d0abb54d5da554fb6fd40a", 16)
        assert(requiredPrivateKey == torusKey.privateKey)
        assertEquals("0x1bbc291d4a8DCcb55fd969568D56b72a4BF62be8", torusKey.publicAddress)
    }

    @DisplayName("Test Aggregate getTorusKey")
    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun shouldAggregrateGetTorusKey() {
        singleFactorAuthArgs = SingleFactorAuthArgs(TorusNetwork.CYAN)
        singleFactorAuth = SingleFactorAuth(singleFactorAuthArgs)
        val privateKey = PemUtils.readPrivateKeyFromFile(
            "src/test/java/com/web3Auth/singlefactorauth/keys/key.pem",
            "EC"
        ) as ECPrivateKey
        val publicKey = KeyFactory.getInstance("EC").generatePublic(
            ECPublicKeySpec(
                privateKey.params.generator,
                privateKey.params
            )
        ) as ECPublicKey
        algorithmRs = Algorithm.ECDSA256(publicKey, privateKey)
        val idToken: String = JwtUtils.generateIdToken(TORUS_TEST_EMAIL, algorithmRs)
        loginParams = LoginParams(
            TEST_AGGREGRATE_VERIFIER, TORUS_TEST_EMAIL, idToken, arrayOf(
                TorusSubVerifierInfo(
                    TEST_VERIFIER, idToken
                )
            )
        )
        val torusKey: TorusKey = singleFactorAuth.getKey(loginParams).get()
        val requiredPrivateKey =
            BigInteger("66af498ea82c95d52fdb8c8dedd44cf2f758424a0eecab7ac3dd8721527ea2d4", 16)
        assert(requiredPrivateKey == torusKey.privateKey)
        assertEquals("0xFF4c4A0Aa5D633302B5711C3047D7D5967884521", torusKey.publicAddress)
    }
}