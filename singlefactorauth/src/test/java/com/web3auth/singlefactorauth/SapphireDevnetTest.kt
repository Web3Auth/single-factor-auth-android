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

class SapphireDevnetTest {

    lateinit var singleFactorAuth: SingleFactorAuth
    private lateinit var singleFactorAuthArgs: SingleFactorAuthArgs
    lateinit var loginParams: LoginParams
    lateinit var algorithmRs: Algorithm
    var TEST_VERIFIER = "torus-test-health"
    var TEST_AGGREGRATE_VERIFIER = "torus-test-health-aggregate"
    var TORUS_TEST_EMAIL = "saasas@tr.us"

    @DisplayName("Test getTorusKey")
    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun shouldGetTorusKey() {
        singleFactorAuthArgs = SingleFactorAuthArgs(
            TorusNetwork.SAPPHIRE_DEVNET,
            "BG4pe3aBso5SjVbpotFQGnXVHgxhgOxnqnNBKyjfEJ3izFvIVWUaMIzoCrAfYag8O6t6a6AOvdLcS4JR2sQMjR4"
        )
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
            BigInteger("04eb166ddcf59275a210c7289dca4a026f87a33fd2d6ed22f56efae7eab4052c", 16)
        assert(requiredPrivateKey == torusKey.privateKey)
        assertEquals("0x4924F91F5d6701dDd41042D94832bB17B76F316F", torusKey.publicAddress)
    }

    @DisplayName("Test Aggregate getTorusKey")
    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun shouldAggregrateGetTorusKey() {
        singleFactorAuthArgs = SingleFactorAuthArgs(
            TorusNetwork.SAPPHIRE_DEVNET,
            "BG4pe3aBso5SjVbpotFQGnXVHgxhgOxnqnNBKyjfEJ3izFvIVWUaMIzoCrAfYag8O6t6a6AOvdLcS4JR2sQMjR4"
        )
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
        val torusKey: TorusKey = singleFactorAuth.getKey(loginParams).get()
        val requiredPrivateKey =
            BigInteger("f0646a9e2b6776d003d8ab9beac748752f8b4a649fba7f9d0b10d87f4c11ce94", 16)
        assert(requiredPrivateKey == torusKey.privateKey)
        assertEquals("0x12db9ca82C26D767acEDc65FD0b3F12F886183e1", torusKey.publicAddress)
    }
}