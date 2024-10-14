package com.example

import com.example.datamodel.IntBaseData
import com.example.datamodel.ResultResponse
import com.example.datamodel.getField
import com.example.datamodel.putField
import io.ktor.server.application.ApplicationCall
import io.ktor.server.logging.toLogString
import io.ktor.server.response.respond
import io.ktor.server.util.toLocalDateTime
import io.ktor.util.InternalAPI
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.reflect.full.declaredMemberProperties

fun String?.toIntPossible() : Boolean {
    if (this == null) return false
    return this.toIntOrNull() != null
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

@OptIn(InternalAPI::class)
fun LocalDateTime.Companion.currectDatetime() = Date().toLocalDateTime().toKotlinLocalDateTime()

/**
 * Вывод информации по запросам на сервер в лог
 */
fun printCallLog(call: ApplicationCall) {
    val curDTime = System.currentTimeMillis().toFormatDateTime()
    println("$curDTime [${call.request.local.remoteAddress}::${call.request.local.remotePort}][${call.request.toLogString()}] params: ${call.parameters.entries()}")
}

suspend fun ApplicationCall.respond(response: ResultResponse) {
    printTextLog("[ApplicationCall::respond] ${response::class.simpleName} code: ${response.status}")
    when(response) {
        is ResultResponse.Error -> respond(status = response.status, message = response.message)
        is ResultResponse.Success -> respond(status = response.status, message = response.data)
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

fun IntBaseData<*>.nulling() {
    this::class.declaredMemberProperties.forEach {
        if (SYS_FIELDS_ARRAY.contains(it.name.lowercase())) return@forEach
        if (!it.returnType.isMarkedNullable) return@forEach
        this.putField(it.name, null)
    }
}

fun <T: Any> IntBaseData<*>.updateFromNullable(nullable: T) : Int {
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

fun printTextLog(text: String) {
    val curDTime = System.currentTimeMillis().toFormatDateTime()
    println("$curDTime $text")
}

fun Long.toFormatDateTime() : String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date(this))
}


fun Double.getPercent(value: Double) : Double {
    return ((this / 100.0) * value).to1Digits()
}

fun Double.addPercent(value: Double) : Double {
    return (this + getPercent(value)).to1Digits()
}

fun Double.removePercent(value: Double) : Double {
    return (this - getPercent(value)).to1Digits()
}

fun Double.to1Digits() = String.format("%.1f", this).replace(",", ".").toDouble()

inline fun <reified T> Any.listFields() : ArrayList<T> {
    val array = ArrayList<T>()
    this::class.java.declaredFields.forEach {
        it.isAccessible = true
        val itField = it.get(this)
        if (itField is T) array.add(itField as T)
    }
    return array
}

fun Field?.getCommentFieldAnnotation(): String {
    val ann = this?.getAnnotation(CommentField::class.java)
    if (ann == null) return ""
    return "{ ${ann.name}; обязательное: ${ann.required} }"
}