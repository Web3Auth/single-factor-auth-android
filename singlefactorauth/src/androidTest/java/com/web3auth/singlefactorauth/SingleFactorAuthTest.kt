package com.web3auth.singlefactorauth

import androidx.test.platform.app.InstrumentationRegistry
import com.auth0.jwt.algorithms.Algorithm
import com.web3auth.singlefactorauth.types.LoginParams
import com.web3auth.singlefactorauth.types.SingleFactorAuthArgs
import com.web3auth.singlefactorauth.types.TorusSubVerifierInfo
import com.web3auth.singlefactorauth.utils.JwtUtils
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
    private lateinit var sfaParams: SingleFactorAuthArgs
    lateinit var loginParams: LoginParams
    lateinit var algorithmRs: Algorithm
    var TEST_VERIFIER = "torus-test-health"
    var TEST_AGGREGRATE_VERIFIER = "torus-test-health-aggregate"
    var TORUS_TEST_EMAIL = "hello@tor.us"

    // TODO: Debug this
    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun shouldGetTorusKey() {
        val context = InstrumentationRegistry.getInstrumentation().context
        sfaParams = SingleFactorAuthArgs(Web3AuthNetwork.TESTNET, "CLIENT_ID", null, 0)
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
        val TorusSFAKey = singleFactorAuth.getKey(loginParams)
        if (TorusSFAKey != null) {
            assert("0x53010055542cCc0f2b6715a5c53838eC4aC96EF7" == TorusSFAKey.getPublicAddress())
            val requiredPrivateKey =
                BigInteger("296045a5599afefda7afbdd1bf236358baff580a0fe2db62ae5c1bbe817fbae4", 16)
            assert(requiredPrivateKey.toString(16) == TorusSFAKey.getPrivateKey())
        } else {
            fail()
        }
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun shouldAggregrateGetTorusKey() {
        val context = InstrumentationRegistry.getInstrumentation().context
        sfaParams = SingleFactorAuthArgs(Web3AuthNetwork.TESTNET, "YOUR_CLIENT_ID")
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
        val TorusSFAKey = singleFactorAuth.getKey(loginParams)
        val requiredPrivateKey =
            BigInteger("ad47959db4cb2e63e641bac285df1b944f54d1a1cecdaeea40042b60d53c35d2", 16)
        if (TorusSFAKey != null) {
            assert(requiredPrivateKey.toString(16) == TorusSFAKey.getPrivateKey())
            assert("0xE1155dB406dAD89DdeE9FB9EfC29C8EedC2A0C8B" == TorusSFAKey.getPublicAddress())
        }  else {
            fail()
        }
    }
}