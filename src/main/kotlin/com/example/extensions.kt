package com.example

import io.ktor.server.application.ApplicationCall
import io.ktor.server.logging.toLogString
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