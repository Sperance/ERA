package com.example.datamodel.services

import com.example.printCallLog
import com.example.respond
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureServices() {
    routing {
        route("/services") {

            get("/all") {
                printCallLog(call)
                call.respond(Services().get(call, ArrayList()))
            }

            get("/{id}") {
                printCallLog(call)
                call.respond(Services().getId(call, ArrayList()))
            }

            post("/update") {
                printCallLog(call)
                call.respond(Services().update(call, ArrayList()))
            }

            post {
                printCallLog(call)
                call.respond(Services().post(call, ArrayList()))
            }

            delete {
                printCallLog(call)
                call.respond(Services().delete(call, ArrayList()))
            }
        }
    }
}
