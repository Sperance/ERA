package com.example.datamodel.employees

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

fun Application.configureEmployees() {
    routing {
        route("/employees") {
            get("/structure") {
                call.respond(ResultResponse.Success(EnumHttpCode.COMPLETED, Employees().getCommentArray()))
            }

            get("/clearTable") {
                Employees.repo_employees.clearTable()
                call.respond(ResultResponse.Success(EnumHttpCode.COMPLETED, "Таблица успешно очищена"))
            }

            get ("/timeslot/{clientId}/{servceLength}") {
                call.respond(Employees().getTimeSlots(call))
            }

            get("/all") {
                call.respond(Employees().get(call, RequestParams()))
            }

            get("/all/filter") {
                call.respond(Employees().getFilter(call, RequestParams()))
            }

            get("/id") {
                call.respond(Employees().getFromId(call, RequestParams()))
            }

            get("/slots/{id}/{data}") {
                call.respond(Employees().getSlots(call))
            }

            post("/auth") {
                call.respond(Employees().auth(call))
            }

            post("/update") {
                call.respond(Employees().update(call, RequestParams(), Employees.serializer()))
            }

            post {
                call.respond(Employees().post(call, RequestParams(), ListSerializer(Employees.serializer())))
            }

            delete {
                call.respond(Employees().delete(call, RequestParams()))
            }
        }
    }
}