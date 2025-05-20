package com.example

import com.example.basemodel.IntBaseDataImpl
import com.example.basemodel.ResultResponse
import com.example.helpers.getField
import com.example.helpers.putField
import com.example.helpers.CommentField
import com.example.logging.DailyLogger.printTextLog
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.util.toLocalDateTime
import io.ktor.utils.io.InternalAPI
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

fun String?.toIntPossible() : Boolean {
    if (this == null) return false
    return this.toIntOrNull() != null
}

fun LocalDateTime.plus(duration: Duration) : LocalDateTime {
    return this.toInstant(TimeZone.UTC).plus(duration).toLocalDateTime(TimeZone.UTC)
}

fun LocalDateTime.minus(duration: Duration) : LocalDateTime {
    return this.toInstant(TimeZone.UTC).minus(duration).toLocalDateTime(TimeZone.UTC)
}

fun String?.toDateTimePossible() : Boolean {
    if (this == null) return false
    return try {
        LocalDateTime.parse(this)
        true
    } catch (_: Exception) {
        false
    }
}

fun Number?.isNullOrZero() : Boolean {
    if (this == null) return true
    if (this == 0) return true
    return false
}

fun LocalDateTime?.isNullOrEmpty() : Boolean {
    if (this == null) return true
    if (this == LocalDateTime.nullDatetime()) return true
    return false
}

fun LocalDateTime.Companion.nullDatetime() = LocalDateTime(2000, 1, 1, 0, 0, 0)

fun LocalDateTime.Companion.currentZeroDate() : LocalDateTime {
    val curDate = currectDatetime()
    return LocalDateTime(curDate.year, curDate.monthNumber, curDate.dayOfMonth, 0, 0).minus((3).hours)
}

@OptIn(InternalAPI::class)
fun LocalDateTime.Companion.currectDatetime() = Date().toLocalDateTime().toKotlinLocalDateTime().minus((3).hours)

suspend fun ApplicationCall.respond(response: ResultResponse) {
    try {
        when(response) {
            is ResultResponse.Error -> {
                this.response.headers.append("Answer-TimeStamp", LocalDateTime.currectDatetime().toString())
                this.response.headers.append("Answer-Error", response.message.toString())
                respond(
                    status = response.status.httpCode,
                    message = response.message)
            }
            is ResultResponse.Success -> {
                this.response.headers.append("Answer-TimeStamp", LocalDateTime.currectDatetime().toString())
                response.headers?.forEach { (key, value) ->
                    this.response.headers.append(key, value)
                }
                respond(status = response.status.httpCode, message = response.data ?: "")
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        printTextLog("[ApplicationCall] Error: ${e.localizedMessage}")
    }
}

fun Any?.isAllNullOrEmpty() : Boolean {
    if (this == null) return true
    when (this) {
        is String -> { return this.isEmpty() }
        is Number -> { return this.isNullOrZero() }
    }
    return false
}

fun <T: Any> IntBaseDataImpl<*>.updateFromNullable(nullable: T) : Int {
    var counterUpdated = 0
    nullable::class.java.declaredFields.filter { !it.name.lowercase().contains("companion") }.forEach {

        if (it.name.lowercase() == "id") return@forEach
        if (it.name.lowercase() == "version") return@forEach
        if (it.name.lowercase() == "createdat") return@forEach

        it.isAccessible = true
        val value = it.get(nullable)
        if (value != null && this.getField(it.name) != value) {
            counterUpdated++
            this.putField(it.name, value)
        }
    }
    return counterUpdated
}

fun Long.toFormatDateTime() : String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date(this))
}

fun Field?.getCommentFieldAnnotation(): String {
    val ann = this?.getAnnotation(CommentField::class.java)
    if (ann == null) return ""
    return "[${ann.name}]"
}

fun IntBaseDataImpl<*>.generateMapError(call: ApplicationCall, errorPair: Pair<Int, String>): MutableMap<String, String> {
    val map = mutableMapOf<String, String>()
    map["errorKey"] = getTable().tableName().uppercase() + "_" + errorPair.first.toString()
    map["errorCode"] = errorPair.first.toString()
    map["errorDescription"] = errorPair.second
    map["errorType"] = call.request.httpMethod.value
    map["errorUri"] = call.request.uri
    map["errorTable"] = getTable().tableName()
    map["requestKey"] = call.response.headers["ERA-key"].toString()
    return map
}

fun generateMapError(call: ApplicationCall, errorPair: Pair<Int, String>): MutableMap<String, String> {
    val map = mutableMapOf<String, String>()
    map["errorCode"] = errorPair.first.toString()
    map["errorDescription"] = errorPair.second
    map["errorType"] = call.request.httpMethod.value
    map["errorUri"] = call.request.uri
    map["requestKey"] = call.response.headers["ERA-key"].toString()
    return map
}