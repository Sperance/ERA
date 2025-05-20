package com.example.datamodel.serverrequests

import com.example.currectDatetime
import com.example.helpers.createBatch
import com.example.interfaces.IntPostgreTable
import com.example.logging.DailyLogger.printTextLog
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
import kotlin.time.Duration.Companion.minutes

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
        private val array_requests = ArrayList<ServerRequests>()

        fun addServerRecord(call: ApplicationCall) {
            val request = ServerRequests(
                url = call.request.path(),
                clientUrl = "${call.request.local.remoteAddress}::${call.request.httpMethod.value}",
                uniqueKey = call.response.headers["ERA-key"],
                code = call.response.status()?.value?:0,
                dateInRequest = call.response.headers["Request-TimeStamp"]?.toLocalDateTime(),
                dateOutRequest = call.response.headers["Answer-TimeStamp"]?.toLocalDateTime(),
                errorMessage = call.response.headers["Answer-Error"]
            )
            array_requests.add(request)
        }

        fun lauchBatchedWriteDB() = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay((1).minutes)
                if (array_requests.isNotEmpty()) {
                    ServerRequests().createBatch("lauchBatchedWriteDB", array_requests)
                    printTextLog("[ServerRequests::lauchBatchedWriteDB] added ${array_requests.size} records")
                    array_requests.clear()
                }
            }
        }
    }

    override fun getTable() = tbl_serverrequests
}