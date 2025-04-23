package com.example.security

import org.junit.jupiter.api.Test

class AESEncryption_test {

    @Test
    fun generate_key() {
        val secretKey = AESEncryption.generateKey()
        val keyString = AESEncryption.keyToString()
        println("Secret Key: $keyString")
    }

}