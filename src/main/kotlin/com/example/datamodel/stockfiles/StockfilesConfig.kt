package com.example.datamodel.stockfiles

import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.clearTable
import com.example.datamodel.services.configureServices
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

fun Application.configureStockfiles() {
    routing {
        route("/stockfiles") {

            get("/structure") {
                this@configureStockfiles.printCallLog(call)
                call.respond(ResultResponse.Success(HttpStatusCode.OK, Stockfiles().getCommentArray()))
            }

            get("/clearTable") {
                this@configureStockfiles.printCallLog(call)
                Stockfiles().clearTable()
                call.respond(ResultResponse.Success(HttpStatusCode.OK, "Таблица успешно очищена"))
            }

            get("/all") {
                this@configureStockfiles.printCallLog(call)
                call.respond(Stockfiles().get(call, IntBaseDataImpl.RequestParams()))
            }

            get("/{id}") {
                this@configureStockfiles.printCallLog(call)
                call.respond(Stockfiles().getId(call, IntBaseDataImpl.RequestParams()))
            }

            post("/update") {
                this@configureStockfiles.printCallLog(call)
                call.respond(Stockfiles().updateFormData(call, IntBaseDataImpl.RequestParams(), Stockfiles.serializer()))
            }

            post {
                this@configureStockfiles.printCallLog(call)
                call.respond(Stockfiles().postFormData(call, IntBaseDataImpl.RequestParams(), Stockfiles.serializer()))
            }

            delete {
                this@configureStockfiles.printCallLog(call)
                call.respond(Stockfiles().delete(call, IntBaseDataImpl.RequestParams()))
            }
        }
    }
}
