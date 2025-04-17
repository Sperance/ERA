package com.example.datamodel.catalogs

import com.example.helpers.CommentField
import com.example.currectDatetime
import com.example.datamodel.BaseRepository
import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.clients.Clients
import com.example.datamodel.services.Services
import io.ktor.http.HttpStatusCode
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
 * Справочник информации
 */
@Serializable
@KomapperEntity
@KomapperTable("tbl_catalogs")
data class Catalogs(
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "catalogs_id")
    val id: Int = 0,
    @CommentField("Тип категории", true)
    var type: String? = null,
    @CommentField("Категория", true)
    var category: String? = null,
    @CommentField("Значение", true)
    var value: String? = null,
    @CommentField("Описание", false)
    var description: String? = null,
    @Transient
    @KomapperVersion
    val version: Int = 0,
    @Transient
    @CommentField("Дата создания строки", false)
    val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntBaseDataImpl<Catalogs>() {

    companion object {
        val tbl_catalogs = Meta.catalogs
        val repo_catalogs = BaseRepository(Catalogs())
    }

    override fun getBaseId() = id
    override fun baseParams(): RequestParams<Catalogs> {
        val params = RequestParams<Catalogs>()
        params.checkings.add { CheckObj(repo_catalogs.getRepositoryData().find { fin -> fin.category == it.category && fin.value == it.value } != null, 440, "В БД уже присутствует категория '${it.category}' со значнеием '${it.value}'") }
        return params
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<Catalogs>, serializer: KSerializer<List<Catalogs>>): ResultResponse {
        params.checkings.add { CheckObj(it.type.isNullOrEmpty(), 430, "Необходимо указать Тип категории(type) для элемента") }
        params.checkings.add { CheckObj(it.category.isNullOrEmpty(), 431, "Необходимо указать Категорию(category) для элемента") }
        params.checkings.add { CheckObj(it.value.isNullOrEmpty(), 432, "Необходимо указать Значение(value) элемента") }
        params.checkings.add { CheckObj(repo_catalogs.getRepositoryData().find { fin -> fin.category == it.category && fin.value == it.value } != null, 440, "В БД уже присутствует категория '${it.category}' со значнеием '${it.value}'") }

        return super.post(call, params, serializer)
    }

    override suspend fun delete(call: ApplicationCall, params: RequestParams<Catalogs>): ResultResponse {
        params.onBeforeCompleted = { obj ->
            Clients.repo_clients.clearLinkEqual(Clients::position, obj?.id)
            Clients.repo_clients.clearLinkEqualArray(Clients::arrayTypeWork, obj?.id)
            Services.repo_services.clearLinkEqual(Services::category, obj?.id)
        }

        return super.delete(call, params)
    }
}