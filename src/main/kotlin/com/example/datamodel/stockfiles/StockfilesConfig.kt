package com.example.datamodel.stockfiles

import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.respond
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureStockfiles() {
    routing {
        route("/stockfiles") {

            get("/structure") {
                call.respond(ResultResponse.Success(HttpStatusCode.OK, Stockfiles().getCommentArray()))
            }

            get("/clearTable") {
                Stockfiles.repo_stockfiles.clearTable()
                call.respond(ResultResponse.Success(HttpStatusCode.OK, "Таблица успешно очищена"))
            }

            get("/all") {
                call.respond(Stockfiles().get(call, IntBaseDataImpl.RequestParams()))
            }

            get("/{id}") {
                call.respond(Stockfiles().getId(call, IntBaseDataImpl.RequestParams()))
            }

            post("/update") {
                call.respond(Stockfiles().update(call, IntBaseDataImpl.RequestParams(), Stockfiles.serializer()))
            }

            post {
                call.respond(Stockfiles().post(call, IntBaseDataImpl.RequestParams(), Stockfiles.serializer()))
            }

            delete {
                call.respond(Stockfiles().delete(call, IntBaseDataImpl.RequestParams()))
            }
        }
    }
}
