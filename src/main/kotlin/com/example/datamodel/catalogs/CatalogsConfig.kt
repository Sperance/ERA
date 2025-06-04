package com.example.datamodel.catalogs

import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
import com.example.datamodel.authentications.secureDelete
import com.example.datamodel.authentications.secureGet
import com.example.datamodel.authentications.securePost
import com.example.enums.EnumBearerRoles
import com.example.helpers.getDataPagination
import com.example.helpers.restoreSafe
import com.example.logObjectProperties
import com.example.respond
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.builtins.ListSerializer

fun Application.configureCatalogs() {
    routing {
        route("/catalogs") {

            secureGet("/structure", EnumBearerRoles.ADMIN) {
                call.respond(ResultResponse.Success(Catalogs().getCommentArray()))
            }

            get("/clearTable") {
                Catalogs.repo_catalogs.clearTable()
                call.respond(ResultResponse.Success("Таблица успешно очищена"))
            }

            get("/errors") {
                call.respond(logObjectProperties(CatalogsErrors, Catalogs()))
            }

            get("/all") {
                call.respond(Catalogs().get(call))
            }

            secureGet("/all/invalid", EnumBearerRoles.ADMIN) {
                call.respond(Catalogs().getInvalid(call))
            }

            get("/all/filter") {
                call.respond(Catalogs().getFilter(call))
            }

            get("/all/pages/filter") {
                call.respond(Catalogs().getDataFilterPagination(call))
            }

            securePost("/update", EnumBearerRoles.MODERATOR) {
                call.respond(Catalogs().update(call, RequestParams(), Catalogs.serializer()))
            }

            securePost("", EnumBearerRoles.MODERATOR) {
                call.respond(Catalogs().post(call, RequestParams(), ListSerializer(Catalogs.serializer())))
            }

            securePost("/restore", EnumBearerRoles.MODERATOR) {
                call.respond(Catalogs().restore(call, RequestParams()))
            }

            secureDelete("/safe", EnumBearerRoles.MODERATOR) {
                call.respond(Catalogs().deleteSafe(call, RequestParams()))
            }

            secureDelete("", EnumBearerRoles.MODERATOR) {
                call.respond(Catalogs().delete(call, RequestParams()))
            }
        }
    }
}