package com.example.plugins.sockets

import com.example.datamodel.records.Records
import com.example.datamodel.records.RecordsChanged
import com.example.logging.DailyLogger.printTextLog
import io.ktor.server.application.Application
import io.ktor.server.request.header
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

val socketsRecords = WebSocketConnections("records")

fun Application.recordsSocket() {
    routing {
        webSocket("/websocket/records") {

            val secKey = call.request.header("Sec-WebSocket-Key")?:"undefined"
            socketsRecords.addConnection(secKey, this)

            try {
                val jsonAll = Json.encodeToString(Records().getFilledRecords())
                send(Frame.Text(jsonAll))
                while (true) {
                    if (Records.repo_records.onChangedObject.isNotEmpty()) {
                        val array = ArrayList<RecordsChanged>()
                        Records.repo_records.onChangedObject.forEach { (records, s) ->
                            array.add(RecordsChanged(records, s))
                            printTextLog("[recordsSocket] send data: $records TYPE: $s")
                        }
                        val json = Json.encodeToString(array)
                        send(Frame.Text(json))
                        Records.repo_records.onChangedObject.clear()
                    }
                    socketsRecords.removeClosedConnections()
                    delay(1000)
                }
            } catch (e: ClosedReceiveChannelException) {
                printTextLog("[recordsSocket] Connection closed: ${e.message}")
            } catch (e: Exception) {
                printTextLog("[recordsSocket] Error occurred: ${e.message}")
            } finally {
                socketsRecords.removeConnection(secKey)
                close()
                printTextLog("[recordsSocket] WebSocket connection closed")
            }
        }
    }
}