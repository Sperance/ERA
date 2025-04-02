

package com.example

import com.example.datamodel.configureTests
import com.example.plugins.LogPlugin
import com.example.plugins.configureDatabases
import com.example.plugins.configureSerialization
import com.example.schedulers.DailyTaskScheduler
import com.example.sockets.configureSockets
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector

val dailyTaskScheduler = DailyTaskScheduler()

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
            configureSockets()
            configureTests()
        }

        dailyTaskScheduler.start()
    }).start(wait = true)
}

