package com.example.datamodel.services

import com.example.datamodel.create
import com.example.datamodel.delete
import com.example.datamodel.getData
import com.example.datamodel.getDataOne
import com.example.datamodel.services.Services.Companion.tbl_services
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

fun Application.configureServices() {
    routing {
        route("/services") {
            get("/all") {
                printCallLog(call)
                call.respond(Services().getData())
            }

            get("/{id}") {
                printCallLog(call)
                val id = call.parameters["id"]
                if (id == null || !id.toIntPossible()) {
                    call.respond(HttpStatusCode.BadRequest, "Incorrect parameter 'id'($id). This parameter must be 'Int' type")
                    return@get
                }
                val service = Services().getDataOne({tbl_services.id eq id.toInt()}, null)
                if (service == null) {
                    call.respond(HttpStatusCode.NotFound, "Not found Service with id $id")
                    return@get
                }
                call.respond(service)
            }

            post {
                printCallLog(call)
                try {
                    val newService = call.receive<Services>()
                    if (newService.name.isBlank()) {
                        call.respond(HttpStatusCode(430, "name must be initialized"), "Необходимо указать Наименование"); return@post
                    }
                    if (newService.price < 0.1) {
                        call.respond(HttpStatusCode(431, "price must be initialized"), "Необходимо указать Стоимость"); return@post
                    }
                    if (newService.category.isBlank()) {
                        call.respond(HttpStatusCode(432, "category must be initialized"), "Необходимо указать Категорию"); return@post
                    }
                    if (newService.duration == 0.toByte()) {
                        call.respond(HttpStatusCode(433, "duration must be initialized"), "Необходимо указать Продолжительность (1пункт = 15минут, 60мин = 4пункта)"); return@post
                    }
                    val finish = newService.create(null).result
                    call.respond(HttpStatusCode.Created, "Successfully created Service with id ${finish.id}")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, e.localizedMessage)
                }
            }

            delete {
                printCallLog(call)
                val name = call.parameters["id"]
                if (name == null || !name.toIntPossible()) {
                    call.respond(HttpStatusCode.BadRequest, "Incorrect parameter 'id'($name). This parameter must be 'Int' type")
                    return@delete
                }
                val findedService = Services().getDataOne({ tbl_services.id eq name.toInt() })
                if (findedService == null) {
                    call.respond(HttpStatusCode.NotFound, "Not found Service with id $name")
                    return@delete
                }
                findedService.delete()
                call.respond(HttpStatusCode.NoContent, "Service with id $name successfully deleted")
            }
        }
    }
}
