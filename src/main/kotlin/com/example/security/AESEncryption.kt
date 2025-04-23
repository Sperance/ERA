package com.example.security

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

object AESEncryption {

    private const val ALGORITHM = "AES"
    private var secretKey: SecretKey? = null

    // Метод для генерации нового секретного ключа
    fun generateKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance(ALGORITHM)
        keyGen.init(256) // Размер ключа 256 бит
        secretKey = keyGen.generateKey()
        return secretKey!!
    }

    // Метод для установки ключа из строки
    fun setKeyFromString(keyString: String) {
        val decodedKey = Base64.getDecoder().decode(keyString)
        secretKey = SecretKeySpec(decodedKey, 0, decodedKey.size, ALGORITHM)
    }

    // Метод для шифрования строки
    fun encrypt(plainText: String?): String {
        if (plainText == null) return "null"
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    // Метод для дешифрования строки
    fun decrypt(cipherText: String?): String {
        if (cipherText == null) return "null"
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(cipherText))
        return String(decryptedBytes, Charsets.UTF_8)
    }

    // Метод для преобразования ключа в строку (для передачи другому клиенту)
    fun keyToString(): String {
        return Base64.getEncoder().encodeToString(secretKey!!.encoded)
    }
}