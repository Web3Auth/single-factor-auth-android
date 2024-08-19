package com.web3auth.singlefactorauth

import androidx.test.platform.app.InstrumentationRegistry
import com.auth0.jwt.algorithms.Algorithm
import com.web3auth.singlefactorauth.types.LoginParams
import com.web3auth.singlefactorauth.types.SingleFactorAuthArgs
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

class SapphireDevnetTest {

    lateinit var singleFactorAuth: SingleFactorAuth
    private lateinit var sfaParams: SingleFactorAuthArgs
    lateinit var loginParams: LoginParams
    lateinit var algorithmRs: Algorithm
    var TEST_VERIFIER = "torus-test-health"
    var TEST_AGGREGRATE_VERIFIER = "torus-test-health-aggregate"
    var TORUS_TEST_EMAIL = "devnettestuser@tor.us"

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun shouldGetTorusKey() {
        val context = InstrumentationRegistry.getInstrumentation().context
        sfaParams = SingleFactorAuthArgs(
            Web3AuthNetwork.SAPPHIRE_DEVNET,
            "CLIENT ID", null, 0
        )
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
            assert("0x462A8BF111A55C9354425F875F89B22678c0Bc44" == TorusSFAKey.getPublicAddress())
            val requiredPrivateKey =
                BigInteger("230dad9f42039569e891e6b066ff5258b14e9764ef5176d74aeb594d1a744203", 16)
            assert(requiredPrivateKey.toString(16) == TorusSFAKey.getPrivateKey())
        } else {
            fail()
        }
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun shouldAggregrateGetTorusKey() {
        val context = InstrumentationRegistry.getInstrumentation().context
        sfaParams = SingleFactorAuthArgs(
            Web3AuthNetwork.SAPPHIRE_DEVNET,
            "CLIENT_ID"
        )
        singleFactorAuth = SingleFactorAuth(sfaParams,context)
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
            BigInteger("f0646a9e2b6776d003d8ab9beac748752f8b4a649fba7f9d0b10d87f4c11ce94", 16)
        if (TorusSFAKey != null) {
            assert(requiredPrivateKey.toString(16) == TorusSFAKey.getPrivateKey())
            assert("0x12db9ca82C26D767acEDc65FD0b3F12F886183e1" == TorusSFAKey.getPublicAddress())
        } else {
            fail()
        }
    }
}