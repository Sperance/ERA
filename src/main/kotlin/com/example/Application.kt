package com.example

import com.example.datamodel.configureTests
import com.example.helpers.TOML_FILE_NAME
import com.example.logging.DailyLogger.printTextLog
import com.example.plugins.LogPlugin
import com.example.plugins.configureAutoHeadResponse
import com.example.plugins.configureCORS
import com.example.plugins.configureDatabases
import com.example.plugins.configurePartialContent
import com.example.plugins.configureContentNegotiation
import com.example.plugins.configureForwardedHeaders
import com.example.plugins.configureRateLimit
import com.example.plugins.configureStatusPages
import com.example.sockets.configureSockets
import com.example.toml.TomlConfig
import com.example.toml.readTomlFile
import io.ktor.server.application.*
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

    embeddedServer(Netty,
        configure = {
            connector {
                port = 8080
                host = "0.0.0.0"
            }
        },
        module = {
            configureStatusPages()
            install(LogPlugin)
            configureForwardedHeaders()
            configureCORS()
            configureRateLimit()
            configurePartialContent()
            configureContentNegotiation()
            configureAutoHeadResponse()
            configureDatabases()
            configureSockets()
            configureTests()
    }).start(wait = true)
}

