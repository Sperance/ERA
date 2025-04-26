package com.example.plugins

import com.example.plugins.sse.RecordsSSE
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.sse.SSE

fun Application.configureSSE() {
    install(SSE)
    RecordsSSE.installRouting(this)
}