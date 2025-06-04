package com.example.datamodel.clientsschelude

import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
import com.example.datamodel.authentications.secureDelete
import com.example.datamodel.authentications.secureGet
import com.example.datamodel.authentications.securePost
import com.example.enums.EnumBearerRoles
import com.example.logObjectProperties
import com.example.respond
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.builtins.ListSerializer

fun Application.configureClientsSchelude() {
    routing {
        route("/clientsschelude") {

            get("/structure") {
                call.respond(ResultResponse.Success(ClientsSchelude().getCommentArray()))
            }

            get("/clearTable") {
                ClientsSchelude.repo_clientsschelude.clearTable()
                call.respond(ResultResponse.Success("Таблица успешно очищена"))
            }

            get("/errors") {
                call.respond(logObjectProperties(ClientsScheludeErrors, ClientsSchelude()))
            }

            secureGet("/all", EnumBearerRoles.USER) {
                call.respond(ClientsSchelude().get(call))
            }

            secureGet("/all/invalid", EnumBearerRoles.ADMIN) {
                call.respond(ClientsSchelude().getInvalid(call))
            }

            secureGet("/all/filter", EnumBearerRoles.USER) {
                call.respond(ClientsSchelude().getFilter(call))
            }

            securePost("/update", EnumBearerRoles.MODERATOR) {
                call.respond(ClientsSchelude().update(call, RequestParams(), ClientsSchelude.serializer()))
            }

            securePost("", EnumBearerRoles.MODERATOR) {
                call.respond(ClientsSchelude().post(call, RequestParams(), ListSerializer(ClientsSchelude.serializer())))
            }

            securePost("/restore", EnumBearerRoles.MODERATOR) {
                call.respond(ClientsSchelude().restore(call, RequestParams()))
            }

            secureDelete("/safe", EnumBearerRoles.MODERATOR) {
                call.respond(ClientsSchelude().deleteSafe(call, RequestParams()))
            }

            secureDelete("", EnumBearerRoles.MODERATOR) {
                call.respond(ClientsSchelude().delete(call, RequestParams()))
            }
        }
    }
}