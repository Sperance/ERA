package com.example

import com.example.datamodel.clients.Clients.Companion.tbl_clients
import com.example.datamodel.employees.Employees.Companion.tbl_employees
import com.example.datamodel.services.Services.Companion.tbl_services
import com.example.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import org.junit.Test
import org.komapper.core.dsl.QueryDsl

fun main() {
    embeddedServer(Netty, port = 6533, host = "95.163.84.228") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureDatabases()
}

class test_app {
    @Test
    fun test_date() {
        println("CURDATE: ${LocalDateTime.currectDatetime()}")
    }
    @Test
    fun clear_databases() {
        runBlocking {
            db.withTransaction {
                db.runQuery { QueryDsl.drop(tbl_clients) }
                db.runQuery { QueryDsl.drop(tbl_employees) }
                db.runQuery { QueryDsl.drop(tbl_services) }
            }
        }
    }
}