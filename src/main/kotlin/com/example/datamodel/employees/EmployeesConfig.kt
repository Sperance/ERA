package com.example.datamodel.employees

import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
import com.example.datamodel.authentications.secureDelete
import com.example.datamodel.authentications.secureGet
import com.example.datamodel.authentications.securePost
import com.example.enums.EnumBearerRoles
import com.example.helpers.clearTable
import com.example.logObjectProperties
import com.example.respond
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.builtins.ListSerializer

fun Application.configureEmployees() {
    routing {
        route("/employees") {
            get("/structure") {
                call.respond(ResultResponse.Success(Employees().getCommentArray()))
            }

            get("/clearTable") {
                Employees().clearTable()
                call.respond(ResultResponse.Success("Таблица успешно очищена"))
            }

            secureGet("/timeslot/{clientId}/{servceLength}", EnumBearerRoles.USER) {
                call.respond(Employees().getTimeSlots(call))
            }

            get("/errors") {
                call.respond(logObjectProperties(EmployeesErrors, Employees()))
            }

            get("/all") {
                call.respond(Employees().get(call))
            }

            secureGet("/all/invalid", EnumBearerRoles.ADMIN) {
                call.respond(Employees().getInvalid(call))
            }

            get("/all/filter") {
                call.respond(Employees().getFilter(call))
            }

            secureGet("/id", EnumBearerRoles.MODERATOR) {
                call.respond(Employees().getFromId(call, RequestParams()))
            }

            secureGet("/slots/{id}/{data}", EnumBearerRoles.USER) {
                call.respond(Employees().getSlots(call))
            }

            post("/auth") {
                call.respond(Employees().auth(call))
            }

            securePost("/update", EnumBearerRoles.USER) {
                call.respond(Employees().update(call, RequestParams(), Employees.serializer()))
            }

            securePost("", EnumBearerRoles.MODERATOR) {
                call.respond(Employees().post(call, RequestParams(), ListSerializer(Employees.serializer())))
            }

            securePost("/onExit", EnumBearerRoles.USER) {
                call.respond(Employees().onExitSite(call))
            }

            securePost("/restore", EnumBearerRoles.MODERATOR) {
                call.respond(Employees().restore(call, RequestParams()))
            }

            secureDelete("/safe", EnumBearerRoles.USER) {
                call.respond(Employees().deleteSafe(call, RequestParams()))
            }

            secureDelete("", EnumBearerRoles.MODERATOR) {
                call.respond(Employees().delete(call, RequestParams()))
            }
        }
    }
}