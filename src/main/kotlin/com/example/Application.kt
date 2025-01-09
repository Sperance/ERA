//package com.example
//
//import com.example.datamodel.configureTests
//import com.example.plugins.*
//import io.ktor.network.tls.certificates.buildKeyStore
//import io.ktor.network.tls.certificates.saveToFile
//import io.ktor.server.application.*
//import io.ktor.server.engine.embeddedServer
//import io.ktor.server.netty.Netty
//import io.ktor.server.netty.NettyApplicationEngine
//import java.io.File
//
//fun main() {
//    embeddedServer(Netty, port = 6533, host = "95.163.84.228") {
//        module()
//    }.start(wait = true)
//}
//
//fun Application.module() {
//    install(LogPlugin)
//    configureSerialization()
//    configureDatabases()
//    configureTests()
//}
//
//private fun NettyApplicationEngine.Configuration.envConfig() {
//    val keyStoreFile = File("keystore.jks")
//    val keyStore = buildKeyStore {
//        certificate("eraAlias") {
//            password = "Password123."
//            domains = listOf("127.0.0.1", "0.0.0.0", "localhost", "95.163.84.228")
//        }
//    }
//    keyStore.saveToFile(keyStoreFile, "Pass123.")
//
//
//}

package com.example

import com.example.datamodel.configureTests
import com.example.plugins.LogPlugin
import com.example.plugins.configureDatabases
import com.example.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.sslConnector
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore

fun main() {
    val keyStoreFile = File("C:/Users/Admin/certificates/new_keystore.p12")
    val keyStorePassword = "keyStore123"

    val keyStore = KeyStore.getInstance("PKCS12").apply {
        FileInputStream(keyStoreFile).use { fis ->
            load(fis, keyStorePassword.toCharArray())
        }
    }

    embeddedServer(Netty, environment = applicationEngineEnvironment {
        connector {
            port = 8080
            host = "95.163.84.228"
        }

        sslConnector(
            keyStore = keyStore,
            keyAlias = "mykey",
            keyStorePassword = { keyStorePassword.toCharArray() },
            privateKeyPassword = { keyStorePassword.toCharArray() }
        ) {
            port = 6533
            host = "95.163.84.228"
        }

        module {
            install(LogPlugin)
            configureSerialization()
            configureDatabases()
            configureTests()
        }
    }).start(wait = true)
}