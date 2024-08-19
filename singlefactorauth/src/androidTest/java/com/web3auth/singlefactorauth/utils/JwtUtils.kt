package com.web3auth.singlefactorauth.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import net.andreinc.mockneat.MockNeat
import java.util.*

object JwtUtils {
    fun generateIdToken(email: String, alg: Algorithm): String {
        val today = Date()
        val calendar = Calendar.getInstance()
        calendar.time = today
        calendar.add(Calendar.MINUTE, 2)
        val modifiedDate = calendar.time
        return JWT.create()
            .withClaim("admin", false)
            .withClaim("name", email)
            .withClaim("email", email)
            .withSubject("email|" + email.split("@")[0]) // sub
            .withClaim("email_verified", true)
            .withAudience("torus-key-test") // aud
            .withExpiresAt(modifiedDate) // eat
            .withIssuer("torus-key-test") // iss
            .withIssuedAt(today) // iat
            .sign(alg)
    }

    val randomEmail: String
        get() {
            val mock = MockNeat.threadLocal()
            return mock.emails().`val`()
        }
}