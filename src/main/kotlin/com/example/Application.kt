

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

fun main() {
    embeddedServer(Netty, environment = applicationEngineEnvironment {
        connector {
            port = 8080
            host = "0.0.0.0"
        }

        module {
            configureSerialization()
            install(LogPlugin)
            configureDatabases()
            configureTests()
        }
    }).start(wait = true)
}

