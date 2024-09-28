package com.example.datamodel.employees

import com.example.printCallLog
import com.example.respond
import io.ktor.server.application.Application
import io.ktor.server.application.call
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
                call.respond(Employees().get(call))
            }

            get("/{id}") {
                printCallLog(call)
                call.respond(Employees().getId(call))
            }

            post("/update") {
                printCallLog(call)
                call.respond(Employees().update(call, EmployeesNullable::class))
            }

            post {
                printCallLog(call)
                call.respond(Employees().post(call))
            }

            delete {
                printCallLog(call)
                call.respond(Employees().delete(call))
            }
        }
    }
}
