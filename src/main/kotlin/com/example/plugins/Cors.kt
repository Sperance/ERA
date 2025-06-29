package com.example.plugins

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.forwardedheaders.ForwardedHeaders
import io.ktor.server.plugins.forwardedheaders.XForwardedHeaders

fun Application.configureCORS() {
    install(XForwardedHeaders)
    install(ForwardedHeaders)
    install(CORS) {
        allowHost("www.salon-era.ru", schemes = listOf("https"))
        allowHost("salon-era.ru", schemes = listOf("https"))
        allowHost("www.salon-era.online", schemes = listOf("https"))
        allowHost("salon-era.online", schemes = listOf("https"))
        allowHost("localhost:3000", schemes = listOf("http"))

        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.AccessControlRequestHeaders)
        allowHeader(HttpHeaders.AccessControlRequestMethod)

        allowHeader("X-User-IP")
        allowHeader("X-User-Lat")
        allowHeader("X-User-Lon")
        allowHeader("X-User-Geo")

        exposeHeader(HttpHeaders.ContentType)
        exposeHeader(HttpHeaders.Authorization)

        allowCredentials = true
    }
}