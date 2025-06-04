package com.example.datamodel.feedbacks

import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
import com.example.datamodel.authentications.secureDelete
import com.example.datamodel.authentications.secureGet
import com.example.datamodel.authentications.securePost
import com.example.enums.EnumBearerRoles
import com.example.logObjectProperties
import com.example.respond
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.builtins.ListSerializer

fun Application.configureFeedbacks() {
    routing {
        route("/feedbacks") {

            get("/structure") {
                call.respond(ResultResponse.Success(FeedBacks().getCommentArray()))
            }

            get("/clearTable") {
                FeedBacks.repo_feedbacks.clearTable()
                call.respond(ResultResponse.Success("Таблица успешно очищена"))
            }

            get("/errors") {
                call.respond(logObjectProperties(FeedBacksErrors, FeedBacks()))
            }

            get("/all") {
                call.respond(FeedBacks().get(call))
            }

            secureGet("/all/invalid", EnumBearerRoles.ADMIN) {
                call.respond(FeedBacks().getInvalid(call))
            }

            get("/all/filter") {
                call.respond(FeedBacks().getFilter(call))
            }

            securePost("/update", EnumBearerRoles.USER) {
                call.respond(FeedBacks().update(call, RequestParams(), FeedBacks.serializer()))
            }

            securePost("", EnumBearerRoles.USER) {
                call.respond(FeedBacks().post(call, RequestParams(), ListSerializer(FeedBacks.serializer())))
            }

            securePost("/restore", EnumBearerRoles.MODERATOR) {
                call.respond(FeedBacks().restore(call, RequestParams()))
            }

            secureDelete("/safe", EnumBearerRoles.MODERATOR) {
                call.respond(FeedBacks().deleteSafe(call, RequestParams()))
            }

            secureDelete("", EnumBearerRoles.USER) {
                call.respond(FeedBacks().delete(call, RequestParams()))
            }
        }
    }
}