package com.example.datamodel.services

import com.example.CommentField
import com.example.currectDatetime
import com.example.datamodel.BaseRepository
import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.catalogs.Catalogs
import com.example.datamodel.catalogs.Catalogs.Companion.tbl_catalogs
import com.example.datamodel.getFromId
import com.example.datamodel.isDuplicate
import com.example.datamodel.records.Records
import com.example.datamodel.stockfiles.Stockfiles
import com.example.datamodel.update
import com.example.isNullOrZero
import io.ktor.server.application.ApplicationCall
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "services_id")
    val id: Int = 0,
    @CommentField("Наименование услуги", true)
    var name: String? = null,
    @CommentField("Описание услуги", false)
    var description: String? = null,
    @CommentField("Категория услуги", true)
    var category: Int? = null,
    @CommentField("Минимальная стоимость", true)
    var priceLow: Double? = null,
    @CommentField("Максимальная стоимость", false)
    var priceMax: Double? = null,
    @CommentField("Продолжительность услуги (1 пункт = 15 мин, т.е. если услуга длится 60 мин, то необходимо указать (60 / 15) = 4 пункта)", true)
    var duration: Byte? = null,
    @CommentField("К какому полу относится услуга (по умолчанию -1 (к любому полу))", false)
    var gender: Byte? = null,
    @CommentField("Ссылка на изображение услуги", false)
    var imageLink: String? = null,
    @Transient
    var imageFormat: String? = null,
    @Transient
    @KomapperVersion
    val version: Int = 0,
    @CommentField("Дата создания строки", false)
    val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntBaseDataImpl<Services>() {

    companion object {
        val tbl_services = Meta.services
        val repo_services = BaseRepository(Services())
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<Services>, serializer: KSerializer<List<Services>>): ResultResponse {
        params.checkings.add { CheckObj(it.name.isNullOrEmpty(), 431, "Необходимо указать Наименование услуги") }
        params.checkings.add { CheckObj(it.priceLow.isNullOrZero(), 432, "Необходимо указать Минимальную стоимость услуги") }
        params.checkings.add { CheckObj(it.duration.isNullOrZero(), 435, "Необходимо указать Продолжительность услуги (не может быть 0)") }
        params.checkings.add { CheckObj((it.priceLow != null && it.priceMax != null) && (it.priceMax!! < it.priceLow!!), 436, "Максимальная стоимость услуги(${it.priceMax}) не может быть меньше минимальной(${it.priceLow})") }
        params.checkings.add { CheckObj(!Catalogs.repo_catalogs.isHaveData(it.category), 441, "Не найдена Категория с id ${it.category}") }

        params.defaults.add { it::gender to -1 }

        return super.post(call, params, serializer)
    }

    override suspend fun update(call: ApplicationCall, params: RequestParams<Services>, serializer: KSerializer<Services>): ResultResponse {
        params.checkings.add { CheckObj(it.category != null && !Catalogs.repo_catalogs.isHaveData(it.category), 441, "Не найдена Категория с id ${it.category}") }

        return super.update(call, params, serializer)
    }

    override suspend fun delete(call: ApplicationCall, params: RequestParams<Services>): ResultResponse {
        params.onBeforeCompleted = { index ->
            Records.repo_records.clearLinkEqual(Records::id_service, index)
            Stockfiles.repo_stockfiles.clearLinkEqual(Stockfiles::service, index)
        }

        return super.delete(call, params)
    }
}