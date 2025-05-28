package com.example.captcha

import com.example.logging.DailyLogger.printTextLog
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json

object GCaptcha {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun verifyRecaptchaToken(token: String, secretKey: String): RecaptchaResponse {
        val response = client.post("https://www.google.com/recaptcha/api/siteverify") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
            setBody(FormDataContent(Parameters.build {
                append("secret", secretKey)
                append("response", token)
            }))
        }

        val result: RecaptchaResponse = response.body()
        printTextLog("[GCaptcha::verifyRecaptchaToken] Requested GCaptcha. Result: $result Token: $token")
        return result
    }
}