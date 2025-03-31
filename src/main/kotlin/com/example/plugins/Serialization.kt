package com.example.plugins

import io.ktor.http.ContentType
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
        anyHost()
        allowHost("www.salon-era.ru", schemes = listOf("https"))
        allowHost("salon-era.ru", schemes = listOf("https"))
        allowHost("localhost:3000", schemes = listOf("http"))
        // Разрешить необходимые методы
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
//        allowMethod(HttpMethod.Head)
//        allowMethod(HttpMethod.Options)
//        allowMethod(HttpMethod.Patch)
//        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        // Разрешить необходимые заголовки
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowCredentials = true
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            encodeDefaults = true
        })
        register(ContentType.Text.Html, StringConverter())
    }

    install(PartialContent)
    install(AutoHeadResponse)
}
