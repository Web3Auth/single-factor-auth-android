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

class SingleFactorAuthTest {

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
        singleFactorAuthArgs = SingleFactorAuthArgs(TorusNetwork.TESTNET)
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
            BigInteger("296045a5599afefda7afbdd1bf236358baff580a0fe2db62ae5c1bbe817fbae4", 16)
        assert(requiredPrivateKey == torusKey.privateKey)
        assertEquals("0x53010055542cCc0f2b6715a5c53838eC4aC96EF7", torusKey.publicAddress)
    }

    @DisplayName("Test Aggregate getTorusKey")
    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun shouldAggregrateGetTorusKey() {
        singleFactorAuthArgs = SingleFactorAuthArgs(TorusNetwork.TESTNET)
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
        val torusKey: TorusKey = singleFactorAuth!!.getKey(loginParams).get()
        val requiredPrivateKey =
            BigInteger("ad47959db4cb2e63e641bac285df1b944f54d1a1cecdaeea40042b60d53c35d2", 16)
        assert(requiredPrivateKey == torusKey.privateKey)
        assertEquals("0xE1155dB406dAD89DdeE9FB9EfC29C8EedC2A0C8B", torusKey.publicAddress)
    }
}