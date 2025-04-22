package com.example.sockets

import com.example.applicationTomlSettings
import com.example.datamodel.records.Records
import com.example.logging.DailyLogger.printTextLog
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.request.header
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

val socketsRecords = WebSocketConnections()

fun Application.configureSockets() {

    if (!applicationTomlSettings!!.SETTINGS.WEB_SOCKET) {
        printTextLog("[applicationTomlSettings] WEB_SOCKET is Disabled")
        return
    } else {
        printTextLog("[applicationTomlSettings] WEB_SOCKET is Active")
    }

    install(WebSockets) {
        pingPeriod = 10.seconds
        timeout = 10.seconds
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
                close()
                println("WebSocket connection closed")
            }
        }
    }
}