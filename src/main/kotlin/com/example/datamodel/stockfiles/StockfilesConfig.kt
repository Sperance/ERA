package com.example.datamodel.stockfiles

import com.example.basemodel.IntBaseDataImpl
import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
import com.example.enums.EnumHttpCode
import com.example.respond
import io.ktor.server.application.Application
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.builtins.ListSerializer

fun Application.configureStockfiles() {
    routing {
        route("/stockfiles") {

            get("/structure") {
                call.respond(ResultResponse.Success(EnumHttpCode.COMPLETED, Stockfiles().getCommentArray()))
            }

            get("/clearTable") {
                Stockfiles.repo_stockfiles.clearTable()
                call.respond(ResultResponse.Success(EnumHttpCode.COMPLETED, "Таблица успешно очищена"))
            }

            get("/all") {
                call.respond(Stockfiles().get(call, RequestParams()))
            }

            get("/all/filter") {
                call.respond(Stockfiles().getFilter(call, RequestParams()))
            }

            post("/update") {
                call.respond(Stockfiles().update(call, RequestParams(), Stockfiles.serializer()))
            }

            post {
                call.respond(Stockfiles().post(call, RequestParams(), ListSerializer(Stockfiles.serializer())))
            }

            delete {
                call.respond(Stockfiles().delete(call, RequestParams()))
            }
        }
    }
}
