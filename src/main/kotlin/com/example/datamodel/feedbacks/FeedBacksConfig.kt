package com.example.datamodel.feedbacks

import com.example.basemodel.IntBaseDataImpl
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

fun Application.configureFeedbacks() {
    routing {
        route("/feedbacks") {

            get("/structure") {
                call.respond(ResultResponse.Success(EnumHttpCode.COMPLETED, FeedBacks().getCommentArray()))
            }

            get("/clearTable") {
                FeedBacks.repo_feedbacks.clearTable()
                call.respond(ResultResponse.Success(EnumHttpCode.COMPLETED, "Таблица успешно очищена"))
            }

            get("/all") {
                call.respond(FeedBacks().get(call))
            }

            get("/all/invalid") {
                call.respond(FeedBacks().getInvalid(call))
            }

            get("/all/filter") {
                call.respond(FeedBacks().getFilter(call))
            }

            post("/update") {
                call.respond(FeedBacks().update(call, RequestParams(), FeedBacks.serializer()))
            }

            post {
                call.respond(FeedBacks().post(call, RequestParams(), ListSerializer(FeedBacks.serializer())))
            }

            delete {
                call.respond(FeedBacks().delete(call, RequestParams()))
            }
        }
    }
}