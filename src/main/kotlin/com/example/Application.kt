package com.example

import com.example.datamodel.configureTests
import com.example.plugins.*
import io.ktor.network.tls.certificates.buildKeyStore
import io.ktor.network.tls.certificates.saveToFile
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import java.io.File

fun main() {
    embeddedServer(Netty, port = 6533, host = "95.163.84.228") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    install(LogPlugin)
    configureSerialization()
    configureDatabases()
    configureTests()
}

private fun NettyApplicationEngine.Configuration.envConfig() {
    val keyStoreFile = File("keystore.jks")
    val keyStore = buildKeyStore {
        certificate("eraAlias") {
            password = "Password123."
            domains = listOf("127.0.0.1", "0.0.0.0", "localhost", "95.163.84.228")
        }
    }
    keyStore.saveToFile(keyStoreFile, "Pass123.")

//    connector {
//        port = 8080
//    }
//    sslConnector(
//        keyStore = keyStore,
//        keyAlias = "sampleAlias",
//        keyStorePassword = { "123456".toCharArray() },
//        privateKeyPassword = { "foobar".toCharArray() }) {
//        port = 8443
//        keyStorePath = keyStoreFile
//    }
}