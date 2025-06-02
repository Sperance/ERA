package com.example.datamodel.services

import com.example.helpers.CommentField
import com.example.currectDatetime
import com.example.basemodel.BaseRepository
import com.example.basemodel.IntBaseDataImpl
import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
import com.example.datamodel.records.Records
import com.example.datamodel.stockfiles.Stockfiles
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
    override val id: Int = 0,
    @CommentField("Наименование услуги")
    var name: String? = null,
    @CommentField("Описание услуги")
    var description: String? = null,
    @CommentField("Категория услуги")
    var category: Int? = null,
    @CommentField("Минимальная стоимость")
    var priceLow: Double? = null,
    @CommentField("Максимальная стоимость")
    var priceMax: Double? = null,
    @CommentField("Продолжительность услуги (1 пункт = 15 мин, т.е. если услуга длится 60 мин, то необходимо указать (60 / 15) = 4 пункта)")
    var duration: Byte? = null,
    @CommentField("К какому полу относится услуга (по умолчанию -1 (к любому полу))")
    var gender: Byte? = null,
    @CommentField("Ссылка на изображение услуги")
    var imageLink: String? = null,
    @Transient
    var imageFormat: String? = null,
    @Transient
    @KomapperVersion
    override val version: Int = 0,
    @Transient
    @CommentField("Дата создания строки")
    override val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
    @Transient
    override val deleted: Boolean = false
) : IntBaseDataImpl<Services>() {

    companion object {
        val tbl_services = Meta.services
        val repo_services = BaseRepository(Services())
    }

    override fun getTable() = tbl_services
    override fun getRepository() = repo_services
    override fun isValidLine(): Boolean {
        return name != null && category != null
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<Services>, serializer: KSerializer<List<Services>>): ResultResponse {
        params.checkings.add { ServicesErrors.ERROR_NAME.toCheckObj(it) }
        params.checkings.add { ServicesErrors.ERROR_PRICELOW.toCheckObj(it) }
        params.checkings.add { ServicesErrors.ERROR_DURATION.toCheckObj(it) }
        params.checkings.add { ServicesErrors.ERROR_CATEGORY.toCheckObj(it) }
        params.checkings.add { ServicesErrors.ERROR_PRICELOWMAX_NOTNULL.toCheckObj(it) }
        params.checkings.add { ServicesErrors.ERROR_CATEGORY_DUPLICATE.toCheckObj(it) }

        params.defaults.add { it::gender to -1 }

        return super.post(call, params, serializer)
    }

    override suspend fun update(call: ApplicationCall, params: RequestParams<Services>, serializer: KSerializer<Services>): ResultResponse {
        params.checkings.add { ServicesErrors.ERROR_PRICELOWMAX_NOTNULL.toCheckObj(it) }
        params.checkings.add { ServicesErrors.ERROR_CATEGORY_DUPLICATE_NOTNULL.toCheckObj(it) }

        return super.update(call, params, serializer)
    }

    override suspend fun delete(call: ApplicationCall, params: RequestParams<Services>): ResultResponse {
        params.onBeforeCompleted = { obj ->
            Records.repo_records.clearLinkEqual(Records::id_service, obj.id)
            Stockfiles.repo_stockfiles.clearLinkEqual(Stockfiles::service, obj.id)
        }

        return super.delete(call, params)
    }
}