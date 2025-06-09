package com.example.datamodel.stockfiles

import com.example.helpers.CommentField
import com.example.currectDatetime
import com.example.basemodel.IntBaseDataImpl
import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
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
import org.komapper.annotation.KomapperUpdatedAt
import org.komapper.annotation.KomapperVersion
import org.komapper.core.dsl.Meta

@Serializable
@KomapperEntity
@KomapperTable("tbl_stockfiles")
data class Stockfiles(
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "stockfiles_id")
    override val id: Int = 0,
    @CommentField("Ссылка на услугу")
    var service: Int? = null,
    @CommentField("Наименование файла")
    var name: String? = null,
    @CommentField("Категория файла")
    var category: String? = null,
    @CommentField("Ссылка на файл")
    var image_link: String? = null,
    @Transient
    var image_format: String? = null,
    @Transient
    @KomapperVersion
    override val version: Int = 0,
    @Transient
    @CommentField("Дата создания строки")
    override val created_at: LocalDateTime = LocalDateTime.currectDatetime(),
    @Transient
    @KomapperUpdatedAt
    override val updated_at: LocalDateTime = LocalDateTime.currectDatetime(),
    @Transient
    override val deleted: Boolean = false
) : IntBaseDataImpl<Stockfiles>() {

    companion object {
        val tbl_stockfiles = Meta.stockfiles
    }

    override fun getTable() = tbl_stockfiles
    override fun isValidLine(): Boolean {
        return service != null && image_link != null && image_format != null
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<Stockfiles>, serializer: KSerializer<List<Stockfiles>>): ResultResponse {
        params.isNeedFile = true
        params.checkings.add { StockfilesErrors.ERROR_CATEGORY.toCheckObj(it) }
        params.checkings.add { StockfilesErrors.ERROR_SERVICE_DUPLICATE_NOTNULL.toCheckObj(it) }

        return super.post(call, params, serializer)
    }

    override suspend fun update(call: ApplicationCall, params: RequestParams<Stockfiles>, serializer: KSerializer<Stockfiles>): ResultResponse {
        params.checkings.add { StockfilesErrors.ERROR_SERVICE_DUPLICATE_NOTNULL.toCheckObj(it) }

        return super.update(call, params, serializer)
    }
}