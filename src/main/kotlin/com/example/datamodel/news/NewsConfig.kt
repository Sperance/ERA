package com.example.datamodel.news

import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.printCallLog
import com.example.respond
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureNews() {
    routing {
        route("/news") {

            get("/structure") {
                this@configureNews.printCallLog(call)
                call.respond(ResultResponse.Success(HttpStatusCode.OK, News().getCommentArray()))
            }

            get("/clearTable") {
                this@configureNews.printCallLog(call)
                News.repo_news.clearTable()
                call.respond(ResultResponse.Success(HttpStatusCode.OK, "Таблица успешно очищена"))
            }

            get("/all") {
                this@configureNews.printCallLog(call)
                call.respond(News().get(call, IntBaseDataImpl.RequestParams()))
            }

            get("/{id}") {
                this@configureNews.printCallLog(call)
                call.respond(News().getId(call, IntBaseDataImpl.RequestParams()))
            }

            post("/update") {
                this@configureNews.printCallLog(call)
                call.respond(News().update(call, IntBaseDataImpl.RequestParams(), News.serializer()))
            }

            post {
                this@configureNews.printCallLog(call)
                call.respond(News().post(call, IntBaseDataImpl.RequestParams(), News.serializer()))
            }

            delete {
                this@configureNews.printCallLog(call)
                call.respond(News().delete(call, IntBaseDataImpl.RequestParams()))
            }
        }
    }
}
