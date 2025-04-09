package com.example.sockets

import com.example.datamodel.catalogs.Catalogs
import com.example.datamodel.records.Records
import com.example.printTextLog
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.logging.toLogString
import io.ktor.server.request.header
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

val connectionSet = mutableSetOf<String>()
val connectionCounter = AtomicInteger(0)

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriodMillis = 15_000
        timeoutMillis = 15_000
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/websocket/records") {

            val secKey = call.request.header("Sec-WebSocket-Key")?:"undefined"
            connectionSet.add(secKey)
            connectionCounter.incrementAndGet()

            val remoteAddress = call.request.local.remoteAddress
            println("WebSocket records connection established: $secKey DATAS: ${call.request.toLogString()} ADDRESS: $remoteAddress")
            println("Total connections: ${connectionCounter.get()} - (${connectionSet.joinToString("; ")})")

            try {
                Records.repo_records.onChanged.set(true)
                while (true) {
                    if (Records.repo_records.onChanged.get()) {
                        val records = Records().getFilledRecords()
                        val json = Json.encodeToString(records)
                        send(Frame.Text(json))
                        Records.repo_records.onChanged.set(false)
                    }
                    delay(1000)
                }
            } catch (e: ClosedReceiveChannelException) {
                println("Connection closed: ${e.message}")
            } catch (e: Exception) {
                println("Error occurred: ${e.message}")
            } finally {
                connectionSet.remove(secKey)
                connectionCounter.decrementAndGet()
                println("WebSocket connection closed")
            }
        }
    }
}