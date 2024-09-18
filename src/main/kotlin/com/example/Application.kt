package com.example

import com.example.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.logging.toLogString
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, port = 6533, host = "95.163.84.228") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    configureSerialization()
    creatingDatabases()
    configureMonitoring()
    configureRouting()
}

/**
 * Вывод информации по запросам на сервер в лог
 */
fun printCallLog(call: ApplicationCall) {
    println("[${call.request.toLogString()}] params ${call.parameters.entries()}")
}