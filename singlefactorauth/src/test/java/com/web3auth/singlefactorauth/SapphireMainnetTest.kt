package com.web3auth.singlefactorauth

import com.auth0.jwt.algorithms.Algorithm
import com.web3auth.singlefactorauth.types.LoginParams
import com.web3auth.singlefactorauth.types.SFAParams
import com.web3auth.singlefactorauth.types.TorusSubVerifierInfo
import com.web3auth.singlefactorauth.utils.JwtUtils
import com.web3auth.singlefactorauth.utils.PemUtils
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.torusresearch.fetchnodedetails.types.Web3AuthNetwork
import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECPublicKeySpec
import java.util.concurrent.ExecutionException

class SapphireMainnetTest {

    lateinit var singleFactorAuth: SingleFactorAuth
    private lateinit var sfaParams: SFAParams
    lateinit var loginParams: LoginParams
    lateinit var algorithmRs: Algorithm
    var TEST_VERIFIER = "torus-test-health"
    var TEST_AGGREGRATE_VERIFIER = "torus-aggregate-sapphire-mainnet"
    var TORUS_TEST_EMAIL = "hello@tor.us"

    @DisplayName("Test getTorusKey")
    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun shouldGetTorusKey() {
        //clientId is mandatory field.
        sfaParams = SFAParams(
            Web3AuthNetwork.SAPPHIRE_MAINNET,
            "BLuMSgycHD7DfSvbmN3ISZ5WkdpIjtByKi_cD9ASg_NS3jUYmrrH-dMuJU16z11cev5YocCWLAjWVfq95tFlOD8",
            false
        )
        singleFactorAuth = SingleFactorAuth(sfaParams)
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
        val sfaKey = singleFactorAuth.getKey(loginParams).get()
        val requiredPrivateKey =
            BigInteger("dfb39b84e0c64b8c44605151bf8670ae6eda232056265434729b6a8a50fa3419", 16)
        assert(requiredPrivateKey == sfaKey.privateKey)
        assertEquals("0x70520A7F04868ACad901683699Fa32765C9F6871", sfaKey.publicAddress)
    }

    @DisplayName("Test Aggregate getTorusKey")
    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun shouldAggregrateGetTorusKey() {
        //clientId is mandatory field.
        sfaParams = SFAParams(
            Web3AuthNetwork.SAPPHIRE_MAINNET,
            "YOUR_CLIENT_ID"
        )
        singleFactorAuth = SingleFactorAuth(sfaParams)
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
        val sfaKey = singleFactorAuth.getKey(loginParams).get()
        val requiredPrivateKey =
            BigInteger("9a8c7d58d4246507cdd6b2c34850eac52a35c4d6ebea8cefbec26010ad8011d6", 16)
        assert(requiredPrivateKey == sfaKey.privateKey)
        assertEquals("0xFC891f704CF73D01e24F2be24f6afF3C2ab19C98", sfaKey.publicAddress)
    }
}