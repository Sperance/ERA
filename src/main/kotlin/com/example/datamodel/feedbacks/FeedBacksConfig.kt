package com.example.datamodel.feedbacks

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

fun Application.configureFeedbacks() {
    routing {
        route("/feedbacks") {

            get("/structure") {
                call.respond(ResultResponse.Success(HttpStatusCode.OK, FeedBacks().getCommentArray()))
            }

            get("/clearTable") {
                FeedBacks.repo_feedbacks.clearTable()
                call.respond(ResultResponse.Success(HttpStatusCode.OK, "Таблица успешно очищена"))
            }

            get("/{clientId}") {
                call.respond(FeedBacks().getFromId(call))
            }

            get("/all") {
                call.respond(FeedBacks().get(call, IntBaseDataImpl.RequestParams()))
            }

            get("/{id}") {
                call.respond(FeedBacks().getId(call, IntBaseDataImpl.RequestParams()))
            }

            post("/update") {
                call.respond(FeedBacks().update(call, IntBaseDataImpl.RequestParams(), FeedBacks.serializer()))
            }

            post {
                call.respond(FeedBacks().post(call, IntBaseDataImpl.RequestParams(), FeedBacks.serializer()))
            }

            delete {
                call.respond(FeedBacks().delete(call, IntBaseDataImpl.RequestParams()))
            }
        }
    }
}