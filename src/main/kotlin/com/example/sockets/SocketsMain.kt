package com.example.sockets

import com.example.datamodel.records.Records
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.logging.toLogString
import io.ktor.server.request.header
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.utils.io.CancellationException
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicInteger

val socketsRecords = WebSocketConnections()

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
            socketsRecords.addConnection(secKey, this)

            try {
                Records.repo_records.onChanged.set(true)
                while (true) {
                    if (Records.repo_records.onChanged.get()) {
                        val records = Records().getFilledRecords()
                        val json = Json.encodeToString(records)
                        send(Frame.Text(json))
                        Records.repo_records.onChanged.set(false)
                    }
                    socketsRecords.removeClosedConnections()
                    delay(1000)
                }
            } catch (e: ClosedReceiveChannelException) {
                println("Connection closed: ${e.message}")
            } catch (e: Exception) {
                println("Error occurred: ${e.message}")
            } finally {
                socketsRecords.removeConnection(secKey)
                println("WebSocket connection closed")
            }
        }
    }
}