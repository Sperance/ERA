package com.example.datamodel.records

import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.getCommentFieldAnnotation
import com.example.printCallLog
import com.example.respond
import io.ktor.http.HttpStatusCode
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

            get("/structure") {
                printCallLog(call)
                call.respond(ResultResponse.Success(HttpStatusCode.OK, Records().getCommentArray()))
            }

            get("/all") {
                printCallLog(call)
                call.respond(Records().get(call, IntBaseDataImpl.RequestParams()))
            }

            get("/{id}") {
                printCallLog(call)
                call.respond(Records().getId(call, IntBaseDataImpl.RequestParams()))
            }

            post("/update") {
                printCallLog(call)
                call.respond(Records().update(call, IntBaseDataImpl.RequestParams()))
            }

            post {
                printCallLog(call)
                call.respond(Records().post(call, IntBaseDataImpl.RequestParams()))
            }

            delete {
                printCallLog(call)
                call.respond(Records().delete(call, IntBaseDataImpl.RequestParams()))
            }
        }
    }
}