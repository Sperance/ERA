package com.example.plugins

import com.example.currectDatetime
import com.example.datamodel.create
import com.example.datamodel.routeshistory.RoutesHistory
import com.example.printTextLog
import io.ktor.http.HttpMethod
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.CallFailed
import io.ktor.server.logging.toLogString
import io.ktor.server.request.header
import io.ktor.server.request.httpMethod
import io.ktor.util.AttributeKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.io.File

val LogPlugin = createApplicationPlugin(name = "LogPlugin") {
    onCall { call ->
        call.attributes.put(AttributeKey("Request_Timestamp"), LocalDateTime.currectDatetime())
        printTextLog("[LogPlugin::onCall] ${call.request.local.remoteAddress}::${call.request.local.remotePort}::${call.request.toLogString()} params: ${call.parameters.entries()}")
    }
    onCallRespond { call ->
        printTextLog("[LogPlugin::onCallRespond] REQUEST: ${call.request.local.remoteAddress}::${call.request.local.remotePort}::${call.request.toLogString()} params: ${call.parameters.entries()}")
        val timeRequest = call.attributes.takeOrNull(AttributeKey("Request_Timestamp"))
        var respondData = ""
        transformBody { data ->
            respondData = when (data) {
                is File -> "[File] name: ${data.name} size: ${data.length()}"
                is Collection<*> -> {
                    if (data.isNotEmpty())
                        "[Collection] size: ${data.size} first: ${data.first()}"
                    else
                        "[Collection] empty collection"
                }

                else -> "[Any] data: $data"
            }
            data
        }
        if (call.request.httpMethod in listOf(HttpMethod.Get, HttpMethod.Post, HttpMethod.Patch, HttpMethod.Delete)) {
            CoroutineScope(Dispatchers.IO).launch {
                val firstHistory = RoutesHistory()
                firstHistory.fillFromCall(call)
                if (timeRequest != null) firstHistory.requestTime = timeRequest as LocalDateTime?
                else firstHistory.requestTime = LocalDateTime.currectDatetime()
                firstHistory.respondData = respondData
                firstHistory.respondTime = LocalDateTime.currectDatetime()
                firstHistory.create(null)
            }
        }
    }
}