package com.example.datamodel.services

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
import kotlinx.serialization.builtins.ListSerializer

fun Application.configureServices() {
    routing {
        route("/services") {

            get("/structure") {
                call.respond(ResultResponse.Success(HttpStatusCode.OK, Services().getCommentArray()))
            }

            get("/clearTable") {
                Services.repo_services.clearTable()
                call.respond(ResultResponse.Success(HttpStatusCode.OK, "Таблица успешно очищена"))
            }

            get("/all") {
                call.respond(Services().get(call, IntBaseDataImpl.RequestParams()))
            }

            get("/{id}") {
                call.respond(Services().getId(call, IntBaseDataImpl.RequestParams()))
            }

            post("/update") {
                call.respond(Services().update(call, IntBaseDataImpl.RequestParams(), Services.serializer()))
            }

            post {
                call.respond(Services().post(call, IntBaseDataImpl.RequestParams(), ListSerializer(Services.serializer())))
            }

            delete {
                call.respond(Services().delete(call, IntBaseDataImpl.RequestParams()))
            }
        }
    }
}
