package com.example.datamodel.authentications

import com.example.basemodel.ResultResponse
import com.example.enums.EnumBearerRoles
import com.example.getRouteAttributes
import com.example.helpers.clearTable
import com.example.respond
import io.ktor.server.application.Application
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.util.putAll

fun Application.configureAuthentications() {
    routing {
        route("/authentications") {
            get("/clearTable") {
                Authentications().clearTable()
                call.respond(ResultResponse.Success("Таблица успешно очищена"))
            }.apply {
                attributes.putAll(getRouteAttributes(title = "Очистка таблицы Authentications в PostgreSQL"))
            }

            secureGet("/me",
                EnumBearerRoles.USER,
                title = "Получить текущий токен авторизации (под которым авторизовался пользователь)"
            ) {
                call.respond(Authentications().getByUser(call, it))
            }

            secureDelete("",
                EnumBearerRoles.ADMIN,
                title = "Удаление объекта из Базы данных по его id"
            ) {
                call.respond(Authentications().deleteById(call))
            }
        }
    }
}