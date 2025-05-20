package com.example.datamodel.news

import com.example.helpers.CommentField
import com.example.currectDatetime
import com.example.basemodel.BaseRepository
import com.example.basemodel.CheckObj
import com.example.basemodel.IntBaseDataImpl
import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
import com.example.datamodel.clientsschelude.ClientsSchelude
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

@Serializable
@KomapperEntity
@KomapperTable("tbl_news")
data class News(
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "news_id")
    override val id: Int = 0,
    @CommentField("Наименование новости")
    var name: String? = null,
    @CommentField("Текст новости")
    var mainText: String? = null,
    @CommentField("Прямая ссылка на картинку")
    var imageLink: String? = null,
    @Transient
    var imageFormat: String? = null,
    @Transient
    @KomapperVersion
    override val version: Int = 0,
    @CommentField("Дата создания строки")
    override val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntBaseDataImpl<News>() {

    companion object {
        val tbl_news = Meta.news
        val repo_news = BaseRepository(News())
    }

    override fun getTable() = tbl_news
    override fun getRepository() = repo_news
    override fun baseParams(): RequestParams<News> {
        val params = RequestParams<News>()
        params.checkings.add { CheckObj(it.name.isNullOrEmpty(), EnumHttpCode.INCORRECT_PARAMETER, 201, "Необходимо указать Наименование новости (name)") }
        return params
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<News>, serializer: KSerializer<List<News>>): ResultResponse {
        params.checkings.add { CheckObj(it.mainText.isNullOrEmpty(), EnumHttpCode.INCORRECT_PARAMETER, 301, "Необходимо указать Текст новости (mainText)") }
        return super.post(call, params, serializer)
    }
}