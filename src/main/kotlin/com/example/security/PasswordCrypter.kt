package com.example.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

fun verifyPassword(storedHash: String?, storedSalt: String?, providedPassword: String?): Boolean {
    if (storedHash == null || storedSalt == null || providedPassword == null) return false
    val providedHash = hashWithSalt(providedPassword, storedSalt)
    return storedHash == providedHash
}

fun hashString(password: String, salt: String): String {
    return hashWithSalt(password, salt)
}

fun generateSalt(): String {
    val random = SecureRandom()
    val salt = ByteArray(16)
    random.nextBytes(salt)
    return Base64.getEncoder().encodeToString(salt)
}

fun hashWithSalt(password: String, salt: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val saltBytes = Base64.getDecoder().decode(salt)
    md.update(saltBytes)
    val hashedPassword = md.digest(password.toByteArray())
    return Base64.getEncoder().encodeToString(hashedPassword)
}