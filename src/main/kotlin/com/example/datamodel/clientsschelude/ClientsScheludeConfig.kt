package com.example.datamodel.clientsschelude

import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.catalogs.Catalogs
import com.example.respond
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respond
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
                call.respond(ResultResponse.Success(HttpStatusCode.OK, ClientsSchelude().getCommentArray()))
            }

            get("/clearTable") {
                ClientsSchelude.repo_clientsschelude.clearTable()
                call.respond(ResultResponse.Success(HttpStatusCode.OK, "Таблица успешно очищена"))
            }

            get("/all") {
                call.respond(ClientsSchelude().get(call, IntBaseDataImpl.RequestParams()))
            }

            get("/all/filter") {
                call.respond(ClientsSchelude().getFilter(call, IntBaseDataImpl.RequestParams()))
            }

            post("/update") {
                call.respond(ClientsSchelude().update(call, IntBaseDataImpl.RequestParams(), ClientsSchelude.serializer()))
            }

            get {
                if (call.parameters["idClient"] != null) call.respond(ClientsSchelude().getFromClient(call))
                call.respond(HttpStatusCode.MethodNotAllowed)
            }

            post {
                call.respond(ClientsSchelude().post(call, IntBaseDataImpl.RequestParams(), ListSerializer(ClientsSchelude.serializer())))
            }

            delete {
                call.respond(ClientsSchelude().delete(call, IntBaseDataImpl.RequestParams()))
            }
        }
    }
}