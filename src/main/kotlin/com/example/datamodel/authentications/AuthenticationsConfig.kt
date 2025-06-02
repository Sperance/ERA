package com.example.datamodel.authentications

import com.example.basemodel.ResultResponse
import com.example.enums.EnumBearerRoles
import com.example.helpers.clearTable
import com.example.respond
import io.ktor.server.application.Application
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureAuthentications() {
    routing {
        route("/authentications") {
            get("/clearTable") {
                Authentications().clearTable()
                call.respond(ResultResponse.Success("Таблица успешно очищена"))
            }

            secureGet("/user", EnumBearerRoles.ADMIN) {
                call.respond(Authentications().getByUser(call))
            }

            secureDelete("", EnumBearerRoles.ADMIN) {
                call.respond(Authentications().deleteById(call))
            }
        }
    }
}