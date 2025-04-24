package com.example.plugins

import com.example.logging.DailyLogger.printTextLog
import io.ktor.server.application.*
import org.slf4j.event.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path

fun Application.configureCallLogging() {
    install(CallLogging) {
        level = Level.INFO
        format { call ->
            val str = "Request: ${call.request.local.remoteAddress}::${call.request.httpMethod.value} ${call.request.path()} params: ${call.request.queryParameters.entries().joinToString()} -> Response: ${call.response.status()}"
            printTextLog("[CallLogging] $str", false)
            str
        }
    }
}