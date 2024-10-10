package com.example.datamodel.clients

import com.example.datamodel.IntBaseDataImpl
import com.example.printCallLog
import com.example.respond
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.http.content.staticFiles
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import java.io.File

fun Application.configureClients() {
    routing {

        staticFiles("/files/clients", File("files/clients"))

        route("/clients") {

            get("/all") {
                printCallLog(call)
                call.respond(Clients().get(call, IntBaseDataImpl.RequestParams()))
            }

            get("/all/{clientType}") {
                printCallLog(call)
                call.respond(Clients().getFromType(call))
            }

            get("/{id}") {
                printCallLog(call)
                call.respond(Clients().getId(call, IntBaseDataImpl.RequestParams()))
            }

            post("/auth") {
                printCallLog(call)
                call.respond(Clients().auth(call))
            }

            post("/update") {
                printCallLog(call)
                call.respond(Clients().update(call, IntBaseDataImpl.RequestParams()))
            }

            post {
                printCallLog(call)
                call.respond(Clients().post(call, IntBaseDataImpl.RequestParams()))
            }

            delete {
                printCallLog(call)
                call.respond(Clients().delete(call, IntBaseDataImpl.RequestParams()))
            }
        }
    }
}
