package com.example.datamodel.stockfiles

import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
import com.example.datamodel.authentications.secureDelete
import com.example.datamodel.authentications.secureGet
import com.example.datamodel.authentications.securePost
import com.example.enums.EnumBearerRoles
import com.example.helpers.clearTable
import com.example.logObjectProperties
import com.example.respond
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.builtins.ListSerializer

fun Application.configureStockfiles() {
    routing {
        route("/stockfiles") {

            get("/structure") {
                call.respond(ResultResponse.Success(Stockfiles().getCommentArray()))
            }

            get("/clearTable") {
                Stockfiles().clearTable()
                call.respond(ResultResponse.Success("Таблица успешно очищена"))
            }

            get("/errors") {
                call.respond(logObjectProperties(StockfilesErrors, Stockfiles()))
            }

            get("/all") {
                call.respond(Stockfiles().get(call))
            }

            secureGet("/all/invalid", EnumBearerRoles.ADMIN) {
                call.respond(Stockfiles().getInvalid(call))
            }

            get("/all/filter") {
                call.respond(Stockfiles().getFilter(call))
            }

            securePost("/update", EnumBearerRoles.MODERATOR) {
                call.respond(Stockfiles().update(call, RequestParams(), Stockfiles.serializer()))
            }

            securePost("", EnumBearerRoles.MODERATOR) {
                call.respond(Stockfiles().post(call, RequestParams(), ListSerializer(Stockfiles.serializer())))
            }

            securePost("/restore", EnumBearerRoles.MODERATOR) {
                call.respond(Stockfiles().restore(call, RequestParams()))
            }

            secureDelete("/safe", EnumBearerRoles.MODERATOR) {
                call.respond(Stockfiles().deleteSafe(call, RequestParams()))
            }

            secureDelete("", EnumBearerRoles.MODERATOR) {
                call.respond(Stockfiles().delete(call, RequestParams()))
            }
        }
    }
}
