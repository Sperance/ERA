package com.example

import com.example.datamodel.configureTests
import com.example.plugins.LogPlugin
import com.example.plugins.configureAutoHeadResponse
import com.example.plugins.configureCORS
import com.example.plugins.configureDatabases
import com.example.plugins.configurePartialContent
import com.example.plugins.configureContentNegotiation
import com.example.plugins.configureRateLimit
import com.example.plugins.configureSessions
import com.example.plugins.configureStatusPages
import com.example.schedulers.DailyTaskScheduler
import com.example.schedulers.HoursTaskScheduler
import com.example.schedulers.configureSchedulers
import com.example.sockets.configureSockets
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
            configureStatusPages()
            install(LogPlugin)

            configureCORS()
            configureRateLimit()
            configurePartialContent()
            configureContentNegotiation()
            configureAutoHeadResponse()
            configureDatabases()
            configureSockets()
            configureTests()
        }
    }).start(wait = true)
}

