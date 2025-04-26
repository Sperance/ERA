package com.example.datamodel.services

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

fun Application.configureServices() {
    routing {
        route("/services") {

            get("/structure") {
                call.respond(ResultResponse.Success(EnumHttpCode.COMPLETED, Services().getCommentArray()))
            }

            get("/clearTable") {
                Services.repo_services.clearTable()
                call.respond(ResultResponse.Success(EnumHttpCode.COMPLETED, "Таблица успешно очищена"))
            }

            get("/all") {
                call.respond(Services().get(call, RequestParams()))
            }

            get("/all/filter") {
                call.respond(Services().getFilter(call, RequestParams()))
            }

            post("/update") {
                call.respond(Services().update(call, RequestParams(), Services.serializer()))
            }

            post {
                call.respond(Services().post(call, RequestParams(), ListSerializer(Services.serializer())))
            }

            delete {
                call.respond(Services().delete(call, RequestParams()))
            }
        }
    }
}
