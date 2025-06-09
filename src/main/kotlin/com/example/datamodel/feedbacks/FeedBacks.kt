package com.example.datamodel.feedbacks

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

/**
 * Список отзывов о сотрудниках
 */
@Serializable
@KomapperEntity
@KomapperTable("tbl_feedbacks")
data class FeedBacks(
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "feedback_id")
    override val id: Int = 0,
    @CommentField("Имя клиента оставившего отзыв")
    var first_name: String? = null,
    @CommentField("Фамилия клиента оставившего отзыв")
    var last_name: String? = null,
    @CommentField("Идентификатор клиента оставившего отзыв")
    var id_client_from: Int? = null,
    @CommentField("Идентификатор сотрудника кому оставили отзыв")
    var id_employee_to: Int? = null,
    @CommentField("Текст отзыва")
    var text: String? = null,
    @CommentField("Поставленная оценка сотруднику")
    var value: Byte? = null,
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
) : IntBaseDataImpl<FeedBacks>() {

    companion object {
        val tbl_feedbacks = Meta.feedBacks
    }

    override fun getTable() = tbl_feedbacks
    override fun isValidLine(): Boolean {
        return id_client_from != null && id_employee_to != null
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<FeedBacks>, serializer: KSerializer<List<FeedBacks>>): ResultResponse {
        params.checkings.add { FeedBacksErrors.ERROR_TEXT.toCheckObj(it) }
        params.checkings.add { FeedBacksErrors.ERROR_VALUE.toCheckObj(it) }
        params.checkings.add { FeedBacksErrors.ERROR_IDCLIENTFROM.toCheckObj(it) }
        params.checkings.add { FeedBacksErrors.ERROR_IDEMPLOYEETO.toCheckObj(it) }
        params.checkings.add { FeedBacksErrors.ERROR_VALUE_LOW0.toCheckObj(it) }
        params.checkings.add { FeedBacksErrors.ERROR_VALUE_GREAT10.toCheckObj(it) }
        params.checkings.add { FeedBacksErrors.ERROR_IDCLIENTFROM_DUPLICATE.toCheckObj(it) }
        params.checkings.add { FeedBacksErrors.ERROR_IDEMPLOYEETO_DUPLICATE.toCheckObj(it) }

        params.defaults.add { it::value to 0.toByte() }

        return super.post(call, params, serializer)
    }

    override suspend fun update(call: ApplicationCall, params: RequestParams<FeedBacks>, serializer: KSerializer<FeedBacks>): ResultResponse {
        params.checkings.add { FeedBacksErrors.ERROR_IDCLIENTFROM_DUPLICATE_NOTNULL.toCheckObj(it) }
        params.checkings.add { FeedBacksErrors.ERROR_IDEMPLOYEETO_DUPLICATE_NOTNULL.toCheckObj(it) }
        params.checkings.add { FeedBacksErrors.ERROR_VALUE_LOW0_NOTNULL.toCheckObj(it) }
        params.checkings.add { FeedBacksErrors.ERROR_VALUE_GREAT10_NOTNULL.toCheckObj(it) }

        return super.update(call, params, serializer)
    }
}

