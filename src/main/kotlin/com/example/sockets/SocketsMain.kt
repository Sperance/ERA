package com.example.sockets

import com.example.datamodel.catalogs.Catalogs
import com.example.datamodel.records.Records
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriodMillis = 15_000
        timeoutMillis = 15_000
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/websocket/records") {
            println("WebSocket records connection established")

            try {
                while (true) {
                    val records = Records.repo_records.getRepositoryData()
                    val json = Json.encodeToString(records)
                    send(Frame.Text(json))
                    delay(1000)
                }
            } catch (e: ClosedReceiveChannelException) {
                println("Connection closed: ${e.message}")
            } catch (e: Exception) {
                println("Error occurred: ${e.message}")
            } finally {
                println("WebSocket connection closed")
            }
        }
        webSocket("/websocket/catalogs") {
            println("WebSocket catalogs connection established")

            try {
                while (true) {
                    val records = Catalogs.repo_catalogs.getRepositoryData()
                    val json = Json.encodeToString(records)
                    send(Frame.Text(json))
                    delay(1000)
                }
            } catch (e: ClosedReceiveChannelException) {
                println("Connection closed: ${e.message}")
            } catch (e: Exception) {
                println("Error occurred: ${e.message}")
            } finally {
                println("WebSocket connection closed")
            }
        }
    }
}