package com.example.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.sessions.SessionStorageMemory
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import io.ktor.server.sessions.maxAge
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Duration.Companion.hours

@Serializable
data class UserSession(val id: String, val count: Int)

fun Application.configureSessions() {
    install(Sessions) {
        cookie<UserSession>("user_session", SessionStorageMemory()) {
            cookie.path = "/"
            cookie.maxAge = (12).hours

            cookie.extensions["ERA_key"] = UUID.randomUUID().toString()
        }
    }
}