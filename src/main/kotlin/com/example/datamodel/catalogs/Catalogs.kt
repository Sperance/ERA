package com.example.datamodel.catalogs

import com.example.helpers.CommentField
import com.example.currectDatetime
import com.example.basemodel.BaseRepository
import com.example.basemodel.CheckObj
import com.example.basemodel.IntBaseDataImpl
import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
import com.example.datamodel.clients.Clients
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
import org.komapper.core.dsl.metamodel.EntityMetamodel

/**
 * Справочник информации
 */
@Serializable
@KomapperEntity
@KomapperTable("tbl_catalogs")
data class Catalogs(
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "catalogs_id")
    override val id: Int = 0,
    @CommentField("Тип категории")
    var type: String? = null,
    @CommentField("Категория")
    var category: String? = null,
    @CommentField("Значение")
    var value: String? = null,
    @CommentField("Описание")
    var description: String? = null,
    @Transient
    @KomapperVersion
    override val version: Int = 0,
    @Transient
    @CommentField("Дата создания строки")
    override val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntBaseDataImpl<Catalogs>() {

    companion object {
        val tbl_catalogs = Meta.catalogs
        val repo_catalogs = BaseRepository(Catalogs())
    }

    override fun getTable() = tbl_catalogs
    override fun getRepository() = repo_catalogs
    override fun baseParams(): RequestParams<Catalogs> {
        val params = RequestParams<Catalogs>()
        params.checkings.add { CheckObj(repo_catalogs.getRepositoryData().find { fin -> fin.category == it.category && fin.value == it.value } != null, EnumHttpCode.DUPLICATE, 201, "В БД уже присутствует категория '${it.category}' со значнеием '${it.value}'") }
        return params
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<Catalogs>, serializer: KSerializer<List<Catalogs>>): ResultResponse {
        params.checkings.add { CheckObj(it.type.isNullOrEmpty(), EnumHttpCode.INCORRECT_PARAMETER, 301, "Необходимо указать Тип категории(type) для элемента") }
        params.checkings.add { CheckObj(it.category.isNullOrEmpty(), EnumHttpCode.INCORRECT_PARAMETER, 302, "Необходимо указать Категорию(category) для элемента") }
        params.checkings.add { CheckObj(it.value.isNullOrEmpty(), EnumHttpCode.INCORRECT_PARAMETER, 303, "Необходимо указать Значение(value) элемента") }

        return super.post(call, params, serializer)
    }

    override suspend fun delete(call: ApplicationCall, params: RequestParams<Catalogs>): ResultResponse {
        params.onBeforeCompleted = { obj ->
            Clients.repo_clients.clearLinkEqual(Clients::position, obj.id)
            Clients.repo_clients.clearLinkEqualArray(Clients::arrayTypeWork, obj.id)
            Services.repo_services.clearLinkEqual(Services::category, obj.id)
        }

        return super.delete(call, params)
    }
}