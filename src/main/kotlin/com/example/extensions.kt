package com.example

import com.example.datamodel.BaseRepository
import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.helpers.getField
import com.example.helpers.putField
import com.example.helpers.CommentField
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.util.toLocalDateTime
import io.ktor.utils.io.InternalAPI
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.io.File
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

fun Long.toFormatDateTime() : String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date(this))
}

fun Field?.getCommentFieldAnnotation(): String {
    val ann = this?.getAnnotation(CommentField::class.java)
    if (ann == null) return ""
    return "[${ann.name}; обязательное: ${ann.required}]"
}

fun isSafeCommand(command: String): String? {
    if (command.trim().isBlank()) return "<blank>"
    if (command.trim() == "/") return "/"
    if (command.trim() == "//") return "//"
    val unsafePatterns = listOf(
        "pstree", "nice", "renice", "ulimit", "dns", "www.", ".org", "/geoserver", "/script",
        "http:", "/api.", ".php", "-stdin", "/json", ".com:443", ".asp", "-bin", ".env", ".pn:443",
        "/robots", ".git", "/login", "goform", "_cfg", "/version",
        "/versions", ".zip", ".html", "/gateway", "/login", "/hello.", "/formLogin", "/admin",
        "/actuator", "/health", "/css", "ab2g", "ab2h", "ReportServer", ".rar",
        "/webui", "/chec", "/powershell", "/sitemap", "/v1", ".tar", ".gz", "/metadata",
        "/web/", "/doc/", ".7z", ".xml", "debug", ".cgi", "pro.", ".js", "/x.", "/owa/",
        "/query", "/resolve", "/GponForm/", "/diag_", ".application", "/ecp/", "/microsoft",
        "/aaa", "/aab", "/index", ".ico", "/device", "/onvif", "web.", ".in:", "config",
        "/t4", "/teorema5", "/HNAP1", "-", "*", "#", "!", ":1", ":2", ":3", ":4", ":5",
        ":6", ":7", ":8", ":9", ":0", "_profiler", "/env", "%")
    return unsafePatterns.find { pat -> command.trim().lowercase().contains(pat.trim().lowercase()) }
}