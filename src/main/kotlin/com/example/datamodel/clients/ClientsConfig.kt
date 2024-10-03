package com.example.datamodel.clients

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
                call.respond(Clients().get(call, ArrayList()))
            }

            get("/{id}") {
                printCallLog(call)
                call.respond(Clients().getId(call, ArrayList()))
            }

            post("/auth") {
                printCallLog(call)
                call.respond(Clients().auth(call))
            }

            post("/update") {
                printCallLog(call)
                call.respond(Clients().update(call, ArrayList()))
            }

            post {
                printCallLog(call)
                call.respond(Clients().post(call, ArrayList()))
            }

            delete {
                printCallLog(call)
                call.respond(Clients().delete(call, ArrayList()))
            }
        }
    }
}
