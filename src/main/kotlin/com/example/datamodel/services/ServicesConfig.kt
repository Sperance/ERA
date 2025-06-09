package com.example.datamodel.services

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

fun Application.configureServices() {
    routing {
        route("/services") {

            get("/structure") {
                call.respond(ResultResponse.Success(Services().getCommentArray()))
            }

            get("/clearTable") {
                Services().clearTable()
                call.respond(ResultResponse.Success("Таблица успешно очищена"))
            }

            get("/errors") {
                call.respond(logObjectProperties(ServicesErrors, Services()))
            }

            get("/all") {
                call.respond(Services().get(call))
            }

            secureGet("/all/invalid", EnumBearerRoles.ADMIN) {
                call.respond(Services().getInvalid(call))
            }

            get("/all/filter") {
                call.respond(Services().getFilter(call))
            }

            securePost("/update", EnumBearerRoles.MODERATOR) {
                call.respond(Services().update(call, RequestParams(), Services.serializer()))
            }

            securePost("", EnumBearerRoles.MODERATOR) {
                call.respond(Services().post(call, RequestParams(), ListSerializer(Services.serializer())))
            }

            securePost("/restore", EnumBearerRoles.MODERATOR) {
                call.respond(Services().restore(call, RequestParams()))
            }

            secureDelete("/safe", EnumBearerRoles.MODERATOR) {
                call.respond(Services().deleteSafe(call, RequestParams()))
            }

            secureDelete("", EnumBearerRoles.ADMIN) {
                call.respond(Services().delete(call, RequestParams()))
            }
        }
    }
}
