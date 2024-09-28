package com.example

import com.example.datamodel.clients.Clients
import com.example.datamodel.clients.ClientsNullbale
import com.example.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.junit.Test

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
//    @Test
//    fun test_put_reflection() {
//        val client = Clients()
//        client.login = "login"
//        println("login 1: ${client.login}")
//        client.putField("login", "sample")
//        println("login 2: ${client.login}")
//    }
    @Test
    fun test_date() {
        val client = Clients()
        client.login = "login"

        val nullable = ClientsNullbale()
        nullable.login = "ll"

//        client.updateFromNullable(nullable)

        println("CLIENT: ${client.login}")
        println("NULLABLE: ${nullable.login}")
    }
//    @Test
//    fun clear_databases() {
//        runBlocking {
//            db.withTransaction {
//                db.runQuery { QueryDsl.drop(tbl_clients) }
//                db.runQuery { QueryDsl.drop(tbl_employees) }
//                db.runQuery { QueryDsl.drop(tbl_services) }
//            }
//        }
//    }
}