package com.example.datamodel.serverhistory

import com.example.currectDatetime
import com.example.datamodel.BaseRepository
import com.example.datamodel.create
import com.example.datamodel.records.Records
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import io.ktor.server.request.httpMethod
import io.ktor.server.request.httpVersion
import io.ktor.server.request.uri
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
@KomapperTable("tbl_serverhistory")
data class ServerHistory(
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "serverhistory_id")
    val id: Int = 0,
    var code: Int = 0,
    var message: String = "",
    var value: String = "",
    @Transient
    @KomapperVersion
    val version: Int = 0,
    @Transient
    val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) {
    companion object {
        val tbl_serverhistory = Meta.serverHistory

        suspend fun addRecord(code: Int, message: String, value: String) {
            ServerHistory(code = code, message = message, value = value).create(null)
        }
    }
}