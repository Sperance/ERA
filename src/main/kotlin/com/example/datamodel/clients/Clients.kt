package com.example.datamodel.clients

import com.example.plugins.db
import kotlinx.serialization.Serializable
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable
import org.komapper.annotation.KomapperVersion
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.operator.count

@Serializable
@KomapperEntity
@KomapperTable("tbl_clients")
data class Clients(
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "client_id")
    val id: Int = 0,

    /**
     * Имя клиента
     */
    var name: String = "",
    /**
     * Фамилия клиента
     */
    var surname: String = "",
    /**
     * Отчество клиента (при наличии)
     */
    var patronymic: String = "",
    /**
     * Логин от личного кабинета клиента
     */
    var login: String = "",
    /**
     * Пароль от личного кабинета клиента
     */
    var password: String = "",
    /**
     * Контактный телефон клиента
     */
    var phone: String = "",
    /**
     * Дата рождения клиента
     */
    var dateBirthday: Long = 0L,

    @KomapperVersion
    val version: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
) {

    companion object {
        val tbl_clients = Meta.clients
    }

    suspend fun isDuplicate(declaration: WhereDeclaration): Boolean {
        return db.runQuery { QueryDsl.from(tbl_clients).where(declaration).select(count()) } == 0L
    }
}