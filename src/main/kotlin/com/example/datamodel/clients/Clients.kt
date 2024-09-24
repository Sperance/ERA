package com.example.datamodel.clients

import com.example.currectDatetime
import com.example.nullDatetime
import com.example.plugins.db
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
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

/**
 * Список клиентов.
 */
@Serializable
@KomapperEntity
@KomapperTable("tbl_clients")
data class Clients(
    /**
     * Идентификатор клиента в БД (заполняется автоматически)
     */
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "client_id")
    val id: Int = 0,
    /**
     * Имя клиента (обязательно к заполнению)
     */
    var firstName: String = "",
    /**
     * Фамилия клиента (обязательно к заполнению)
     */
    var lastName: String = "",
    /**
     * Отчество клиента (при наличии)
     */
    var patronymic: String = "",
    /**
     * Логин от личного кабинета клиента (обязательно к заполнению)
     */
    var login: String = "",
    /**
     * Пароль от личного кабинета клиента
     */
    var password: String = "",
    /**
     * Контактный телефон клиента (обязательно к заполнению)
     */
    var phone: String = "",
    /**
     * Дата рождения клиента (вида "2000-01-01T00:00")
     */
    var dateBirthday: LocalDateTime = LocalDateTime.nullDatetime(),
    /**
     * Пол клиента (0 - Мужской, 1 - Женский) (обязательно к заполнению)
     */
    var gender: Byte = -1,
    /**
     * Версия обновлений записи клиента (заполняется автоматически)
     */
    @KomapperVersion
    val version: Int = 0,
    /**
     * Дата создания записи (заполняется автоматически)
     */
    val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) {

    companion object {
        val tbl_clients = Meta.clients
    }

    /**
     * Задаётся условие [declaration] для поиска записей по нему в БД.
     * @return нашлась ли хотя бы одна запись с заданным условием.
     */
    suspend fun isDuplicate(declaration: WhereDeclaration): Boolean {
        return db.runQuery { QueryDsl.from(tbl_clients).where(declaration).select(count()) } != 0L
    }
}