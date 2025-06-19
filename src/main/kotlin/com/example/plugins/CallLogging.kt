package com.example.plugins

import com.example.currectDatetime
import com.example.datamodel.serverrequests.ServerRequests
import com.example.helpers.AUTH_ERROR_KEY
import com.example.helpers.IpApiResponse
import com.example.helpers.getLocationByIp
import com.example.logging.DailyLogger.printTextLog
import io.ktor.server.application.*
import org.slf4j.event.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.util.AttributeKey
import io.ktor.util.putAll
import kotlinx.datetime.LocalDateTime
import java.util.UUID

val attrGEO = AttributeKey<IpApiResponse>("GEO")

fun Application.configureCallLogging() {
    install(CallLogging) {
        level = Level.TRACE
        filter { call ->
            call.response.headers["ERA-key"] != null
        }
        format { call ->

            val errorMsg = if (call.attributes.contains(AUTH_ERROR_KEY)) {
                call.attributes[AUTH_ERROR_KEY]
            } else {
                call.response.headers["Answer-Error"]
            }

            val str = "Request: ${call.request.local.remoteAddress}::${call.request.httpMethod.value} ${call.request.path()} params: [${call.request.queryParameters.entries().joinToString()}] -> Response: ${call.response.status()} ${errorMsg?:""}"
            printTextLog("[CallLogging] $str ATRS: ${call.attributes.allKeys.joinToString(", ")}", false)

            if (call.response.headers["Answer-TimeStamp"] != null) ServerRequests.addServerRecord(call)
            str
        }
    }

    intercept(ApplicationCallPipeline.Call) {
        try {
            call.response.headers.append("Request-TimeStamp", LocalDateTime.currectDatetime().toString())
            call.response.headers.append("ERA-key", UUID.randomUUID().toString().replace("-", ""))

            if (call.request.local.remoteAddress != "127.0.0.1") {
                val geo = getLocationByIp(call.request.local.remoteAddress)
                call.attributes.putAll(geo.toFormatAttributes())
            }
        }catch (_: Exception) { }
    }
}