package com.example.datamodel.feedbacks

import com.example.helpers.CommentField
import com.example.currectDatetime
import com.example.datamodel.BaseRepository
import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.clients.Clients
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
    val id: Int = 0,
    @CommentField("Имя клиента оставившего отзыв", true)
    var firstName: String? = null,
    @CommentField("Фамилия клиента оставившего отзыв", true)
    var lastName: String? = null,
    @CommentField("Идентификатор клиента оставившего отзыв", true)
    var id_client_from: Int? = null,
    @CommentField("Идентификатор сотрудника кому оставили отзыв", true)
    var id_client_to: Int? = null,
    @CommentField("Текст отзыва", true)
    var text: String? = null,
    @CommentField("Поставленная оценка сотруднику", true)
    var value: Byte? = null,
    @Transient
    @KomapperVersion
    val version: Int = 0,
    @Transient
    @CommentField("Дата создания строки", false)
    val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntBaseDataImpl<FeedBacks>() {

    companion object {
        val tbl_feedbacks = Meta.feedBacks
        val repo_feedbacks = BaseRepository(FeedBacks())
    }

    override fun getBaseId() = id
    override fun baseParams(): RequestParams<FeedBacks> {
        val params = RequestParams<FeedBacks>()
        params.checkings.add { CheckObj(it.id_client_from != null && !Clients.repo_clients.isHaveData(it.id_client_from!!), 441, "Не существует Клиента с id ${it.id_client_from}") }
        params.checkings.add { CheckObj(it.id_client_to != null && !Clients.repo_clients.isHaveData(it.id_client_to!!), 442, "Не существует Клиента с id ${it.id_client_to}") }
        return params
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<FeedBacks>, serializer: KSerializer<List<FeedBacks>>): ResultResponse {
        params.checkings.add { CheckObj(it.text.isNullOrEmpty(), 431, "Необходимо указать Текст отзыва") }
        params.checkings.add { CheckObj(it.value.isNullOrZero(), 432, "Необходимо указать Оценку отзыва") }
        params.checkings.add { CheckObj(it.id_client_from.isNullOrZero(), 433, "Необходимо указать id Клиента который оставляет отзыв") }
        params.checkings.add { CheckObj(it.id_client_to.isNullOrZero(), 434, "Необходимо указать id Клиента, которому составляется отзыв") }
        params.checkings.add { CheckObj(it.value!! < 0, 435, "Оценка не может быть меньше 0") }
        params.checkings.add { CheckObj(it.value!! > 10, 435, "Оценка не может быть больше 10") }
        params.checkings.add { CheckObj(!Clients.repo_clients.isHaveData(it.id_client_from!!), 441, "Не существует Клиента с id ${it.id_client_from}") }
        params.checkings.add { CheckObj(!Clients.repo_clients.isHaveData(it.id_client_to!!), 442, "Не существует Клиента с id ${it.id_client_to}") }

        params.defaults.add { it::value to 0.toByte() }

        return super.post(call, params, serializer)
    }
}

