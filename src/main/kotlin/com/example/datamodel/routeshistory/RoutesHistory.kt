package com.example.datamodel.routeshistory

import com.example.currectDatetime
import com.example.datamodel.BaseRepository
import com.example.datamodel.create
import com.example.datamodel.records.Records
import com.example.nullDatetime
import com.example.toDateTimePossible
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import io.ktor.server.request.contentLength
import io.ktor.server.request.header
import io.ktor.server.request.httpMethod
import io.ktor.server.request.httpVersion
import io.ktor.server.request.uri
import io.ktor.server.routing.Route
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable
import org.komapper.annotation.KomapperVersion
import org.komapper.core.dsl.Meta

@Serializable
@KomapperEntity
@KomapperTable("tbl_routeshistory")
data class RoutesHistory(
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "routeshistory_id")
    val id: Int = 0,
    var httpMethod: String = "",
    var uri: String = "",
    var parameters: String = "",
    var clientTime: LocalDateTime? = null,
    var requestTime: LocalDateTime? = null,
    var respondTime: LocalDateTime? = null,
    var timeDifference: String? = null,
    var httpVersion: String = "",
    var contentLength: Long = 0,
    var remoteAddress: String = "",
    var remoteHost: String = "",
    var remotePort: String = "",
    var respondData: String = "",
    @Transient
    @KomapperVersion
    val version: Int = 0,
    @Transient
    val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) {
    fun fillFromCall(call: ApplicationCall) {
        this.parameters = call.parameters.entries().joinToString("; ")
        this.httpMethod = call.request.httpMethod.value
        this.uri = call.request.uri.substringBefore("?")
        val headerTimestamp = call.request.header("Request_Timestamp")?.trim()?.replace(" ", "T")
        if (headerTimestamp.toDateTimePossible()) this.clientTime = headerTimestamp?.toLocalDateTime()
        this.httpVersion = call.request.httpVersion
        this.contentLength = call.request.contentLength()?:0
        this.remoteAddress = call.request.headers["X-Forwarded-For"]
            ?: call.request.headers["X-Real-IP"]
                    ?: call.request.origin.remoteAddress
        this.remoteHost = call.request.origin.remoteHost
        this.remotePort = call.request.origin.remotePort.toString()
    }

    companion object {
        val tbl_routeshistory = Meta.routesHistory
    }
}