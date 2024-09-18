package com.web3auth.singlefactorauth

import androidx.test.platform.app.InstrumentationRegistry
import com.auth0.jwt.algorithms.Algorithm
import com.web3auth.singlefactorauth.types.LoginParams
import com.web3auth.singlefactorauth.types.SFAParams
import com.web3auth.singlefactorauth.types.TorusSubVerifierInfo
import com.web3auth.singlefactorauth.utils.JwtUtils.generateIdToken
import com.web3auth.singlefactorauth.utils.PemUtils.readPrivateKeyFromReader
import com.web3auth.singlefactorauth.utils.WellKnownSecret
import junit.framework.TestCase.fail
import org.junit.Test
import org.torusresearch.fetchnodedetails.types.Web3AuthNetwork
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECPublicKeySpec
import java.util.concurrent.ExecutionException

class CyanTest {

    lateinit var singleFactorAuth: SingleFactorAuth
    private lateinit var sfaParams: SFAParams
    lateinit var loginParams: LoginParams
    lateinit var algorithmRs: Algorithm
    var TEST_VERIFIER = "torus-test-health"
    var TEST_AGGREGRATE_VERIFIER = "torus-test-health-aggregate"
    var TORUS_TEST_EMAIL = "hello@tor.us"

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun shouldGetTorusKey() {
        val context = InstrumentationRegistry.getInstrumentation().context
        sfaParams = SFAParams(Web3AuthNetwork.CYAN, "YOUR_CLIENT_ID", 86400, null, 0)
        singleFactorAuth = SingleFactorAuth(sfaParams, context)
        val privateKey = readPrivateKeyFromReader(
            WellKnownSecret.pem(),
            "EC"
        ) as ECPrivateKey
        val publicKey = KeyFactory.getInstance("EC").generatePublic(
            ECPublicKeySpec(
                privateKey.params.generator,
                privateKey.params
            )
        ) as ECPublicKey
        algorithmRs = Algorithm.ECDSA256(publicKey, privateKey)
        val idToken: String = generateIdToken(TORUS_TEST_EMAIL, algorithmRs)
        loginParams = LoginParams(TEST_VERIFIER, TORUS_TEST_EMAIL, idToken)
        val sfakey = singleFactorAuth.connect(loginParams, context)
        if (sfakey != null) {
            assert("0x6b902fBCEb0E0374e5eB9eDFe68cD4B888c32150" == sfakey.getPublicAddress())
            val requiredPrivateKey =
            BigInteger("223d982054fa1ad27d1497560521e4cce5b8c6438c38533c7bad27ff21ce0546", 16)
            assert(requiredPrivateKey.toString(16) == sfakey.getPrivateKey())
        } else {
            fail()
        }
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun shouldAggregrateGetTorusKey() {
        val context = InstrumentationRegistry.getInstrumentation().context
        sfaParams = SFAParams(Web3AuthNetwork.CYAN, "YOUR_CLIENT_ID")
        singleFactorAuth = SingleFactorAuth(sfaParams, context)
        val privateKey = readPrivateKeyFromReader(
            WellKnownSecret.pem(),
            "EC"
        ) as ECPrivateKey
        val publicKey = KeyFactory.getInstance("EC").generatePublic(
            ECPublicKeySpec(
                privateKey.params.generator,
                privateKey.params
            )
        ) as ECPublicKey
        algorithmRs = Algorithm.ECDSA256(publicKey, privateKey)
        val idToken: String = generateIdToken(TORUS_TEST_EMAIL, algorithmRs)
        loginParams = LoginParams(
            TEST_AGGREGRATE_VERIFIER, TORUS_TEST_EMAIL, idToken, arrayOf(
                TorusSubVerifierInfo(
                    TEST_VERIFIER, idToken
                )
            )
        )
        val sfakey = singleFactorAuth.connect(loginParams, context)
        if (sfakey != null) {
            val requiredPrivateKey =
                BigInteger("66af498ea82c95d52fdb8c8dedd44cf2f758424a0eecab7ac3dd8721527ea2d4", 16)
            assert(requiredPrivateKey.toString(16) == sfakey.getPrivateKey())
            assert("0xFF4c4A0Aa5D633302B5711C3047D7D5967884521" == sfakey.getPublicAddress())
        } else {
            fail()
        }
    }
}