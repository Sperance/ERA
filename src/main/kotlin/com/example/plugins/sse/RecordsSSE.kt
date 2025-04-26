package com.example.plugins.sse

import com.example.datamodel.records.Records
import com.example.logging.DailyLogger.printTextLog
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import io.ktor.server.sse.ServerSSESession
import io.ktor.server.sse.sse
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import java.util.concurrent.CopyOnWriteArrayList

object RecordsSSE {
    private val clients = CopyOnWriteArrayList<ServerSSESession>()

    fun installRouting(application: Application) {
        application.routing {
            sse("sse/records") {
                try {
                    clients += this
                    printTextLog("[SSE::records] Client $this connected. Size: ${clients.size}")
                    Records.repo_records.onChanged.set(true)
                    while (true) {
                        if (Records.repo_records.onChanged.get()) {
                            val data = Records().getFilledRecords()
                            val json = Json.encodeToString(data)
                            send(ServerSentEvent(json))
                            printTextLog("[SSE::records] send data size: ${data.size}")
                            Records.repo_records.onChanged.set(false)
                        }
                        delay(1000)
                    }
                } catch (e: Exception) {
                    printTextLog("[SSE::records] Disconnected session: $this size: ${clients.size}. Error: ${e.message}")
                    clients.remove(this)
                } finally {
                    printTextLog("[SSE::records] Removed session: $this. Size: ${clients.size}")
                    clients.remove(this)
                }
            }
        }
    }
}