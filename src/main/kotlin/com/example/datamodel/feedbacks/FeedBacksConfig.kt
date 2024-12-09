package com.example.datamodel.feedbacks

import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.clearTable
import com.example.datamodel.clients.configureClients
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
                this@configureFeedbacks.printCallLog(call)
                call.respond(ResultResponse.Success(HttpStatusCode.OK, FeedBacks().getCommentArray()))
            }

            get("/clearTable") {
                this@configureFeedbacks.printCallLog(call)
                FeedBacks().clearTable()
                call.respond(ResultResponse.Success(HttpStatusCode.OK, "Таблица успешно очищена"))
            }

            get("/{clientId}") {
                this@configureFeedbacks.printCallLog(call)
                call.respond(FeedBacks().getFromId(call))
            }

            get("/all") {
                this@configureFeedbacks.printCallLog(call)
                call.respond(FeedBacks().get(call, IntBaseDataImpl.RequestParams()))
            }

            get("/{id}") {
                this@configureFeedbacks.printCallLog(call)
                call.respond(FeedBacks().getId(call, IntBaseDataImpl.RequestParams()))
            }

            post("/update") {
                this@configureFeedbacks.printCallLog(call)
                call.respond(FeedBacks().updateFormData(call, IntBaseDataImpl.RequestParams(), FeedBacks.serializer()))
            }

            post {
                this@configureFeedbacks.printCallLog(call)
                call.respond(FeedBacks().postFormData(call, IntBaseDataImpl.RequestParams(), FeedBacks.serializer()))
            }

            delete {
                this@configureFeedbacks.printCallLog(call)
                call.respond(FeedBacks().delete(call, IntBaseDataImpl.RequestParams()))
            }
        }
    }
}