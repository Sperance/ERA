package com.example.plugins

import com.example.isSafeCommand
import com.example.printTextLog
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.logging.toLogString
import io.ktor.server.request.httpVersion
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.netty.handler.codec.http.HttpVersion

val LogPlugin = createApplicationPlugin(name = "LogPlugin") {
    onCall { call ->
        if (call.request.httpVersion.contains("0.9")) {
            val textCommand = "HTTP version 0.9 not supported"
            printTextLog("[LogPlugin::onCall] ${call.request.local.remoteAddress}::${call.request.local.remotePort}::${call.request.toLogString()} params: ${call.parameters.entries()} - $textCommand")
            call.respond(HttpStatusCode.NotAcceptable, textCommand)
            return@onCall
        }

        val safeCommand = isSafeCommand(call.request.path())
        if (safeCommand != null) {
            val textCommand = "Unsafe command: '$safeCommand'"
            printTextLog("[LogPlugin::onCall] ${call.request.local.remoteAddress}::${call.request.local.remotePort}::${call.request.toLogString()} params: ${call.parameters.entries()} - $textCommand")
            call.respond(HttpStatusCode.NotAcceptable, textCommand)
            return@onCall
        }

        printTextLog("[LogPlugin::onCall] ${call.request.local.remoteAddress}::${call.request.local.remotePort}::${call.request.toLogString()} params: ${call.parameters.entries()}")
    }
}