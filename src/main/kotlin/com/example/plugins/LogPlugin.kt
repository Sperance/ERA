package com.example.plugins

import com.example.currectDatetime
import com.example.isSafeCommand
import com.example.printTextLog
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.logging.toLogString
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.util.AttributeKey
import kotlinx.datetime.LocalDateTime

val LogPlugin = createApplicationPlugin(name = "LogPlugin") {
    onCall { call ->
        val timeRequest = call.attributes.takeOrNull(AttributeKey("Request_Timestamp"))
        if (timeRequest == null) call.attributes.put(AttributeKey("Request_Timestamp"), LocalDateTime.currectDatetime())
        val safeCommand = isSafeCommand(call.request.path())
        printTextLog("[LogPlugin::onCall] ${call.request.local.remoteAddress}::${call.request.local.remotePort}::${call.request.toLogString()} params: ${call.parameters.entries()} [safe:$safeCommand]")

        if (safeCommand != null) {
            call.respond(HttpStatusCode.NotAcceptable, "Unsafe command detected: '$safeCommand'")
            return@onCall
        }
    }
    onCallRespond { call ->
        if (call.response.status() == HttpStatusCode.NotAcceptable) {
            return@onCallRespond
        }
        printTextLog("[LogPlugin::onCallRespond::${call.response.status()}] ${call.request.local.remoteAddress}::${call.request.local.remotePort}::${call.request.toLogString()} params: ${call.parameters.entries()}")
    }
}