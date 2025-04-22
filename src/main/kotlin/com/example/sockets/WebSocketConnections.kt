package com.example.sockets

import com.example.logging.DailyLogger.printTextLog
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class WebSocketConnections {
    private val connections = ConcurrentHashMap<String, WebSocketSession>()

    fun addConnection(key: String, session: WebSocketSession) {
        connections[key] = session
        printConnectionsStatus("add")
    }

    fun removeClosedConnections() {
        val filterKeysForClosing = connections.filter { !it.value.isActive }.map { it.key }
        filterKeysForClosing.forEach { key ->
            removeConnection(key)
        }
    }

    fun removeConnection(key: String) {
        connections.remove(key)
        printConnectionsStatus("rem")
    }

    fun broadcast(message: String) {
        connections.values.forEach { session ->
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    session.send(Frame.Text(message))
                }
            } catch (e: Exception) {
                println("Error sending message to connection: ${e.message}")
                // Можно добавить обработку ошибок отправки
            }
        }
    }

    private fun printConnectionsStatus(type: String) {
        printTextLog("Total active connections [$type]: ${connections.size} - Keys: ${connections.keys.joinToString(", ")}")
    }
}