package com.example.datamodel.routeshistory

import com.example.currectDatetime
import com.example.datamodel.BaseRepository
import com.example.datamodel.create
import com.example.datamodel.records.Records
import com.example.nullDatetime
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import io.ktor.server.request.httpMethod
import io.ktor.server.request.httpVersion
import io.ktor.server.request.uri
import io.ktor.server.routing.Route
import kotlinx.datetime.LocalDateTime
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
    var requestTime: LocalDateTime? = null,
    var httpVersion: String = "",
    var contentLength: String = "",
    var remoteAddress: String = "",
    var remoteHost: String = "",
    var remotePort: String = "",
    var respondData: String = "",
    var respondTime: LocalDateTime? = null,
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
        this.httpVersion = call.request.httpVersion
        this.contentLength = call.request.headers["Content-Length"]?:""
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