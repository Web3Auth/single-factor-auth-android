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

class SapphireMainnetTest {

    lateinit var singleFactorAuth: SingleFactorAuth
    private lateinit var web3AuthOptions: Web3AuthOptions
    lateinit var loginParams: LoginParams
    lateinit var algorithmRs: Algorithm
    var TEST_VERIFIER = "torus-test-health"
    var TEST_AGGREGRATE_VERIFIER = "torus-aggregate-sapphire-mainnet"
    var TORUS_TEST_EMAIL = "devnettestuser@tor.us"

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun shouldGetTorusKey() {
        val context = InstrumentationRegistry.getInstrumentation().context
        web3AuthOptions = Web3AuthOptions(
            "CLIENT ID", Web3AuthNetwork.SAPPHIRE_MAINNET,
            86400
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
            assert("0x0934d844a0a6db37CF75aF0269436ae1b2Ae5D36" == sessionData.publicAddress)
            val requiredPrivateKey =
                BigInteger("2c4b346a91ecd11fe8a02d111d00bd921bf9b543f0a1e811face91b5f28947d6", 16)
            assert(requiredPrivateKey.toString(16) == sessionData.privateKey)
        } else {
            fail()
        }
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun shouldAggregrateGetTorusKey() {
        val context = InstrumentationRegistry.getInstrumentation().context
        web3AuthOptions = Web3AuthOptions(
            "CLIENT ID",
            Web3AuthNetwork.SAPPHIRE_MAINNET
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
        loginParams = LoginParams(
            TEST_AGGREGRATE_VERIFIER, TORUS_TEST_EMAIL, idToken, arrayOf(
                TorusSubVerifierInfo(
                    TEST_VERIFIER, idToken
                )
            )
        )
        val sessionData = singleFactorAuth.connect(loginParams, context)
        val requiredPrivateKey =
            BigInteger("0c724bb285560dc41e585b91aa2ded94fdd703c2e7133dcc64b1361b0d1fd105", 16)

        if (sessionData != null) {
            assert(requiredPrivateKey.toString(16).padStart(64, '0') == sessionData.privateKey)
            assert(
                "0xA92E2C756B5b2abABc127907b02D4707dc085612" ==
                        sessionData.publicAddress
            )
        } else {
            fail()
        }
    }
}