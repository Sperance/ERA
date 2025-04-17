package com.example.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.response.respondText

fun Application.configureStatusPages() {
    install(StatusPages) {

        exception<Throwable> { call, status ->
            call.respondText("500 Error: ${status.message}", status = HttpStatusCode.InternalServerError)
        }
        
        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(status, mapOf(
                "code" to HttpStatusCode.NotFound.value,
                "error" to "Not Found",
                "message" to "Указанного запроса '${call.request.httpMethod.value}::${call.request.path()}' не существует. Пожалуйста, проверьте параметры еще раз"
            )
            )
        }

        status(HttpStatusCode.TooManyRequests) { call, status ->
            val xRetry = call.response.headers["Retry-After"]
            call.respond(status, mapOf(
                    "code" to HttpStatusCode.TooManyRequests.value,
                    "error" to "Too Many Requests",
                    "message" to "Слишком много запросов. Попробуйте повторить через $xRetry сек.",
                    "retry-after" to xRetry
                )
            )
        }
    }
}