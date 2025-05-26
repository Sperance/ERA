package com.example.datamodel.news

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

fun Application.configureNews() {
    routing {
        route("/news") {

            get("/structure") {
                call.respond(ResultResponse.Success(EnumHttpCode.COMPLETED, News().getCommentArray()))
            }

            get("/clearTable") {
                News.repo_news.clearTable()
                call.respond(ResultResponse.Success(EnumHttpCode.COMPLETED, "Таблица успешно очищена"))
            }

            get("/all") {
                call.respond(News().get(call))
            }

            get("/all/invalid") {
                call.respond(News().getInvalid(call))
            }

            get("/all/filter") {
                call.respond(News().getFilter(call))
            }

            post("/update") {
                call.respond(News().update(call, RequestParams(), News.serializer()))
            }

            post {
                call.respond(News().post(call, RequestParams(), ListSerializer(News.serializer())))
            }

            delete {
                call.respond(News().delete(call, RequestParams()))
            }
        }
    }
}
