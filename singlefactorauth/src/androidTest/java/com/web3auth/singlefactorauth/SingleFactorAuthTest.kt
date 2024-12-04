package com.web3auth.singlefactorauth

import androidx.test.platform.app.InstrumentationRegistry
import com.auth0.jwt.algorithms.Algorithm
import com.web3auth.singlefactorauth.types.LoginParams
import com.web3auth.singlefactorauth.types.TorusSubVerifierInfo
import com.web3auth.singlefactorauth.types.Web3AuthOptions
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

class SingleFactorAuthTest {

    lateinit var singleFactorAuth: SingleFactorAuth
    private lateinit var web3AuthOptions: Web3AuthOptions
    lateinit var loginParams: LoginParams
    lateinit var algorithmRs: Algorithm
    var TEST_VERIFIER = "torus-test-health"
    var TEST_AGGREGRATE_VERIFIER = "torus-test-health-aggregate"
    var TORUS_TEST_EMAIL = "hello@tor.us"

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun shouldGetTorusKey() {
        val context = InstrumentationRegistry.getInstrumentation().context
        web3AuthOptions = Web3AuthOptions(
            "CLIENT ID", Web3AuthNetwork.MAINNET, 86400
        )
        singleFactorAuth = SingleFactorAuth(web3AuthOptions, context)
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
        val sessionData = singleFactorAuth.connect(loginParams, context)
        if (sessionData != null) {
            assert("0x90A926b698047b4A87265ba1E9D8b512E8489067" == sessionData.publicAddress)
            val requiredPrivateKey =
                BigInteger(
                    "0129494416ab5d5f674692b39fa49680e07d3aac01b9683ee7650e40805d4c44",
                    16
                ).toString(16).padStart(64, '0')
            assert(requiredPrivateKey == sessionData.privateKey)
        } else {
            fail()
        }
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun shouldAggregrateGetTorusKey() {
        val context = InstrumentationRegistry.getInstrumentation().context
        web3AuthOptions = Web3AuthOptions("CLIENT ID", Web3AuthNetwork.MAINNET)
        singleFactorAuth = SingleFactorAuth(web3AuthOptions, context)
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
        val sessionData = singleFactorAuth.connect(loginParams, context)
        val requiredPrivateKey =
            BigInteger("68390578bbdab74e9883de58d3919c176662852bdd42a783bc3a08f1a1024e0c", 16)
        if (sessionData != null) {
            assert(requiredPrivateKey.toString(16) == sessionData.privateKey)
            assert("0x86129bC541b03B6B42A76E9e002eE88F81E0aadD" == sessionData.publicAddress)
        } else {
            fail()
        }
    }
}