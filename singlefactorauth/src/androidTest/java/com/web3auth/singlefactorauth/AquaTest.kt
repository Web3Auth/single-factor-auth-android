package com.web3auth.singlefactorauth

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.auth0.jwt.algorithms.Algorithm
import com.web3auth.singlefactorauth.types.LoginParams
import com.web3auth.singlefactorauth.types.TorusSubVerifierInfo
import com.web3auth.singlefactorauth.types.Web3AuthOptions
import com.web3auth.singlefactorauth.utils.JwtUtils.generateIdToken
import com.web3auth.singlefactorauth.utils.PemUtils.readPrivateKeyFromReader
import com.web3auth.singlefactorauth.utils.WellKnownSecret
import junit.framework.TestCase.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.torusresearch.fetchnodedetails.types.Web3AuthNetwork
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECPublicKeySpec
import java.util.concurrent.ExecutionException

@RunWith(AndroidJUnit4::class)
class AquaTest {

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

        val context = getInstrumentation().context
        web3AuthOptions = Web3AuthOptions("YOUR_CLIENT_ID", Web3AuthNetwork.AQUA, 86400)
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
        singleFactorAuth.initialize(context)
        val requiredPrivateKey =
            BigInteger("d8204e9f8c270647294c54acd8d49ee208789f981a7503158e122527d38626d8", 16)
        if (sessionData != null) {
            assert(requiredPrivateKey.toString(16) == sessionData.privateKey)
            assert("0x8b32926cD9224fec3B296aA7250B049029434807" == sessionData.publicAddress)
        } else {
            fail()
        }
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun shouldAggregrateGetTorusKey() {
        web3AuthOptions = Web3AuthOptions("YOUR_CLIENT_ID", Web3AuthNetwork.AQUA)
        val context = getInstrumentation().context
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
        singleFactorAuth.initialize(context)
        val requiredPrivateKey =
            BigInteger("6f8b884f19975fb0d138ed21b22a6a7e1b79e37f611d0a29f1442b34efc6bacd", 16)
        if (sessionData != null) {
            assert(requiredPrivateKey.toString(16) == sessionData.privateKey)
            assert("0x62BaCa60f48C2b2b7e3074f7B7b4795EeF2afD2e" == sessionData.publicAddress)
        } else {
            fail()
        }
    }
}