package com.example.datamodel.feedbacks

import com.example.currectDatetime
import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.clients.Clients
import com.example.datamodel.isHaveData
import com.example.isNullOrZero
import io.ktor.server.application.ApplicationCall
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
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
    /**
     * Идентификатор услуги в БД (заполняется автоматически)
     */
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "feedback_id")
    val id: Int = 0,
    /**
     * Идентификатор клиента оставившего отзыв
     */
    var id_client_from: Int? = null,
    /**
     * Идентификатор сотрудника кому оставили отзыв
     */
    var id_client_to: Int? = null,
    /**
     * Сам текст отзыва
     */
    var text: String? = null,
    /**
     * Оценка для сотрудника по отзыву
     */
    var value: Byte? = null,
    /**
     * Версия обновлений записи услуги (заполняется автоматически)
     */
    @KomapperVersion
    val version: Int = 0,
    /**
     * Дата создания услуги (заполняется автоматически)
     */
    val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntBaseDataImpl<FeedBacks>() {

    companion object {
        val tbl_feedbacks = Meta.feedBacks
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<FeedBacks>): ResultResponse {
        params.checkings.add { CheckObj(it.text.isNullOrEmpty(), 431, "Необходимо указать Текст отзыва") }
        params.checkings.add { CheckObj(it.value.isNullOrZero(), 432, "Необходимо указать Оценку отзыва") }
        params.checkings.add { CheckObj(it.id_client_from.isNullOrZero(), 433, "Необходимо указать id Клиента который оставляет отзыв") }
        params.checkings.add { CheckObj(it.id_client_to.isNullOrZero(), 434, "Необходимо указать id Клиента, которому составляется отзыв") }
        params.checkings.add { CheckObj(it.value!! < 0, 435, "Оценка не может быть меньше 0") }
        params.checkings.add { CheckObj(Clients().isHaveData(it.id_client_from!!), 435, "Не существует Клиента с id ${it.id_client_from}") }
        params.checkings.add { CheckObj(Clients().isHaveData(it.id_client_to!!), 436, "Не существует Клиента с id ${it.id_client_to}") }

        params.defaults.add { it::value to 0.toByte() }

        return super.post(call, params)
    }
}