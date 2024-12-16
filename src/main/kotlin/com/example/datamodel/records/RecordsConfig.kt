package com.example.datamodel.records

import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.respond
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRecords() {
    routing {
        route("/records") {

            get("/structure") {
                call.respond(ResultResponse.Success(HttpStatusCode.OK, Records().getCommentArray()))
            }

            get("/clearTable") {
                Records.repo_records.clearTable()
                call.respond(ResultResponse.Success(HttpStatusCode.OK, "Таблица успешно очищена"))
            }

            get("/all") {
                call.respond(Records().get(call, IntBaseDataImpl.RequestParams()))
            }

            get("/{id}") {
                call.respond(Records().getId(call, IntBaseDataImpl.RequestParams()))
            }

            post("/update") {
                call.respond(Records().update(call, IntBaseDataImpl.RequestParams(), Records.serializer()))
            }

            post {
                call.respond(Records().post(call, IntBaseDataImpl.RequestParams(), Records.serializer()))
            }

            delete {
                call.respond(Records().delete(call, IntBaseDataImpl.RequestParams()))
            }
        }
    }
}