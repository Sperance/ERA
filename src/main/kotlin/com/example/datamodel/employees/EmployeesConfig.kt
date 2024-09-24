package com.example.datamodel.employees

import com.example.datamodel.create
import com.example.datamodel.employees.Employees.Companion.tbl_employees
import com.example.datamodel.delete
import com.example.datamodel.getData
import com.example.datamodel.getDataOne
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

fun Application.configureEmployees() {
    routing {
        route("/employees") {
            get("/all") {
                printCallLog(call)
                call.respond(Employees().getData())
            }

            get("/{id}") {
                printCallLog(call)
                val id = call.parameters["id"]
                if (id == null || !id.toIntPossible()) {
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
                    val newUser = call.receive<Employees>().create(null).result
                    call.respond(HttpStatusCode.Created, "Successfully created Employee with id ${newUser.id}")
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
                val findedEmployee = Employees().getDataOne({ tbl_employees.id eq name.toInt() })
                if (findedEmployee == null) {
                    call.respond(HttpStatusCode.NotFound, "Not found Employee with id $name")
                    return@delete
                }
                findedEmployee.delete()
                call.respond(HttpStatusCode.NoContent, "Employee with id $name successfully deleted")
            }
        }
    }
}
