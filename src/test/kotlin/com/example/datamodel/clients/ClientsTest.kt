package com.example.datamodel.clients

import com.example.security.generateSalt
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ClientsTest {

    private val URL = "/clients"

    @Test
    fun test_structure() = testApplication {
        application {
            configureClients()
        }
        val response = client.get("$URL/structure")
        Assertions.assertEquals(HttpStatusCode.OK, response.status)
    }
    @Test
    fun test_clearTable() = testApplication {
        application {
            configureClients()
        }
        val response = client.get("$URL/clearTable")
        Assertions.assertEquals(HttpStatusCode.OK, response.status)
        Assertions.assertEquals("Таблица успешно очищена", response.bodyAsText())
    }
    @Test
    fun test_post() = testApplication {
        application {
            configureClients()
        }
        val response1 = client.post(URL) {
            contentType(ContentType.Application.Json)
            setBody(Clients(firstName = "fName", login = "login1", password = "password1", email = "email1", phone = "phone1", salt = generateSalt()).apply {
                setNewPassword(password!!)
            })
        }
        Assertions.assertEquals(HttpStatusCode.Created, response1.status)
        val response2 = client.post(URL) {
            contentType(ContentType.Application.Json)
            setBody(Clients(firstName = "fName", login = "login2", password = "password2", email = "email2", phone = "phone2", salt = generateSalt()).apply {
                setNewPassword(password!!)
            })
        }
        Assertions.assertEquals(HttpStatusCode.Created, response2.status)
        val response3 = client.post(URL) {
            contentType(ContentType.Application.Json)
            setBody(Clients(firstName = "fName", login = "login3", password = "password3", email = "email3", phone = "phone3", salt = generateSalt()).apply {
                setNewPassword(password!!)
            })
        }
        Assertions.assertEquals(HttpStatusCode.Created, response3.status)
    }
}