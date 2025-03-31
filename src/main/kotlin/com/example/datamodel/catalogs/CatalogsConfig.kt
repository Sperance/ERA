package com.example.datamodel.catalogs

import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.clients.Clients
import com.example.respond
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.builtins.ListSerializer

fun Application.configureCatalogs() {
    routing {
        route("/catalogs") {

            get("/structure") {
                call.respond(ResultResponse.Success(HttpStatusCode.OK, Catalogs().getCommentArray()))
            }

            get("/clearTable") {
                Catalogs.repo_catalogs.clearTable()
                call.respond(ResultResponse.Success(HttpStatusCode.OK, "Таблица успешно очищена"))
            }

            get("/all") {
                call.respond(Catalogs().get(call, IntBaseDataImpl.RequestParams()))
            }

            get("/{id}") {
                call.respond(Catalogs().getId(call, IntBaseDataImpl.RequestParams()))
            }

            get {
                if (call.parameters["type"] != null) call.respond(Catalogs().getFromType(call))
                else if (call.parameters["category"] != null) call.respond(Catalogs().getFromCategory(call))
                else call.respond(HttpStatusCode.MethodNotAllowed)
            }

            post("/update") {
                call.respond(Catalogs().update(call, IntBaseDataImpl.RequestParams(), Catalogs.serializer()))
            }

            post {
                call.respond(Catalogs().post(call, IntBaseDataImpl.RequestParams(), ListSerializer(Catalogs.serializer())))
            }

            delete {
                call.respond(Catalogs().delete(call, IntBaseDataImpl.RequestParams()))
            }
        }
    }
}