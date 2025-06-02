package com.example.datamodel.clients

import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
import com.example.datamodel.authentications.secureDelete
import com.example.datamodel.authentications.secureGet
import com.example.datamodel.authentications.securePost
import com.example.datamodel.catalogs.Catalogs
import com.example.datamodel.catalogs.CatalogsErrors
import com.example.enums.EnumBearerRoles
import com.example.logObjectProperties
import com.example.respond
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.builtins.ListSerializer

fun Application.configureClients() {
    routing {
        route("/clients") {
//            get("/structure") {
//                call.respond(ResultResponse.Success(Clients().getCommentArray()))
//            }

            get("/clearTable") {
                Clients.repo_clients.clearTable()
                call.respond(ResultResponse.Success("Таблица успешно очищена"))
            }

            get("/errors") {
                call.respond(logObjectProperties(ClientsErrors, Clients()))
            }

            secureGet("/all", EnumBearerRoles.MODERATOR) {
                call.respond(Clients().get(call))
            }

            get("/all/invalid") {
                call.respond(Clients().getInvalid(call))
            }

            secureGet("/all/filter", EnumBearerRoles.USER) {
                call.respond(Clients().getFilter(call))
            }

            secureGet("/id", EnumBearerRoles.USER) {
                call.respond(Clients().getFromId(call, RequestParams()))
            }

            post("/auth") {
                call.respond(Clients().auth(call))
            }

            securePost("/update", EnumBearerRoles.USER) {
                call.respond(Clients().update(call, RequestParams(), Clients.serializer()))
            }

            post {
                call.respond(Clients().post(call, RequestParams(), ListSerializer(Clients.serializer())))
            }

            securePost("/recoveryPassword", EnumBearerRoles.USER) {
                call.respond(Clients().postRecoveryPassword(call))
            }

            securePost("/changePasswordFromEmail", EnumBearerRoles.USER) {
                call.respond(Clients().changePasswordFromEmail(call))
            }

            securePost("/onExit", EnumBearerRoles.USER) {
                call.respond(Clients().onExitSite(call))
            }

            secureDelete("", EnumBearerRoles.USER) {
                call.respond(Clients().delete(call, RequestParams()))
            }
        }
    }
}
