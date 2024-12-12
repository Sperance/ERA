package com.example.datamodel.records

import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.printCallLog
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
                this@configureRecords.printCallLog(call)
                call.respond(ResultResponse.Success(HttpStatusCode.OK, Records().getCommentArray()))
            }

            get("/clearTable") {
                this@configureRecords.printCallLog(call)
                Records.repo_records.clearTable()
                call.respond(ResultResponse.Success(HttpStatusCode.OK, "Таблица успешно очищена"))
            }

            get("/all") {
                this@configureRecords.printCallLog(call)
                call.respond(Records().get(call, IntBaseDataImpl.RequestParams()))
            }

            get("/{id}") {
                this@configureRecords.printCallLog(call)
                call.respond(Records().getId(call, IntBaseDataImpl.RequestParams()))
            }

            post("/update") {
                this@configureRecords.printCallLog(call)
                call.respond(Records().update(call, IntBaseDataImpl.RequestParams(), Records.serializer()))
            }

            post {
                this@configureRecords.printCallLog(call)
                call.respond(Records().post(call, IntBaseDataImpl.RequestParams(), Records.serializer()))
            }

            delete {
                this@configureRecords.printCallLog(call)
                call.respond(Records().delete(call, IntBaseDataImpl.RequestParams()))
            }
        }
    }
}