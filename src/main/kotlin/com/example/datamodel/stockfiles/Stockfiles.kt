package com.example.datamodel.stockfiles

import com.example.helpers.CommentField
import com.example.currectDatetime
import com.example.datamodel.BaseRepository
import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.services.Services
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
    val id: Int = 0,
    @CommentField("Ссылка на услугу", false)
    var service: Int? = null,
    @CommentField("Наименование файла", false)
    var name: String? = null,
    @CommentField("Категория файла", true)
    var category: String? = null,
    @CommentField("Ссылка на файл", false)
    var imageLink: String? = null,
    @Transient
    var imageFormat: String? = null,
    @Transient
    @KomapperVersion
    val version: Int = 0,
    @Transient
    @CommentField("Дата создания строки", false)
    val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntBaseDataImpl<Stockfiles>() {

    companion object {
        val tbl_stockfiles = Meta.stockfiles
        val repo_stockfiles = BaseRepository(Stockfiles())
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<Stockfiles>, serializer: KSerializer<List<Stockfiles>>): ResultResponse {
        params.isNeedFile = true
        params.checkings.add { CheckObj(it.category.isNullOrEmpty(), 431, "Необходимо указать Категорию файла") }
        params.checkings.add { CheckObj(it.service != null && !Services.repo_services.isHaveData(it.service), 441, "Необходимо указать Категорию файла") }

        return super.post(call, params, serializer)
    }

    override suspend fun update(call: ApplicationCall, params: RequestParams<Stockfiles>, serializer: KSerializer<Stockfiles>): ResultResponse {
        params.checkings.add { CheckObj(it.service != null && !Services.repo_services.isHaveData(it.service), 441, "Необходимо указать Категорию файла") }

        return super.update(call, params, serializer)
    }
}