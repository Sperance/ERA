package com.example

import com.example.datamodel.putField
import kotlinx.datetime.LocalDateTime
import org.junit.Test
import kotlin.reflect.full.declaredMemberProperties

class AppTest {

    data class Nulling(
        var id: Int,
        var nonnull: String,
        var name: String?,
        var price: Int? = null,
        var date: LocalDateTime? = LocalDateTime.nullDatetime(),
        var category: String? = "Client"
    ) {
        override fun toString(): String {
            return "Nulling(id=$id, nonnull='$nonnull', name=$name, price=$price, date=$date, category=$category)"
        }
    }

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
}