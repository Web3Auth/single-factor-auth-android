package com.web3auth.singlefactorauth.utils

import org.bouncycastle.util.io.pem.PemReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException
import java.io.Reader
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.EncodedKeySpec
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

object PemUtils {
    @Throws(IOException::class)
    private fun parsePEMFile(pemFile: File): ByteArray {
        if (!pemFile.isFile || !pemFile.exists()) {
            throw FileNotFoundException(
                String.format(
                    "The file '%s' doesn't exist.",
                    pemFile.absolutePath
                )
            )
        }
        val reader = PemReader(FileReader(pemFile))
        val pemObject = reader.readPemObject()
        val content = pemObject.content
        reader.close()
        return content
    }

    private fun getPublicKey(keyBytes: ByteArray, algorithm: String): PublicKey? {
        var publicKey: PublicKey? = null
        try {
            val kf = KeyFactory.getInstance(algorithm)
            val keySpec: EncodedKeySpec = X509EncodedKeySpec(keyBytes)
            publicKey = kf.generatePublic(keySpec)
        } catch (e: NoSuchAlgorithmException) {
            println("Could not reconstruct the public key, the given algorithm could not be found.")
        } catch (e: InvalidKeySpecException) {
            println("Could not reconstruct the public key")
        }
        return publicKey
    }

    private fun getPrivateKey(keyBytes: ByteArray, algorithm: String): PrivateKey? {
        var privateKey: PrivateKey? = null
        try {
            val kf = KeyFactory.getInstance(algorithm)
            val keySpec: EncodedKeySpec = PKCS8EncodedKeySpec(keyBytes)
            privateKey = kf.generatePrivate(keySpec)
        } catch (e: NoSuchAlgorithmException) {
            println("Could not reconstruct the private key, the given algorithm could not be found.")
        } catch (e: InvalidKeySpecException) {
            println("Could not reconstruct the private key")
        }
        return privateKey
    }

    @Throws(IOException::class)
    fun readPublicKeyFromFile(filepath: String, algorithm: String): PublicKey? {
        val bytes = parsePEMFile(File(filepath))
        return getPublicKey(bytes, algorithm)
    }

    @Throws(IOException::class)
    fun readPrivateKeyFromReader(reader: Reader, algorithm: String): PrivateKey? {
        val pemReader = PemReader(reader)
        val pemObject = pemReader.readPemObject()
        val content = pemObject.content
        pemReader.close()
        return getPrivateKey(content, algorithm)
    }

    @Throws(IOException::class)
    fun readPublicKeyFromReader(reader: Reader, algorithm: String): PublicKey? {
        val pemReader = PemReader(reader)
        val pemObject = pemReader.readPemObject()
        val content = pemObject.content
        pemReader.close()
        return getPublicKey(content, algorithm)
    }
}