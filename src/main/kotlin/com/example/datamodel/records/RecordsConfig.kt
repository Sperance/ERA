package com.example.datamodel.records

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

fun Application.configureRecords() {
    routing {
        route("/records") {

            get("/structure") {
                call.respond(ResultResponse.Success(Records().getCommentArray()))
            }

            get("/clearTable") {
                Records().clearTable()
                call.respond(ResultResponse.Success("Таблица успешно очищена"))
            }

            get("/errors") {
                call.respond(logObjectProperties(RecordsErrors, Records()))
            }

            secureGet("/all", EnumBearerRoles.USER) {
                call.respond(Records().get(call))
            }

            secureGet("/all/invalid", EnumBearerRoles.ADMIN) {
                call.respond(Records().getInvalid(call))
            }

            secureGet("/all/filter", EnumBearerRoles.USER) {
                call.respond(Records().getFilter(call))
            }

            secureGet("/id", EnumBearerRoles.USER) {
                call.respond(Records().getFromId(call, RequestParams()))
            }

            securePost("/update", EnumBearerRoles.USER) {
                call.respond(Records().update(call, RequestParams(), Records.serializer()))
            }

            securePost("", EnumBearerRoles.USER) {
                call.respond(Records().post(call, RequestParams(), ListSerializer(Records.serializer())))
            }

            securePost("/restore", EnumBearerRoles.MODERATOR) {
                call.respond(Records().restore(call, RequestParams()))
            }

            secureDelete("/safe", EnumBearerRoles.MODERATOR) {
                call.respond(Records().deleteSafe(call, RequestParams()))
            }

            secureDelete("", EnumBearerRoles.MODERATOR) {
                call.respond(Records().delete(call, RequestParams()))
            }
        }
    }
}