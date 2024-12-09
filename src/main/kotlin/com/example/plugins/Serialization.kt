package com.example.plugins

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.partialcontent.PartialContent
import kotlinx.serialization.json.Json

fun Application.configureSerialization() {

    install(CORS) {
        // Разрешить все источники (не рекомендуется для продакшена)
        anyHost()
        // Разрешить только конкретный источник
        allowHost("localhost:3000")
        // Разрешить необходимые методы
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Head)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        // Разрешить необходимые заголовки
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        // Если нужно разрешить все заголовки, можно оставить:
        // allowHeaders { true }
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            encodeDefaults = true
        })
    }

    install(PartialContent)
    install(AutoHeadResponse)
}
