package com.example.datamodel.clients

import com.example.currentZeroDate
import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.clearTable
import com.example.datamodel.clients.Clients.Companion.tbl_clients
import com.example.datamodel.getData
import com.example.datamodel.records.Records
import com.example.datamodel.records.Records.Companion.tbl_records
import com.example.datamodel.services.Services
import com.example.getCommentFieldAnnotation
import com.example.minus
import com.example.plus
import com.example.printCallLog
import com.example.respond
import com.example.toIntPossible
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.datetime.LocalDateTime
import java.io.File
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

fun Application.configureClients() {
    routing {

        staticFiles("/files/clients", File("files/clients"))

        route("/clients") {

            get("/structure") {
                printCallLog(call)
                call.respond(ResultResponse.Success(HttpStatusCode.OK, Clients().getCommentArray()))
            }

            get("/clearTable") {
                printCallLog(call)
                Clients().clearTable()
                call.respond(ResultResponse.Success(HttpStatusCode.OK, "Таблица успешно очищена"))
            }

            get ("/timeslot/{clientId}/{servceLength}") {
                printCallLog(call)

                val _clientId = call.parameters["clientId"]
                val _servceLength = call.parameters["servceLength"]

                if (_clientId == null || !_clientId.toIntPossible()) {
                    call.respond("Param clientId must be Int type")
                    return@get
                }

                if (_servceLength == null || !_servceLength.toIntPossible()) {
                    call.respond("Param servceLength must be Int type")
                    return@get
                }

                val clientId = _clientId.toInt()
                val servceLength = _servceLength.toInt()

                val startDate = LocalDateTime.currentZeroDate()
                val endDate = startDate.plus((30).days)

                val currentRecords = Records().getData({ tbl_records.id_client_to eq clientId ; tbl_records.dateRecord.between(startDate..endDate) ; tbl_records.status.inList(
                    listOf("Заказ создан", "Заказ принят")
                ) })
                val allServices = Services().getData()

                val stockPeriod = 15
                val blockSlots = servceLength * stockPeriod

                val arrayClosed = ArrayList<ClosedRange<LocalDateTime>>()
                currentRecords.filter { it.dateRecord!! in startDate..endDate }.forEach {
                    val servLen = allServices.find { serv -> serv.id == it.id_service }!!
                    val servDateBegin = it.dateRecord!!.minus((blockSlots - 1).minutes)
                    val servDateEnd = it.dateRecord!!.plus((servLen.duration!! * stockPeriod - 1).minutes)
                    arrayClosed.add(servDateBegin..servDateEnd)
                }

                var stockDate = startDate
                val maxDateTime = stockDate.plus((30).days).minus((blockSlots - 1).minutes)
                val araResult = ArrayList<LocalDateTime>()
                while (true) {
                    if (stockDate >= maxDateTime) break
                    val finded = arrayClosed.find { ar -> stockDate in ar }
                    if (finded == null && stockDate.hour in 10..19) {
                        araResult.add(stockDate)
                    }
                    stockDate = stockDate.plus((stockPeriod).minutes)
                }

                call.respond(HttpStatusCode.OK, araResult)
            }

            get("/all") {
                printCallLog(call)
                call.respond(Clients().get(call, IntBaseDataImpl.RequestParams()))
            }

            get("/all/{clientType}") {
                printCallLog(call)
                call.respond(Clients().getFromType(call))
            }

            get("/{id}") {
                printCallLog(call)
                call.respond(Clients().getId(call, IntBaseDataImpl.RequestParams()))
            }

            get("/slots/{id}/{data}") {
                printCallLog(call)
                call.respond(Clients().getSlots(call))
            }

            post("/auth") {
                printCallLog(call)
                call.respond(Clients().auth(call))
            }

            post("/update") {
                printCallLog(call)
                call.respond(Clients().update(call, IntBaseDataImpl.RequestParams()))
            }

            post("/array") {
                printCallLog(call)
                call.respond(Clients().postArray(call, IntBaseDataImpl.RequestParams()))
            }

            post {
                printCallLog(call)
                call.respond(Clients().post(call, IntBaseDataImpl.RequestParams()))
            }

            delete {
                printCallLog(call)
                call.respond(Clients().delete(call, IntBaseDataImpl.RequestParams()))
            }
        }
    }
}
