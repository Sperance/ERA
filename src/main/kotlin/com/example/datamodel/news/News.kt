package com.example.datamodel.news

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
@KomapperTable("tbl_news")
data class News(
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "news_id")
    override val id: Int = 0,
    @CommentField("Наименование новости")
    var name: String? = null,
    @CommentField("Текст новости")
    var main_text: String? = null,
    @CommentField("Прямая ссылка на картинку")
    var image_link: String? = null,
    @Transient
    var image_format: String? = null,
    @CommentField("Дата для новости")
    var date_news: LocalDateTime? = null,
    @Transient
    @KomapperVersion
    override val version: Int = 0,
    @CommentField("Дата создания строки")
    override val created_at: LocalDateTime = LocalDateTime.currectDatetime(),
    @Transient
    @KomapperUpdatedAt
    override val updated_at: LocalDateTime = LocalDateTime.currectDatetime(),
    @Transient
    override val deleted: Boolean = false
) : IntBaseDataImpl<News>() {

    companion object {
        val tbl_news = Meta.news
    }

    override fun getTable() = tbl_news
    override fun isValidLine(): Boolean {
        return name != null
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<News>, serializer: KSerializer<List<News>>): ResultResponse {
        params.checkings.add { NewsErrors.ERROR_MAINTEXT.toCheckObj(it) }
        params.checkings.add { NewsErrors.ERROR_NAME.toCheckObj(it) }

        params.defaults.add { it::date_news to LocalDateTime.currectDatetime() }

        return super.post(call, params, serializer)
    }
}