package com.example.datamodel.clients

import com.example.datamodel.create
import com.example.datamodel.clients.Clients.Companion.tbl_clients
import com.example.datamodel.delete
import com.example.datamodel.getData
import com.example.datamodel.getDataOne
import com.example.datamodel.getSize
import com.example.printCallLog
import com.example.toIntPossible
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureClients() {
    routing {
        route("/clients") {
            get {
                printCallLog(call)
                call.respond(Clients().getData())
            }

            get("/{id}") {
                printCallLog(call)
                val id = call.parameters["id"]
                if (id == null || !id.toIntPossible()) {
                    call.respond(HttpStatusCode.BadRequest, "Incorrect parameter 'id'($id). This parameter must be 'Int' type")
                    return@get
                }
                val client = Clients().getDataOne({ tbl_clients.id eq id.toInt()})
                if (client == null) {
                    call.respond(HttpStatusCode.NotFound, "Not found Client with id $id")
                    return@get
                }
                call.respond(client)
            }

            get("/create") {
                printCallLog(call)
                val param1 = call.request.queryParameters["name"]
                val param2 = call.request.queryParameters["password"]
                val param3 = call.request.queryParameters["phone"]

                if (param1 == null) { call.respond(HttpStatusCode.BadRequest, "Not found parameter with name 'name'") ; return@get }
                if (param2 == null) { call.respond(HttpStatusCode.BadRequest, "Not found parameter with name 'password'") ; return@get }

                if (Clients().getSize { tbl_clients.name eq param1; tbl_clients.password eq param2 } != 0L) {
                    call.respond(HttpStatusCode.Conflict, "User with requested 'name' and 'password' already exists")
                    return@get
                }

                val newClient = Clients(name = param1, password = param2, phone = param3?:"").create(null).result
                call.respond(HttpStatusCode.Created, "Client with id ${newClient.id} successfully created")
            }

            post {
                printCallLog(call)
                try {
                    val newUser = call.receive<Clients>().create(null).result
                    call.respond(HttpStatusCode.Created, "Successfully created Client with id ${newUser.id}")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, e.localizedMessage)
                }
            }

            delete {
                printCallLog(call)
                val id = call.parameters["id"]
                if (id == null || !id.toIntPossible()) {
                    call.respond(HttpStatusCode.BadRequest, "Incorrect parameter 'id'($id). This parameter must be 'Int' type")
                    return@delete
                }
                val findedClient = Clients().getDataOne({ tbl_clients.id eq id.toInt() })
                if (findedClient == null) {
                    call.respond(HttpStatusCode.NotFound, "Not found Client with id $id")
                    return@delete
                }
                findedClient.delete()
                call.respond(HttpStatusCode.NoContent, "Client with id $id successfully deleted")
            }
        }
    }
}
