package com.example.captcha

import com.example.applicationTomlSettings
import com.example.basemodel.ResultResponse
import com.example.captcha.GCaptcha.verifyRecaptchaToken
import com.example.generateMapError
import com.example.respond
import io.ktor.server.application.Application
import io.ktor.server.request.receiveParameters
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureGCaptcha() {
    routing {
        route("/captcha") {
            post("/submit-form") {
                val params = call.receiveParameters()
                val recaptchaToken = params["g-recaptcha-response"] ?: return@post call.respond(ResultResponse.Error(generateMapError(call, 100 to "Not find parameter 'g-recaptcha-response' in form-data request")))

                val res = verifyRecaptchaToken(recaptchaToken, secretKey = applicationTomlSettings!!.SETTINGS.CAPTHCA_V3)
                val isHuman = res.success

                if (!isHuman) {
                    call.respond(ResultResponse.Error(generateMapError(call, 101 to (res.errorCodes?.joinToString()?:"Verify token error"))))
                } else {
                    call.respond(ResultResponse.Success("Success"))
                }
            }
        }
    }
}