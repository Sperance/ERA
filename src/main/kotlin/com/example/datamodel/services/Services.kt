package com.example.datamodel.services

import com.example.currectDatetime
import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.clients.Clients
import com.example.isNullOrZero
import io.ktor.server.application.ApplicationCall
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable
import org.komapper.annotation.KomapperVersion
import org.komapper.core.dsl.Meta

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

    override suspend fun post(call: ApplicationCall, params: RequestParams<Services>): ResultResponse {
        params.checkings.add { CheckObj(it.name.isNullOrEmpty(), 431, "Необходимо указать Наименование услуги") }
        params.checkings.add { CheckObj(it.priceLow.isNullOrZero(), 432, "Необходимо указать Минимальную стоимость услуги") }
        params.checkings.add { CheckObj(it.duration.isNullOrZero(), 435, "Необходимо указать Продолжительность услуги (не может быть 0)") }
        params.checkings.add { CheckObj((it.priceLow != null && it.priceMax != null) && (it.priceMax!! < it.priceLow!!), 436, "Максимальная стоимость услуги(${it.priceMax}) не может быть меньше минимальной(${it.priceLow})") }

        params.defaults.add { it::priceMax to it.priceLow }
        params.defaults.add { it::gender to -1 }

        return super.post(call, params)
    }
}