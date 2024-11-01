package com.example.datamodel.feedbacks

import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.clearTable
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

fun Application.configureFeedbacks() {
    routing {
        route("/feedbacks") {

            get("/structure") {
                printCallLog(call)
                call.respond(ResultResponse.Success(HttpStatusCode.OK, FeedBacks().getCommentArray()))
            }

            get("/clearTable") {
                printCallLog(call)
                FeedBacks().clearTable()
                call.respond(ResultResponse.Success(HttpStatusCode.OK, "Таблица успешно очищена"))
            }

            get("/{clientId}") {
                printCallLog(call)
                call.respond(FeedBacks().getFromId(call))
            }

            get("/all") {
                printCallLog(call)
                call.respond(FeedBacks().get(call, IntBaseDataImpl.RequestParams()))
            }

            get("/{id}") {
                printCallLog(call)
                call.respond(FeedBacks().getId(call, IntBaseDataImpl.RequestParams()))
            }

            post("/update") {
                printCallLog(call)
                call.respond(FeedBacks().update(call, IntBaseDataImpl.RequestParams()))
            }

            post {
                printCallLog(call)
                call.respond(FeedBacks().post(call, IntBaseDataImpl.RequestParams()))
            }

            delete {
                printCallLog(call)
                call.respond(FeedBacks().delete(call, IntBaseDataImpl.RequestParams()))
            }
        }
    }
}