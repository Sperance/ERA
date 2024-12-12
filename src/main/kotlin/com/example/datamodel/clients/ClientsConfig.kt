package com.example.datamodel.clients

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

fun Application.configureClients() {
    routing {
        route("/clients") {
            get("/structure") {
                this@configureClients.printCallLog(call)
                call.respond(ResultResponse.Success(HttpStatusCode.OK, Clients().getCommentArray()))
            }

            get("/clearTable") {
                this@configureClients.printCallLog(call)
                Clients.repo_clients.clearTable()
                call.respond(ResultResponse.Success(HttpStatusCode.OK, "Таблица успешно очищена"))
            }

            get ("/timeslot/{clientId}/{servceLength}") {
                this@configureClients.printCallLog(call)
                call.respond(Clients().getTimeSlots(call))
            }

            get("/all") {
                this@configureClients.printCallLog(call)
                call.respond(Clients().get(call, IntBaseDataImpl.RequestParams()))
            }

            get("/all/{clientType}") {
                this@configureClients.printCallLog(call)
                call.respond(Clients().getFromType(call))
            }

            get("/{id}") {
                this@configureClients.printCallLog(call)
                call.respond(Clients().getId(call, IntBaseDataImpl.RequestParams()))
            }

            get("/slots/{id}/{data}") {
                this@configureClients.printCallLog(call)
                call.respond(Clients().getSlots(call))
            }

            post("/auth") {
                this@configureClients.printCallLog(call)
                call.respond(Clients().auth(call))
            }

            post("/update") {
                this@configureClients.printCallLog(call)
                call.respond(Clients().update(call, IntBaseDataImpl.RequestParams(), Clients.serializer()))
            }

            post {
                this@configureClients.printCallLog(call)
                call.respond(Clients().post(call, IntBaseDataImpl.RequestParams(), Clients.serializer()))
            }

            post("/recoveryPassword") {
                this@configureClients.printCallLog(call)
                call.respond(Clients().postRecoveryPassword(call))
            }

            post("/changePasswordFromEmail") {
                this@configureClients.printCallLog(call)
                call.respond(Clients().changePasswordFromEmail(call))
            }

            delete {
                this@configureClients.printCallLog(call)
                call.respond(Clients().delete(call, IntBaseDataImpl.RequestParams()))
            }
        }
    }
}
