package com.example.plugins.sockets

import com.example.datamodel.catalogs.Catalogs
import com.example.datamodel.catalogs.CatalogsChanged
import com.example.datamodel.records.Records
import com.example.datamodel.records.RecordsChanged
import com.example.logging.DailyLogger.printTextLog
import io.ktor.server.application.Application
import io.ktor.server.request.header
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

val socketsCatalogs = WebSocketConnections("catalogs")

fun Application.catalogsSocket() {
    routing {
        webSocket("/websocket/catalogs") {

            val secKey = call.request.header("Sec-WebSocket-Key")?:"undefined"
            socketsCatalogs.addConnection(secKey, this)

            try {
                val jsonAll = Json.encodeToString(Catalogs.repo_catalogs.getRepositoryData())
                send(Frame.Text(jsonAll))
                while (true) {
                    if (Catalogs.repo_catalogs.onChangedObject.isNotEmpty()) {
                        val array = ArrayList<CatalogsChanged>()
                        Catalogs.repo_catalogs.onChangedObject.forEach { (catalog, s) ->
                            array.add(CatalogsChanged(catalog, s))
                            printTextLog("[catalogsSocket] send data: $catalog TYPE: $s")
                        }
                        val json = Json.encodeToString(array)
                        send(Frame.Text(json))
                        Catalogs.repo_catalogs.onChangedObject.clear()
                    }
                    socketsCatalogs.removeClosedConnections()
                    delay(1000)
                }
            } catch (e: ClosedReceiveChannelException) {
                printTextLog("[catalogsSocket] Connection closed: ${e.message}")
            } catch (e: Exception) {
                printTextLog("[catalogsSocket] Error occurred: ${e.message}")
            } finally {
                socketsCatalogs.removeConnection(secKey)
                close()
                printTextLog("[catalogsSocket] WebSocket connection closed")
            }
        }
    }
}