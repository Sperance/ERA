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

            get("/all") {
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

            post {
                printCallLog(call)
                try {
                    val newUser = call.receive<Clients>()
                    if (newUser.firstName.isBlank()) {
                        call.respond(HttpStatusCode(433, "firstName must be selected"), "Необходимо указать Имя пользователя"); return@post
                    }
                    if (newUser.lastName.isBlank()) {
                        call.respond(HttpStatusCode(434, "lastName must be selected"), "Необходимо указать Фамилию пользователя"); return@post
                    }
                    if (newUser.phone.isBlank()) {
                        call.respond(HttpStatusCode(435, "phone must be selected"), "Необходимо указать Телефон пользователя"); return@post
                    }
                    if (newUser.login.isBlank()) {
                        call.respond(HttpStatusCode(436, "login must be selected"), "Необходимо указать Логин пользователя"); return@post
                    }
                    if (newUser.gender == (-1).toByte()) {
                        call.respond(HttpStatusCode(437, "gender must be selected"), "Необходимо указать Гендер пользователя"); return@post
                    }
                    if (newUser.isDuplicate { tbl_clients.login eq newUser.login }) {
                        call.respond(HttpStatusCode(431, "Login already exists"), "Клиент с логином ${newUser.login} уже существует"); return@post
                    }
                    if (newUser.isDuplicate { tbl_clients.phone eq newUser.phone }) {
                        call.respond(HttpStatusCode(432, "Phone already exists"), "Клиент с номером телефона ${newUser.phone} уже существует"); return@post
                    }
                    val created = newUser.create(null).result
                    call.respond(HttpStatusCode.Created, "Successfully created Client with id ${created.id}")
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
