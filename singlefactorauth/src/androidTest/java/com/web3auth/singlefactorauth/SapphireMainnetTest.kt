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

class SapphireMainnetTest {

    lateinit var singleFactorAuth: SingleFactorAuth
    private lateinit var sfaParams: SingleFactorAuthArgs
    lateinit var loginParams: LoginParams
    lateinit var algorithmRs: Algorithm
    var TEST_VERIFIER = "torus-test-health"
    var TEST_AGGREGRATE_VERIFIER = "torus-aggregate-sapphire-mainnet"
    var TORUS_TEST_EMAIL = "devnettestuser@tor.us"

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun shouldGetTorusKey() {
        val context = InstrumentationRegistry.getInstrumentation().context
        sfaParams = SingleFactorAuthArgs(
            Web3AuthNetwork.SAPPHIRE_MAINNET,
            "CLIENT ID",
            null, 0
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
        loginParams = LoginParams(TEST_VERIFIER,TORUS_TEST_EMAIL, idToken)
        val TorusSFAKey = singleFactorAuth.getKey(loginParams)
        if (TorusSFAKey != null) {
            assert("0x0934d844a0a6db37CF75aF0269436ae1b2Ae5D36" == TorusSFAKey.getPublicAddress())
            val requiredPrivateKey =
                BigInteger("2c4b346a91ecd11fe8a02d111d00bd921bf9b543f0a1e811face91b5f28947d6", 16)
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
            Web3AuthNetwork.SAPPHIRE_MAINNET,
            "CLIENT_ID"
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
        loginParams = LoginParams(
            TEST_AGGREGRATE_VERIFIER, TORUS_TEST_EMAIL, idToken, arrayOf(
                TorusSubVerifierInfo(
                    TEST_VERIFIER, idToken
                )
            )
        )
        val TorusSFAKey = singleFactorAuth.getKey(loginParams)
        val requiredPrivateKey =
            BigInteger("9a8c7d58d4246507cdd6b2c34850eac52a35c4d6ebea8cefbec26010ad8011d6", 16)

        if (TorusSFAKey != null) {
            assert(requiredPrivateKey.toString(16) == TorusSFAKey.getPrivateKey())
            assert(
                "0xFC891f704CF73D01e24F2be24f6afF3C2ab19C98" ==
                TorusSFAKey.getPublicAddress()
            )
        } else {
            fail()
        }
    }
}