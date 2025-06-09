package com.example.datamodel.news

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

fun Application.configureNews() {
    routing {
        route("/news") {

            get("/structure") {
                call.respond(ResultResponse.Success(News().getCommentArray()))
            }

            get("/clearTable") {
                News().clearTable()
                call.respond(ResultResponse.Success("Таблица успешно очищена"))
            }

            get("/errors") {
                call.respond(logObjectProperties(NewsErrors, News()))
            }

            get("/all") {
                call.respond(News().get(call))
            }

            secureGet("/all/invalid", EnumBearerRoles.ADMIN) {
                call.respond(News().getInvalid(call))
            }

            get("/all/filter") {
                call.respond(News().getFilter(call))
            }

            securePost("/update", EnumBearerRoles.MODERATOR) {
                call.respond(News().update(call, RequestParams(), News.serializer()))
            }

            securePost("", EnumBearerRoles.MODERATOR) {
                call.respond(News().post(call, RequestParams(), ListSerializer(News.serializer())))
            }

            securePost("/restore", EnumBearerRoles.MODERATOR) {
                call.respond(News().restore(call, RequestParams()))
            }

            secureDelete("/safe", EnumBearerRoles.MODERATOR) {
                call.respond(News().deleteSafe(call, RequestParams()))
            }

            secureDelete("", EnumBearerRoles.MODERATOR) {
                call.respond(News().delete(call, RequestParams()))
            }
        }
    }
}
