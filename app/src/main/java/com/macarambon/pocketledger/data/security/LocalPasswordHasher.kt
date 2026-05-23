package com.macarambon.pocketledger.data.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

object LocalPasswordHasher {

    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val input = "$salt:$password".toByteArray(Charsets.UTF_8)
        return Base64.encodeToString(digest.digest(input), Base64.NO_WRAP)
    }

    fun verify(password: String, salt: String, expectedHash: String): Boolean =
        hash(password, salt) == expectedHash
}
