package com.web3auth.sfaexample

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.security.PrivateKey
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*

object JwtUtils {
    fun generateIdToken(email: String): String {
        return Jwts.builder()
            .setSubject("email|" + email.split("@".toRegex()).toTypedArray()[0])
            .setAudience("torus-key-test")
            .setExpiration(Date(System.currentTimeMillis() + 3600 * 1000))
            .setIssuedAt(Date())
            .setIssuer("torus-key-test")
            .claim("email", email)
            .claim("nickname", email.split("@".toRegex()).toTypedArray()[0])
            .claim("name", email)
            .claim("picture", "")
            .claim("email_verified", true)
            .signWith(getPrivateKey(), SignatureAlgorithm.ES256).compact()
    }

    private fun getPrivateKey(): PrivateKey? {
        val encoded: ByteArray = byteArrayOf(48, 65, 2, 1, 0, 48, 19, 6, 7, 42, -122, 72, -50, 61, 2, 1, 6, 8, 42, -122, 72, -50, 61, 3, 1, 7, 4, 39, 48, 37, 2, 1, 1, 4, 32, -125, -18, -126, -21, 112, -90, -98, -6, 53, 89, 60, 108, 121, -40, 38, -1, -108, -88, 93, 43, 26, 87, -114, 95, 94, 24, -42, -75, 50, 86, 57, -20)
        val keyFactory: KeyFactory = KeyFactory.getInstance("EC")
        val keySpec = PKCS8EncodedKeySpec(encoded)
        return keyFactory.generatePrivate(keySpec)
    }
}