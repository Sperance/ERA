package com.example.plugins

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

fun Application.configureCORS() {
    install(CORS) {
        allowHost("www.salon-era.ru", schemes = listOf("https"))
        allowHost("salon-era.ru", schemes = listOf("https"))
        allowHost("localhost:3000", schemes = listOf("http"))

        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)

        allowHeader(HttpHeaders.Accept)

        exposeHeader(HttpHeaders.ContentType)
        exposeHeader(HttpHeaders.Authorization)

        allowCredentials = true
    }
}