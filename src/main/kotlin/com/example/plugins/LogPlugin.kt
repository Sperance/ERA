package com.example.plugins

import com.example.calculateDifference
import com.example.currectDatetime
import com.example.datamodel.create
import com.example.datamodel.routeshistory.RoutesHistory
import com.example.printTextLog
import com.example.toDateTimePossible
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
        val timeRequest = call.attributes.takeOrNull(AttributeKey("Request_Timestamp"))
        if (timeRequest == null) call.attributes.put(AttributeKey("Request_Timestamp"), LocalDateTime.currectDatetime())
        printTextLog("[LogPlugin::onCall] REQUEST ${call.request.local.remoteAddress}::${call.request.local.remotePort}::${call.request.toLogString()} params: ${call.parameters.entries()}")
    }
    onCallRespond { call ->
        val timeRequest = call.attributes.takeOrNull(AttributeKey("Request_Timestamp"))
        printTextLog("[LogPlugin::onCallRespond] RESPOND: ${call.request.local.remoteAddress}::${call.request.local.remotePort}::${call.request.toLogString()} params: ${call.parameters.entries()} timeHeader: $timeRequest")
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
        if (call.request.httpMethod in listOf(HttpMethod.Get, HttpMethod.Post, HttpMethod.Patch, HttpMethod.Delete)) {
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