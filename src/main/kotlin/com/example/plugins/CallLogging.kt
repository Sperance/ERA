package com.example.plugins

import com.example.currectDatetime
import com.example.datamodel.serverrequests.ServerRequests
import com.example.logging.DailyLogger.printTextLog
import io.ktor.server.application.*
import org.slf4j.event.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import kotlinx.datetime.LocalDateTime
import java.util.UUID

fun Application.configureCallLogging() {
    install(CallLogging) {
        level = Level.TRACE
        filter { call ->
            call.response.headers["ERA-key"] != null
        }
        format { call ->
            val str = "Request: ${call.request.local.remoteAddress}::${call.request.httpMethod.value} ${call.request.path()} params: [${call.request.queryParameters.entries().joinToString()}] -> Response: ${call.response.status()}"
            printTextLog("[CallLogging] $str", false)

            if (call.response.headers["Answer-TimeStamp"] != null) ServerRequests.addServerRecord(call)
            str
        }
    }

    intercept(ApplicationCallPipeline.Call) {
        try {
            call.response.headers.append("Request-TimeStamp", LocalDateTime.currectDatetime().toString())
            call.response.headers.append("ERA-key", UUID.randomUUID().toString().replace("-", ""))
        }catch (_: Exception) { }
    }
}