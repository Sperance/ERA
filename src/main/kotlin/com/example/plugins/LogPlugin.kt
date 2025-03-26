package com.example.plugins

import com.example.calculateDifference
import com.example.currectDatetime
import com.example.datamodel.create
import com.example.datamodel.routeshistory.RoutesHistory
import com.example.isSafeCommand
import com.example.printTextLog
import com.example.toDateTimePossible
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.logging.toLogString
import io.ktor.server.request.header
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.util.AttributeKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import java.io.File

val LogPlugin = createApplicationPlugin(name = "LogPlugin") {
    onCall { call ->
        val timeRequest = call.attributes.takeOrNull(AttributeKey("Request_Timestamp"))
        val EraPost = call.request.header("EraPost")
        if (timeRequest == null) call.attributes.put(AttributeKey("Request_Timestamp"), LocalDateTime.currectDatetime())
        val safeCommand = isSafeCommand(call.request.path())
        printTextLog("[LogPlugin::onCall] ${call.request.local.remoteAddress}::${call.request.local.remotePort}::${call.request.toLogString()} params: ${call.parameters.entries()} [EraPost:$EraPost][safeCommand:$safeCommand]")

        if (safeCommand != null) {
            call.respond(HttpStatusCode.NotAcceptable, "Unsafe command detected: '$safeCommand'")
            return@onCall
        }
    }
    onCallRespond { call ->
        if (call.response.status() == HttpStatusCode.NotAcceptable) {
            return@onCallRespond
        }
        val timeRequest = call.attributes.takeOrNull(AttributeKey("Request_Timestamp"))
        printTextLog("[LogPlugin::onCallRespond::${call.response.status()}] ${call.request.local.remoteAddress}::${call.request.local.remotePort}::${call.request.toLogString()} params: ${call.parameters.entries()}")
        var respondData = ""
        transformBody { data ->
            respondData = when (data) {
                is File -> "[File] name: ${data.name} size: ${data.length()}"
                is Collection<*> -> {
                    if (data.isNotEmpty()) "[Collection] size: ${data.size} first: ${data.first()}"
                    else "[Collection] empty collection"
                }
                else -> "[Any] data: $data"
            }
            data
        }
        if (!respondData.contains("404 Not Found") && call.request.httpMethod in listOf(HttpMethod.Get, HttpMethod.Post, HttpMethod.Patch, HttpMethod.Delete)) {
            CoroutineScope(Dispatchers.IO).launch {
                val firstHistory = RoutesHistory()
                firstHistory.fillFromCall(call)
                if (timeRequest != null && timeRequest.toString().toDateTimePossible()) firstHistory.requestTime = timeRequest as LocalDateTime?
                else firstHistory.requestTime = LocalDateTime.currectDatetime()
                firstHistory.clientTime = firstHistory.requestTime
                if (respondData.length > 500) firstHistory.respondData = respondData.substring(0, 495)
                else firstHistory.respondData = respondData
                firstHistory.respondTime = LocalDateTime.currectDatetime()
                firstHistory.timeDifference = calculateDifference(firstHistory.clientTime, firstHistory.respondTime)
                firstHistory.create(null)
            }
        }
    }
}