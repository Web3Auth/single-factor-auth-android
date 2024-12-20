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

class SapphireDevnetTest {

    lateinit var singleFactorAuth: SingleFactorAuth
    private lateinit var web3AuthOptions: Web3AuthOptions
    lateinit var loginParams: LoginParams
    lateinit var algorithmRs: Algorithm
    var TEST_VERIFIER = "torus-test-health"
    var TEST_AGGREGRATE_VERIFIER = "torus-test-health-aggregate"
    var TORUS_TEST_EMAIL = "devnettestuser@tor.us"

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun shouldGetTorusKey() {
        val context = InstrumentationRegistry.getInstrumentation().context
        web3AuthOptions = Web3AuthOptions(
            "CLIENT ID", Web3AuthNetwork.SAPPHIRE_DEVNET, 86400
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
            assert("0x462A8BF111A55C9354425F875F89B22678c0Bc44" == sessionData.publicAddress)
            val requiredPrivateKey =
                BigInteger("230dad9f42039569e891e6b066ff5258b14e9764ef5176d74aeb594d1a744203", 16)
            assert(requiredPrivateKey.toString(16) == sessionData.privateKey)
        } else {
            fail()
        }
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun shouldAggregrateGetTorusKey() {
        val context = InstrumentationRegistry.getInstrumentation().context
        web3AuthOptions = Web3AuthOptions("CLIENT ID", Web3AuthNetwork.SAPPHIRE_DEVNET)
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
            BigInteger("edef171853fdf23ed3cfc702d32cf46f181b475a449d2f7b636924cabecd81d4", 16)
        if (sessionData != null) {
            assert(requiredPrivateKey.toString(16) == sessionData.privateKey)
            assert("0xfC58EB0475F1E3fa05877eE2e1350f6A619E2d78" == sessionData.publicAddress)
        } else {
            fail()
        }
    }
}