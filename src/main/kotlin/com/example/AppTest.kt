package com.example

import com.example.datamodel.BaseRepository
import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.clients.Clients
import com.example.datamodel.clients.Clients.Companion.tbl_clients
import com.example.datamodel.getField
import com.example.datamodel.getMethod
import com.example.datamodel.putField
import com.example.datamodel.records.Records
import com.example.datamodel.records.Records.Companion.tbl_records
import com.example.datamodel.services.Services
import com.example.datamodel.stockfiles.Stockfiles
import com.example.plugins.GMailSender
import com.example.plugins.db
import io.ktor.network.tls.certificates.buildKeyStore
import io.ktor.network.tls.certificates.saveToFile
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import org.junit.Test
import org.komapper.core.dsl.QueryDsl
import java.io.File
import java.util.Properties
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.declaredMembers
import kotlin.time.Duration.Companion.minutes


@Serializable
data class Recordsdata (
    var clientFrom: Clients?,
    var clientTo: Clients?,
    var service: Services?,
    var record: Records?
)

@Suppress("UNCHECKED_CAST")
class AppTest {

    private fun Any.nulling() {
        this::class.declaredMemberProperties.forEach {
            if (listOf("companion", "id", "version", "createdAt").contains(it.name.lowercase())) return@forEach
            if (!it.returnType.isMarkedNullable) return@forEach
            this.putField(it.name, null)
        }
    }

    private fun Any.integrate(obj: Any) {
        this::class.declaredMemberProperties.forEach {
            if (listOf("companion", "id", "version", "createdAt").contains(it.name.lowercase())) return@forEach
            if (!it.returnType.isMarkedNullable) return@forEach
            this.putField(it.name, null)
        }
    }

    @Test
    fun testCompanion() {
        val cl = Clients()
        getObjectRepository(cl)
    }

    @Test
    fun sendEmail() {
        try {
            GMailSender().sendMail(
                    "THEME",
                    "message",
                    "kaltemeis@gmail.com")
        }catch (e: Exception) {
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
    fun test_null_class() {
        println(calculateDifference(LocalDateTime.currentZeroDate(), LocalDateTime.currectDatetime()))
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
    fun test_range(){
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

    data class Nulling(
        var id: Int,
        @CommentField("НОН НУЛЛ парент", true)
        var nonnull: String,
        var name: String?,
        var price: Int? = null,
        var date: LocalDateTime? = LocalDateTime.nullDatetime(),
        var category: String? = "Client"
    ) {
        fun testFun() {
            println("textfff")
        }

        override fun toString(): String {
            return "Nulling(id=$id, nonnull='$nonnull', name=$name, price=$price, date=$date, category=$category)"
        }
    }

    @Test
    fun test_structure() {
        val testDate = LocalDateTime.currectDatetime()
    }

    @Test
    fun test_reflect() {

        val defaults: ArrayList<(Clients) -> KMutableProperty0<*>> = ArrayList()

        val clie = Clients(clientType = "asd")
        println("type1: ${clie.clientType}")

        defaults.add { it::clientType }

        defaults.forEach { def ->
            val res = def.invoke(clie) as KMutableProperty0<Any?>
            println("res: ${res.get()}")
            res.set("newSet444te333d")
        }

        println("type2: ${clie.clientType}")
    }
}