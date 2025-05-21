package com.example.datamodel.stockfiles

import com.example.helpers.CommentField
import com.example.currectDatetime
import com.example.basemodel.BaseRepository
import com.example.basemodel.CheckObj
import com.example.basemodel.IntBaseDataImpl
import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
import com.example.datamodel.services.Services
import com.example.enums.EnumHttpCode
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
    var imageLink: String? = null,
    @Transient
    var imageFormat: String? = null,
    @Transient
    @KomapperVersion
    override val version: Int = 0,
    @Transient
    @CommentField("Дата создания строки")
    override val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntBaseDataImpl<Stockfiles>() {

    companion object {
        val tbl_stockfiles = Meta.stockfiles
        val repo_stockfiles = BaseRepository(Stockfiles())
    }

    override fun getTable() = tbl_stockfiles
    override fun getRepository() = repo_stockfiles
    override fun isValidLine(): Boolean {
        return service != null && imageLink != null && imageFormat != null
    }
    override fun baseParams(): RequestParams<Stockfiles> {
        val params = RequestParams<Stockfiles>()
        params.checkings.add { CheckObj(it.service != null && !Services.repo_services.isHaveData(it.service), EnumHttpCode.NOT_FOUND, 201, "Не найдена Категория файла с id ${it.service}") }
        return params
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<Stockfiles>, serializer: KSerializer<List<Stockfiles>>): ResultResponse {
        params.isNeedFile = true
        params.checkings.add { CheckObj(it.category.isNullOrEmpty(), EnumHttpCode.INCORRECT_PARAMETER, 301, "Необходимо указать Категорию файла") }

        return super.post(call, params, serializer)
    }
}