package com.example.datamodel.clients

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

fun Application.configureClients() {
    routing {
        route("/clients") {
            get("/structure") {
                call.respond(ResultResponse.Success(EnumHttpCode.COMPLETED, Clients().getCommentArray()))
            }

            get("/clearTable") {
                Clients.repo_clients.clearTable()
                call.respond(ResultResponse.Success(EnumHttpCode.COMPLETED, "Таблица успешно очищена"))
            }

            get("/all") {
                call.respond(Clients().get(call))
            }

            get("/all/invalid") {
                call.respond(Clients().getInvalid(call))
            }

            get("/all/filter") {
                call.respond(Clients().getFilter(call))
            }

            get("/id") {
                call.respond(Clients().getFromId(call, RequestParams()))
            }

            post("/auth") {
                call.respond(Clients().auth(call))
            }

            post("/update") {
                call.respond(Clients().update(call, RequestParams(), Clients.serializer()))
            }

            post {
                call.respond(Clients().post(call, RequestParams(), ListSerializer(Clients.serializer())))
            }

            post("/recoveryPassword") {
                call.respond(Clients().postRecoveryPassword(call))
            }

            post("/changePasswordFromEmail") {
                call.respond(Clients().changePasswordFromEmail(call))
            }

            delete {
                call.respond(Clients().delete(call, RequestParams()))
            }
        }
    }
}
