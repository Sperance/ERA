package com.example.datamodel.catalogs

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

fun Application.configureCatalogs() {
    routing {
        route("/catalogs") {

            get("/structure") {
                call.respond(ResultResponse.Success(EnumHttpCode.COMPLETED, Catalogs().getCommentArray()))
            }

            get("/clearTable") {
                Catalogs.repo_catalogs.clearTable()
                call.respond(ResultResponse.Success(EnumHttpCode.COMPLETED, "Таблица успешно очищена"))
            }

            get("/all") {
                call.respond(Catalogs().get(call, RequestParams()))
            }

            get("/all/filter") {
                call.respond(Catalogs().getFilter(call, RequestParams()))
            }

            post("/update") {
                call.respond(Catalogs().update(call, RequestParams(), Catalogs.serializer()))
            }

            post("/addColumn") {
                call.respond(Catalogs().addColumn(call))
            }

            post("/delColumn") {
                call.respond(Catalogs().delColumn(call))
            }

            post {
                call.respond(Catalogs().post(call, RequestParams(), ListSerializer(Catalogs.serializer())))
            }

            delete {
                call.respond(Catalogs().delete(call, RequestParams()))
            }
        }
    }
}