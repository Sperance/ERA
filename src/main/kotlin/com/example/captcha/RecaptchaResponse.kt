package com.example.captcha

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecaptchaResponse(
    val success: Boolean,
    @SerialName("challenge_ts") val challengeTs: String? = null,
    val hostname: String? = null,
    @SerialName("error-codes") val errorCodes: List<String>? = null,
    val score: Float? = null,
    val action: String? = null
) {
    override fun toString(): String {
        return "RecaptchaResponse(success=$success, challengeTs=$challengeTs, hostname=$hostname, errorCodes=$errorCodes, score=$score, action=$action)"
    }
}