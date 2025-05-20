package com.example.datamodel.catalogs

import com.example.basemodel.IntBaseDataImpl
import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
import com.example.datamodel.authentications.secureDelete
import com.example.datamodel.authentications.secureGet
import com.example.datamodel.authentications.securePost
import com.example.enums.EnumBearerRoles
import com.example.enums.EnumHttpCode
import com.example.respond
import io.ktor.server.application.Application
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.builtins.ListSerializer

fun Application.configureCatalogs() {
    routing {
        route("/catalogs") {

            secureGet("/structure", EnumBearerRoles.ADMIN) {
                call.respond(ResultResponse.Success(EnumHttpCode.COMPLETED, Catalogs().getCommentArray()))
            }

            secureGet("/clearTable", EnumBearerRoles.ADMIN) {
                Catalogs.repo_catalogs.clearTable()
                call.respond(ResultResponse.Success(EnumHttpCode.COMPLETED, "Таблица успешно очищена"))
            }

            get("/all") {
                call.respond(Catalogs().get(call, RequestParams()))
            }

            get("/all/filter") {
                call.respond(Catalogs().getFilter(call, RequestParams()))
            }

            securePost("/update", EnumBearerRoles.USER) {
                call.respond(Catalogs().update(call, RequestParams(), Catalogs.serializer()))
            }

            securePost("/addColumn", EnumBearerRoles.MODERATOR) {
                call.respond(Catalogs().addColumn(call))
            }

            securePost("/delColumn", EnumBearerRoles.MODERATOR) {
                call.respond(Catalogs().delColumn(call))
            }

            securePost("", EnumBearerRoles.USER) {
                call.respond(Catalogs().post(call, RequestParams(), ListSerializer(Catalogs.serializer())))
            }

            secureDelete("", EnumBearerRoles.MODERATOR) {
                call.respond(Catalogs().delete(call, RequestParams()))
            }
        }
    }
}