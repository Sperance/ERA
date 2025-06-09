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
import org.komapper.annotation.KomapperUpdatedAt
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
    var client_url: String? = null,
    var unique_key: String? = null,
    var code: Int = 0,
    var error_message: String? = null,
    var date_in_request: LocalDateTime? = null,
    var date_out_request: LocalDateTime? = null,
    @Transient
    @KomapperVersion
    override val version: Int = 0,
    @Transient
    override val created_at: LocalDateTime = LocalDateTime.currectDatetime(),
    @Transient
    @KomapperUpdatedAt
    override val updated_at: LocalDateTime = LocalDateTime.currectDatetime(),
    @Transient
    override val deleted: Boolean = false
) : IntPostgreTable<ServerRequests> {
    companion object {
        val tbl_serverrequests = Meta.serverRequests
        private val array_requests = ArrayList<ServerRequests>()

        fun addServerRecord(call: ApplicationCall) {

            var answer_error = call.response.headers["Answer-Error"]
            if (answer_error != null && answer_error.length > 499) {
                answer_error = answer_error.substring(0, 490)
            }

            val request = ServerRequests(
                url = call.request.path(),
                client_url = "${call.request.local.remoteAddress}::${call.request.httpMethod.value}",
                unique_key = call.response.headers["ERA-key"],
                code = call.response.status()?.value?:0,
                date_in_request = call.response.headers["Request-TimeStamp"]?.toLocalDateTime(),
                date_out_request = call.response.headers["Answer-TimeStamp"]?.toLocalDateTime(),
                error_message = answer_error
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