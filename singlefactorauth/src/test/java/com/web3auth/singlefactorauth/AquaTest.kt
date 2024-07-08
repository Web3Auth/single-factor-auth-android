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

class AquaTest {

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
        singleFactorAuthArgs = SingleFactorAuthArgs(TorusNetwork.AQUA, "YOUR_CLIENT_ID")
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
            BigInteger("d8204e9f8c270647294c54acd8d49ee208789f981a7503158e122527d38626d8", 16)
        assert(requiredPrivateKey == torusKey.privateKey)
        assertEquals("0x8b32926cD9224fec3B296aA7250B049029434807", torusKey.publicAddress)
    }

    @DisplayName("Test Aggregate getTorusKey")
    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun shouldAggregrateGetTorusKey() {
        singleFactorAuthArgs = SingleFactorAuthArgs(TorusNetwork.AQUA, "YOUR_CLIENT_ID")
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
            BigInteger("6f8b884f19975fb0d138ed21b22a6a7e1b79e37f611d0a29f1442b34efc6bacd", 16)
        assert(requiredPrivateKey == torusKey.privateKey)
        assertEquals("0x62BaCa60f48C2b2b7e3074f7B7b4795EeF2afD2e", torusKey.publicAddress)
    }
}