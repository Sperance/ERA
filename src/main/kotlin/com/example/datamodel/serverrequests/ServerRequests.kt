package com.example.datamodel.serverrequests

import com.example.currectDatetime
import com.example.datamodel.serverhistory.ServerHistory
import com.example.datamodel.serverhistory.serverHistory
import com.example.helpers.create
import com.example.interfaces.IntPostgreTable
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.request.uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
import org.komapper.core.dsl.metamodel.EntityMetamodel
import java.util.UUID

@Serializable
@KomapperEntity
@KomapperTable("tbl_serverrequests")
data class ServerRequests(
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "serverrequests_id")
    override val id: Int = 0,
    var url: String? = null,
    var clientUrl: String? = null,
    var uniqueKey: String? = null,
    var code: Int = 0,
    var errorMessage: String? = null,
    var dateInRequest: LocalDateTime? = null,
    var dateOutRequest: LocalDateTime? = null,
    @Transient
    @KomapperVersion
    override val version: Int = 0,
    @Transient
    override val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntPostgreTable<ServerRequests> {
    companion object {
        val tbl_serverrequests = Meta.serverRequests

        fun addServerRecord(call: ApplicationCall) {
            CoroutineScope(Dispatchers.IO).launch {
                ServerRequests(
                    url = call.request.path(),
                    clientUrl = "${call.request.local.remoteAddress}::${call.request.httpMethod.value}",
                    uniqueKey = call.response.headers["ERA-key"],
                    code = call.response.status()?.value?:0,
                    dateInRequest = call.response.headers["Request-TimeStamp"]?.toLocalDateTime(),
                    dateOutRequest = call.response.headers["Answer-TimeStamp"]?.toLocalDateTime(),
                    errorMessage = call.response.headers["Answer-Error"]
                ).create("ServerRequests::addServerRecord")
            }
        }
    }

    override fun getTable() = tbl_serverrequests
}