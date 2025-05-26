package com.example.datamodel.clientsschelude

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

fun Application.configureClientsSchelude() {
    routing {
        route("/clientsschelude") {

            get("/structure") {
                call.respond(ResultResponse.Success(EnumHttpCode.COMPLETED, ClientsSchelude().getCommentArray()))
            }

            get("/clearTable") {
                ClientsSchelude.repo_clientsschelude.clearTable()
                call.respond(ResultResponse.Success(EnumHttpCode.COMPLETED, "Таблица успешно очищена"))
            }

            get("/all") {
                call.respond(ClientsSchelude().get(call))
            }

            get("/all/invalid") {
                call.respond(ClientsSchelude().getInvalid(call))
            }

            get("/all/filter") {
                call.respond(ClientsSchelude().getFilter(call))
            }

            post("/update") {
                call.respond(ClientsSchelude().update(call, RequestParams(), ClientsSchelude.serializer()))
            }

            post("/update/many") {
                call.respond(ClientsSchelude().updateMany(call, RequestParams(), ListSerializer(ClientsSchelude.serializer())))
            }

            post {
                call.respond(ClientsSchelude().post(call, RequestParams(), ListSerializer(ClientsSchelude.serializer())))
            }

            delete {
                call.respond(ClientsSchelude().delete(call, RequestParams()))
            }
        }
    }
}