package com.example.datamodel.clients

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

fun Application.configureClients() {
    routing {
        route("/clients") {
            get("/structure") {
                call.respond(ResultResponse.Success(HttpStatusCode.OK, Clients().getCommentArray()))
            }

            get("/clearTable") {
                Clients.repo_clients.clearTable()
                call.respond(ResultResponse.Success(HttpStatusCode.OK, "Таблица успешно очищена"))
            }

            get ("/timeslot/{clientId}/{servceLength}") {
                call.respond(Clients().getTimeSlots(call))
            }

            get("/all") {
                call.respond(Clients().get(call, IntBaseDataImpl.RequestParams()))
            }

            get("/all/{clientType}") {
                call.respond(Clients().getFromType(call))
            }

            get("/{id}") {
                call.respond(Clients().getId(call, IntBaseDataImpl.RequestParams()))
            }

            get("/slots/{id}/{data}") {
                call.respond(Clients().getSlots(call))
            }

            post("/auth") {
                call.respond(Clients().auth(call))
            }

            post("/update") {
                call.respond(Clients().update(call, IntBaseDataImpl.RequestParams(), Clients.serializer()))
            }

            post {
                call.respond(Clients().post(call, IntBaseDataImpl.RequestParams(), Clients.serializer()))
            }

            post("/recoveryPassword") {
                call.respond(Clients().postRecoveryPassword(call))
            }

            post("/changePasswordFromEmail") {
                call.respond(Clients().changePasswordFromEmail(call))
            }

            delete {
                call.respond(Clients().delete(call, IntBaseDataImpl.RequestParams()))
            }
        }
    }
}
