package com.web3auth.singlefactorauth.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import net.andreinc.mockneat.MockNeat
import java.util.*

object JwtUtils {
    fun generateIdToken(email: String, alg: Algorithm): String {
        return JWT.create()
            .withSubject("email|" + email.split("@").toTypedArray()[0])
            .withAudience("torus-key-test")
            .withExpiresAt(Date(System.currentTimeMillis() + 3600 * 1000))
            .withIssuedAt(Date())
            .withIssuer("torus-key-test")
            .withClaim("email", email)
            .withClaim("nickname", email.split("@").toTypedArray()[0])
            .withClaim("name", email)
            .withClaim("picture", "")
            .withClaim("email_verified", true)
            .sign(alg)
    }

    val randomEmail: String
        get() {
            val mock = MockNeat.threadLocal()
            return mock.emails().`val`()
        }
}