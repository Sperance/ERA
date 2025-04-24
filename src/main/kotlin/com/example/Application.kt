package com.example

import com.example.datamodel.configureTests
import com.example.helpers.ENCRYPT_KEY
import com.example.helpers.TOML_FILE_NAME
import com.example.logging.DailyLogger.printTextLog
import com.example.plugins.configureCORS
import com.example.plugins.configureCallLogging
import com.example.plugins.configureDatabases
import com.example.plugins.configureContentNegotiation
import com.example.plugins.configureRateLimit
import com.example.plugins.configureStatusPages
import com.example.security.AESEncryption
import com.example.sockets.configureSockets
import com.example.toml.TomlConfig
import com.example.toml.readTomlFile
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.engine.connector

var applicationTomlSettings: TomlConfig? = null

fun main() {

    applicationTomlSettings = readTomlFile()
    if (applicationTomlSettings == null) {
        printTextLog("[applicationTomlSettings] Error: dont find correct file settings: $TOML_FILE_NAME")
        return
    }

    val checkDatabase = applicationTomlSettings!!.checkForCorrect()
    if (checkDatabase != null) {
        printTextLog("[applicationTomlSettings] Error: empty field in file $TOML_FILE_NAME: $checkDatabase")
        return
    }

    AESEncryption.setKeyFromString(ENCRYPT_KEY)

    embeddedServer(Netty,
        configure = {
            connector {
                port = 8080
                host = "0.0.0.0"
            }
        },
        module = {
            configureCallLogging()
            configureStatusPages()
            configureCORS()
            configureRateLimit()
            configureContentNegotiation()
            configureDatabases()
            configureSockets()
            configureTests()
    }).start(wait = true)
}

