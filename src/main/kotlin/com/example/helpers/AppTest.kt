package com.example.helpers

import com.example.datamodel.catalogs.Catalogs
import com.example.datamodel.catalogs.Catalogs.Companion.tbl_catalogs
import com.example.datamodel.catalogs.CatalogsErrors
import com.example.datamodel.clients.Clients
import com.example.datamodel.clients.Clients.Companion.tbl_clients
import com.example.datamodel.employees.Employees
import com.example.datamodel.records.Records
import com.example.datamodel.records.Records.Companion.tbl_records
import com.example.datamodel.services.Services
import com.example.logObjectProperties
import com.example.logging.DailyLogger.printTextLog
import com.example.minus
import com.example.plugins.db
import com.example.plus
import com.example.toDateTimePossible
import io.ktor.network.tls.certificates.buildKeyStore
import io.ktor.network.tls.certificates.saveToFile
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Test
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import java.io.File
import kotlin.time.Duration.Companion.minutes

@Serializable
data class Recordsdata(
    var clientFrom: Clients?,
    var employeeTo: Employees?,
    var service: Services?,
    var record: Records?
)

class AppTest {

    @Test
    fun test_all_methods() {
        logObjectProperties(CatalogsErrors, Catalogs()).forEach {
            printTextLog("LOG: $it")
        }
    }

    @Test
    fun testColumns() {
        runBlocking {
            val obj = Meta.all()
            obj.forEach {
                println("TABLE: ${it.tableName()}")
            }
        }
    }

    @Test
    fun testTranscation() {
        printTextLog("[START]")
        runBlocking {
            db.withTransaction { tx ->
                try {
                    printTextLog("[IN START]")
                    val newCatalog = Catalogs().apply { type = "testType2"; category = "testCat2"; value = "testVal2" }.create("test")
                    testLaunchTransaction()
//               tx.setRollbackOnly()
                    printTextLog("[IN END]")
                } catch (e: Exception) {
                    tx.setRollbackOnly()
                    printTextLog("[EXCEPTION] ${e.localizedMessage}")
                }
            }
        }
        printTextLog("[END]")
    }

    suspend fun testLaunchTransaction() {
        val findCatal = Catalogs().getDataOne({ tbl_catalogs.type eq "testType" })
        if (true) throw Exception("SampleEx")
        findCatal?.value = "updated"
        findCatal?.update("testLaunchTransaction")
    }

    @Test
    fun sendEmail() {
        try {
            GMailSender().sendMail(
                "THEME",
                "message",
                "kaltemeis@gmail.com"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Test
    fun testDatetime() {
        val timeRequest = "2024-12-19T15:37:42.133"
        printTextLog(timeRequest.toDateTimePossible().toString())
    }

    @Test
    fun generateCertificate() {
        val keyStoreFile = File("build/keystore.jks")
        val keyStore = buildKeyStore {
            certificate("eraAlias") {
                password = "Password123."
                domains = listOf("127.0.0.1", "0.0.0.0", "localhost", "95.163.84.228")
            }
        }
        keyStore.saveToFile(keyStoreFile, "Pass123.")
    }

    @Test
    fun test_get_all_json() {
        runBlocking {
            val result = db.runQuery {
                QueryDsl.from(tbl_records)
                    .leftJoin(tbl_clients) { tbl_clients.id eq tbl_records.id_client_from }
//                    .innerJoin(tbl_services) { tbl_services.id eq Meta.records.id_service }
                    .selectAsEntity(tbl_records)
            }
            println("RES: $result")
        }
    }

    @Test
    fun test_range() {
        val stockPeriod = 30
        val blockSlots = 6 * stockPeriod

        val servDate = LocalDateTime(2024, 10, 16, 17, 0)
        val servDateBeg = servDate.minus((blockSlots - 1).minutes)
        val servDateTime = servDate.plus((4 * stockPeriod - 1).minutes)

        val servDate2 = LocalDateTime(2024, 10, 16, 11, 0)
        val servDate2Beg = servDate2.minus((blockSlots - 1).minutes)
        val servDateTime2 = servDate2.plus((6 * stockPeriod - 1).minutes)

        val arrayClosed = ArrayList<ClosedRange<LocalDateTime>>()
        arrayClosed.add(servDateBeg..servDateTime)
        arrayClosed.add(servDate2Beg..servDateTime2)

        println("New service length: $blockSlots min")
        println("service 1: Start $servDate End $servDateTime")
        println("service 2: Start $servDate2 End $servDateTime2")
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

        araResult.forEach(::println)
    }
}