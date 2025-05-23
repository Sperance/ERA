package com.example.datamodel.records

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

fun Application.configureRecords() {
    routing {
        route("/records") {

            get("/structure") {
                call.respond(ResultResponse.Success(EnumHttpCode.COMPLETED, Records().getCommentArray()))
            }

            get("/clearTable") {
                Records.repo_records.clearTable()
                call.respond(ResultResponse.Success(EnumHttpCode.COMPLETED, "Таблица успешно очищена"))
            }

            get("/all") {
                call.respond(Records().get(call, RequestParams()))
            }

            get("/all/filter") {
                call.respond(Records().getFilter(call, RequestParams()))
            }

            get("/id") {
                call.respond(Records().getFromId(call, RequestParams()))
            }

            post("/update") {
                call.respond(Records().update(call, RequestParams(), Records.serializer()))
            }

            post {
                call.respond(Records().post(call, RequestParams(), ListSerializer(Records.serializer())))
            }

            delete {
                call.respond(Records().delete(call, RequestParams()))
            }
        }
    }
}