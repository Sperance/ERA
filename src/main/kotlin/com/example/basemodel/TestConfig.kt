package com.example.basemodel

import com.example.datamodel.authentications.Authentications
import com.example.datamodel.authentications.secureGet
import com.example.datamodel.authentications.securePost
import com.example.datamodel.clients.Clients
import com.example.enums.EnumBearerRoles
import com.example.enums.EnumSQLTypes
import com.example.minus
import com.example.helpers.GMailSender
import com.example.helpers.clearTable
import com.example.helpers.executeAddColumn
import com.example.helpers.executeDelColumn
import com.example.plus
import com.example.respond
import com.example.toIntPossible
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes

fun Application.configureTests() {
    routing {
        route("/test") {
            get ("/createColumn") {
                val res = Clients().executeAddColumn("newColumn", EnumSQLTypes.VARCHAR_255)
                call.respond(HttpStatusCode.OK, res?:"")
            }
            get ("/deleteColumn") {
                val res = Clients().executeDelColumn("newColumn")
                call.respond(HttpStatusCode.OK, res?:"")
            }
            get("/authentications/clearTable") {
                Authentications().clearTable()
                call.respond(ResultResponse.Success("Таблица успешно очищена"))
            }
            post ("/emailMessage") {
                val email = call.parameters["email"]

                if (email.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadGateway, "Param email must be selected")
                    return@post
                }

                try {
                    GMailSender().sendMail(
                            "Test Theme",
                            "Test message",
                        email)
                }catch (e: Exception) {
                    call.respond(HttpStatusCode.BadGateway, "Email error: ${e.localizedMessage}")
                    return@post
                }
                call.respond(HttpStatusCode.OK, "Email successfully sended")
            }
            get ("/slot_time/{serviceLength}") {

                val serviceLength = call.parameters["serviceLength"]

                if (serviceLength == null || !serviceLength.toIntPossible()) {
                    call.respond("Param serviceLength must be Int type")
                    return@get
                }

                val stockPeriod = 30
                val blockSlots = serviceLength.toInt() * stockPeriod

                val servDate = LocalDateTime(2024, 10, 16, 17, 0)
                val servDateBeg = servDate.minus((blockSlots - 1).minutes)
                val servDateTime = servDate.plus((4 * stockPeriod - 1).minutes)

                val servDate2 = LocalDateTime(2024, 10, 16, 11, 0)
                val servDate2Beg = servDate2.minus((blockSlots - 1).minutes)
                val servDateTime2 = servDate2.plus((6 * stockPeriod - 1).minutes)

                val arrayClosed = ArrayList<ClosedRange<LocalDateTime>>()
                arrayClosed.add(servDateBeg..servDateTime)
                arrayClosed.add(servDate2Beg..servDateTime2)

                var resultString = ""

                resultString += ("New service length: $blockSlots min\n")
                resultString += ("service 1: Start $servDate End ${servDateTime.plus((1).minutes)}\n")
                resultString += ("service 2: Start $servDate2 End ${servDateTime2.plus((1).minutes)}\n")
                val araResult = ArrayList<LocalDateTime>()

                var stockDate = LocalDateTime(2024, 10, 16, 9, 0)
                val maxDateTime = LocalDateTime(2024, 10, 16, 23, 0).minus((blockSlots - 1).minutes)
                while (true) {
                    if (stockDate >= maxDateTime) break
                    val finded = arrayClosed.find { ar -> stockDate in ar }
                    if (finded == null) {
                        araResult.add(stockDate)
                    }
                    stockDate = stockDate.toInstant(TimeZone.UTC).plus((stockPeriod).minutes).toLocalDateTime(TimeZone.UTC)
                }

                resultString += "\nFree dates: \n"
                araResult.forEach {
                    resultString += "$it\n"
                }
                call.respond(HttpStatusCode.OK, resultString)
            }

            route("/bearer") {
                secureGet("/getData") { userId ->
                    call.respond(HttpStatusCode.OK, "Hello $userId")
                }
                secureGet("/getData_admin", EnumBearerRoles.ADMIN) { userId ->
                    call.respond(HttpStatusCode.OK, "Hello $userId")
                }

                securePost("/postData") { userId ->
                    call.respond(HttpStatusCode.OK, "Hello $userId")
                }
                securePost("/postData_admin", EnumBearerRoles.ADMIN) { userId ->
                    call.respond(HttpStatusCode.OK, "Hello $userId")
                }
            }
        }
    }
}