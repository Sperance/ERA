package com.example.plugins

import com.example.converters.StringConverter
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            encodeDefaults = true
        })
        register(ContentType.Text.Html, StringConverter())
    }
}
