package com.example.datamodel.news

import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.respond
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.builtins.ListSerializer

fun Application.configureNews() {
    routing {
        route("/news") {

            get("/structure") {
                call.respond(ResultResponse.Success(HttpStatusCode.OK, News().getCommentArray()))
            }

            get("/clearTable") {
                News.repo_news.clearTable()
                call.respond(ResultResponse.Success(HttpStatusCode.OK, "Таблица успешно очищена"))
            }

            get("/all") {
                call.respond(News().get(call, IntBaseDataImpl.RequestParams()))
            }

            get("/{id}") {
                call.respond(News().getId(call, IntBaseDataImpl.RequestParams()))
            }

            post("/update") {
                call.respond(News().update(call, IntBaseDataImpl.RequestParams(), News.serializer()))
            }

            post {
                call.respond(News().post(call, IntBaseDataImpl.RequestParams(), ListSerializer(News.serializer())))
            }

            delete {
                call.respond(News().delete(call, IntBaseDataImpl.RequestParams()))
            }
        }
    }
}
