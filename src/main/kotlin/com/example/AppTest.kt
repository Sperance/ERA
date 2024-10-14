package com.example

import com.example.datamodel.clients.Clients
import com.example.datamodel.clients.Clients.Companion.tbl_clients
import com.example.datamodel.putField
import com.example.datamodel.records.Records
import com.example.datamodel.records.Records.Companion.tbl_records
import com.example.datamodel.services.Services
import com.example.plugins.db
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.junit.Test
import org.komapper.core.dsl.QueryDsl
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.full.declaredMemberProperties

@Serializable
data class Recordsdata (
    var clientFrom: Clients?,
    var clientTo: Clients?,
    var service: Services?,
    var record: Records?
)

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
    fun test_null_class() {
        val objClass = Nulling(id = 1, nonnull = "23", name = null, price = 124)
        println(objClass)
        objClass.nulling()
        println(objClass)
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
        val printer = Nulling(id = 0, nonnull = "123", name = "Name")
        printer::class.java.declaredFields.forEach {
            println("ann: ${it.getCommentFieldAnnotation()}")
        }
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