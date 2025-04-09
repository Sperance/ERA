package com.example

import com.example.datamodel.BaseRepository
import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.getField
import com.example.datamodel.putField
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.logging.toLogString
import io.ktor.server.plugins.origin
import io.ktor.server.request.httpMethod
import io.ktor.server.request.httpVersion
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.util.toLocalDateTime
import io.ktor.util.InternalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMembers
import kotlin.time.Duration

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
    return LocalDateTime(curDate.year, curDate.monthNumber, curDate.dayOfMonth, 0, 0)
}

@OptIn(InternalAPI::class)
fun LocalDateTime.Companion.currectDatetime() = Date().toLocalDateTime().toKotlinLocalDateTime()

suspend fun ApplicationCall.respond(response: ResultResponse) {
    when(response) {
        is ResultResponse.Error -> respond(status = response.status, message = response.message)
        is ResultResponse.Success -> respond(status = response.status, message = response.data)
    }
}

fun calculateDifference(start: LocalDateTime?, end: LocalDateTime?): String {

    if (start == null || end == null) return ""

    // Вычисляем продолжительность между двумя датами
    val duration = java.time.Duration.between(start.toJavaLocalDateTime(), end.toJavaLocalDateTime())

    var resultString = ""
    // Получаем количество дней, часов, минут и секунд
    val days = duration.toDays()
    if (days > 0) resultString += "${days}d "
    val hours = duration.toHours() % 24
    if (hours > 0) resultString += "${hours}h "
    val minutes = duration.toMinutes() % 60
    if (minutes > 0) resultString += "${minutes}m "
    val seconds = duration.seconds % 60
    if (seconds > 0) resultString += "${seconds}s "
    val millis = duration.toMillis() % 1000
    if (millis > 0) resultString += "${millis}ms"

    // Формируем строку с результатом
    return resultString
}

@Suppress("UNCHECKED_CAST")
fun <T: Any> getObjectRepository(obj: T) : BaseRepository<T>? {
    val simpleClass = obj::class.java
    val instance = simpleClass.kotlin.companionObject?.declaredMembers?.find { it.name == "repo_${simpleClass.simpleName.lowercase()}" }
    return instance?.call(simpleClass.kotlin.companionObjectInstance) as BaseRepository<T>?
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
fun Double.to0Digits() = String.format("%.0f", this).replace(",", ".").toDouble()

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
    return "[${ann.name}; обязательное: ${ann.required}]"
}

fun isSafeCommand(command: String): String? {
    if (command.trim() == "/") return "/"
    if (command.trim() == "//") return "//"
    val unsafePatterns = listOf(
        "rm -rf", "wget", "chmod 777", "curl", "gzip", "gunzip", "bzip2", "unzip",
        "tar -x", "tar -xf", "tar -czf", "tar -xzf", "tar -cjf", "tar -xjf", "tar -cJf", "tar -xJf",
        "bash", "python", "perl", "ruby", "node", "java", "ruby",
        "find", "grep", "xargs", "echo", "more", "less", "head", "tail",
        "paste", "sort", "uniq","diff", "comm", "join", "paste", "split",
        "expand", "unexpand", "fold", "base64", "uuencode",
        "uudecode", "xxd", "hexdump", "strings", "stat",
        "touch", "mkdir", "rmdir", "chown", "chgrp", "chmod",
        "passwd", "sudo", "useradd", "userdel", "groupadd", "groupdel", "usermod",
        "groupmod", "crontab", "systemctl", "init", "reboot", "shutdown",
        "halt", "poweroff", "kill", "pkill", "killall", "htop", "pgrep",
        "pstree", "nice", "renice", "ulimit", "restore.php", "dns", "www.", ".org", "/geoserver", "/script",
        "http:", "/api.", ".php", "-stdin", "/json", ".com:443", ".asp", "-bin", ".env", ".pn:443",
        "/robots", ".git", "/login", "goform", "set_LimitClient_cfg", "_cfg", "/version",
        "/versions", ".zip", ".html", "/gateway", "/login", "/hello.", "/formLogin", "/admin",
        "/actuator", "/health", "/css", "ab2g", "ab2h", "ReportServer", ".rar",
        "/webui", "/chec", "/powershell", "/sitemap", "/v1")
    return unsafePatterns.find { pat -> command.trim().lowercase().contains(pat.trim().lowercase()) }
}