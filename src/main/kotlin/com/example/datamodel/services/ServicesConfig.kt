package com.example.datamodel.services

import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.clearTable
import com.example.datamodel.clients.Clients
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

fun Application.configureServices() {
    routing {
        route("/services") {

            get("/structure") {
                printCallLog(call)
                call.respond(ResultResponse.Success(HttpStatusCode.OK, Services().getCommentArray()))
            }

            get("/clearTable") {
                printCallLog(call)
                Services().clearTable()
                call.respond(ResultResponse.Success(HttpStatusCode.OK, "Таблица успешно очищена"))
            }

            get("/all") {
                printCallLog(call)
                call.respond(Services().get(call, IntBaseDataImpl.RequestParams()))
            }

            get("/{id}") {
                printCallLog(call)
                call.respond(Services().getId(call, IntBaseDataImpl.RequestParams()))
            }

            post("/update") {
                printCallLog(call)
                call.respond(Services().update(call, IntBaseDataImpl.RequestParams()))
            }

            post {
                printCallLog(call)
                call.respond(Services().post(call, IntBaseDataImpl.RequestParams()))
            }

            delete {
                printCallLog(call)
                call.respond(Services().delete(call, IntBaseDataImpl.RequestParams()))
            }
        }
    }
}
