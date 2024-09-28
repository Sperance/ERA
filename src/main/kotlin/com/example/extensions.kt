package com.example

import com.example.datamodel.IntBaseData
import com.example.datamodel.ResultResponse
import com.example.datamodel.employees.EmployeesNullable
import com.example.datamodel.getField
import com.example.datamodel.putField
import io.ktor.server.application.ApplicationCall
import io.ktor.server.logging.toLogString
import io.ktor.server.request.receiveText
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.util.toLocalDateTime
import io.ktor.util.InternalAPI
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun String?.toIntPossible() : Boolean {
    if (this == null) return false
    return this.toIntOrNull() != null
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

fun <T: Any> IntBaseData.updateFromNullable(nullable: T) : Int {
    var counterUpdated = 0
    nullable::class.java.declaredFields.filter { !it.name.lowercase().contains("companion") }.forEach {
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