package com.example.datamodel.services

import com.example.currectDatetime
import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.create
import com.example.datamodel.delete
import com.example.datamodel.getDataOne
import com.example.datamodel.update
import com.example.toIntPossible
import com.example.updateFromNullable
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable
import org.komapper.annotation.KomapperVersion
import org.komapper.core.dsl.Meta
import kotlin.reflect.KClass

//@Serializable
//@Suppress
//data class ServicesNullable(
//    val id: Int? = null,
//    var name: String? = null,
//    var description: String? = null,
//    var category: String? = null,
//    var priceLow: Double? = null,
//    var priceMax: Double? = null,
//    var duration: Byte? = null,
//    var gender: Byte? = null,
//    var imageLink: String? = null
//)

/**
 * Список услуг.
 */
@Serializable
@KomapperEntity
@KomapperTable("tbl_services")
data class Services(
    /**
     * Идентификатор услуги в БД (заполняется автоматически)
     */
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "services_id")
    val id: Int = 0,

    /**
     * Наименование услуги (обязательно к заполнению)
     */
    var name: String? = null,
    /**
     * Описание услуги
     */
    var description: String? = null,
    /**
     * Категория, к которой относится услуга
     */
    var category: String? = null,
    /**
     * Минимальная стоимость услуги в рублях
     */
    var priceLow: Double? = null,
    /**
     * Максимальная стоимость услуги в рублях
     */
    var priceMax: Double? = null,
    /**
     * Продолжительность услуги (1 пункт = 15 мин, например если услуга длится 60 мин - то поле в будет 4)
     */
    var duration: Byte? = null,
    /**
     * К какому полу относится услуга ("-1" - к любому, "0" - к мужскому, "1" - к женскому) [по умолчанию -1]
     */
    var gender: Byte? = null,
    /**
     * Ссылка на изображение услуги
     */
    var imageLink: String? = null,
    /**
     * Версия обновлений записи услуги (заполняется автоматически)
     */
    @KomapperVersion
    val version: Int = 0,
    /**
     * Дата создания услуги (заполняется автоматически)
     */
    val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntBaseDataImpl<Services>() {

    companion object {
        val tbl_services = Meta.services
    }

    override suspend fun post(call: ApplicationCall, checkings: ArrayList<suspend (Services) -> CheckObj>): ResultResponse {
        checkings.add { CheckObj(it.name.isNullOrEmpty(), 431, "Необходимо указать Наименование услуги") }
        checkings.add { CheckObj((it.priceLow ?: 0.0) < 1, 432, "Необходимо указать Минимальную стоимость услуги") }
        checkings.add { CheckObj((it.priceMax ?: 0.0 )< 1, 433, "Необходимо указать Максимальную стоимость услуги") }
        checkings.add { CheckObj(it.category.isNullOrEmpty(), 434, "Необходимо указать Категорию услуги") }
        checkings.add { CheckObj(it.duration == 0.toByte(), 435, "Необходимо указать Продолжительность услуги") }
        return super.post(call, checkings)
    }
}