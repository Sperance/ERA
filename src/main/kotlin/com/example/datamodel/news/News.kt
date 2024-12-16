package com.example.datamodel.news

import com.example.CommentField
import com.example.currectDatetime
import com.example.datamodel.BaseRepository
import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
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

@Serializable
@KomapperEntity
@KomapperTable("tbl_news")
data class News(
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "news_id")
    val id: Int = 0,
    @CommentField("Наименование новости", true)
    var name: String? = null,
    @CommentField("Текст новости", true)
    var mainText: String? = null,
    @CommentField("Прямая ссылка на картинку", false)
    var imageLink: String? = null,
    @Transient
    var imageFormat: String? = null,
    @Transient
    @KomapperVersion
    val version: Int = 0,
    @CommentField("Дата создания строки", false)
    val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntBaseDataImpl<News>() {

    companion object {
        val tbl_news = Meta.news
        val repo_news = BaseRepository(News())
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<News>, serializer: KSerializer<News>): ResultResponse {
        params.checkings.add { CheckObj(it.name.isNullOrEmpty(), 431, "Необходимо указать Наименование новости (name)") }
        params.checkings.add { CheckObj(it.mainText.isNullOrEmpty(), 432, "Необходимо указать Текст новости (mainText)") }
        return super.post(call, params, serializer)
    }
}