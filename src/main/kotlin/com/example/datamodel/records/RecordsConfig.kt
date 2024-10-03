package com.example.datamodel.records

import com.example.datamodel.feedbacks.FeedBacks
import com.example.printCallLog
import com.example.respond
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRecords() {
    routing {
        route("/records") {
            get("/all") {
                printCallLog(call)
                call.respond(Records().get(call, ArrayList()))
            }

            get("/{id}") {
                printCallLog(call)
                call.respond(Records().getId(call, ArrayList()))
            }

            post("/update") {
                printCallLog(call)
                call.respond(Records().update(call, ArrayList()))
            }

            post {
                printCallLog(call)
                call.respond(Records().post(call, ArrayList()))
            }

            delete {
                printCallLog(call)
                call.respond(Records().delete(call, ArrayList()))
            }
        }
    }
}