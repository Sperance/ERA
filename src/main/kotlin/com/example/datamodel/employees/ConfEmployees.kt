package com.example.datamodel.employees

import com.example.datamodel.employees.Employees.Companion.tbl_employees
import com.example.getData
import com.example.getDataOne
import com.example.printCallLog
import com.example.toIntPossible
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.logging.toLogString
import io.ktor.server.request.httpMethod
import io.ktor.server.request.receive
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureEmployees() {
    routing {
        route("/employees") {
            get {
                printCallLog(call)
                val tasks = Employees().getData()
                call.respond(tasks)
            }

            get("/{id}") {
                printCallLog(call)
                val id = call.parameters["id"]
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Not found parameter 'id'")
                    return@get
                }
                if (!id.toIntPossible()) {
                    call.respond(HttpStatusCode.BadRequest, "Incorrect parameter 'id'($id). This parameter must be 'Int' type")
                    return@get
                }
                val user = Employees().getDataOne({tbl_employees.id eq id.toInt()})
                if (user == null) {
                    call.respond(HttpStatusCode.NotFound, "Not found Employee with id $id")
                    return@get
                }
                call.respond(user)
            }

            post {
                printCallLog(call)
                try {
                    val task = call.receive<Employees>()
                    val newUser = task.create()
                    call.respond(HttpStatusCode.Created, "Successfully created Employee with id ${newUser.id}")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, e.localizedMessage)
                }
            }

            delete {
                printCallLog(call)
                val name = call.parameters["id"]
                if (name == null) {
                    //Проверка на отсутствие параметра id в запросе
                    call.respond(HttpStatusCode.BadRequest, "Not found parameter 'id'")
                    return@delete
                }
                if (!name.toIntPossible()) {
                    //Проверка на тип параметра (должен быть Integer)
                    call.respond(HttpStatusCode.BadRequest, "Incorrect parameter 'id'($name). This parameter must be 'Int' type")
                    return@delete
                }
                val findedEmployee = Employees().getDataOne({ tbl_employees.id eq name.toInt() })
                if (findedEmployee == null) {
                    //Проверка пользователя на наличие в базе
                    call.respond(HttpStatusCode.NotFound, "Not found Employee with id $name")
                    return@delete
                }
                findedEmployee.delete()
                call.respond(HttpStatusCode.NoContent, "Employee with id $name successfully deleted")
            }
        }
    }
}
