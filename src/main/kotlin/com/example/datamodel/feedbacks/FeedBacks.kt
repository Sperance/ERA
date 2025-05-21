package com.example.datamodel.feedbacks

import com.example.helpers.CommentField
import com.example.currectDatetime
import com.example.basemodel.BaseRepository
import com.example.basemodel.CheckObj
import com.example.basemodel.IntBaseDataImpl
import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
import com.example.datamodel.clients.Clients
import com.example.datamodel.employees.Employees
import com.example.enums.EnumHttpCode
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
    override val id: Int = 0,
    @CommentField("Имя клиента оставившего отзыв")
    var firstName: String? = null,
    @CommentField("Фамилия клиента оставившего отзыв")
    var lastName: String? = null,
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
    override val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntBaseDataImpl<FeedBacks>() {

    companion object {
        val tbl_feedbacks = Meta.feedBacks
        val repo_feedbacks = BaseRepository(FeedBacks())
    }

    override fun getTable() = tbl_feedbacks
    override fun getRepository() = repo_feedbacks
    override fun isValidLine(): Boolean {
        return id_client_from != null && id_employee_to != null
    }
    override fun baseParams(): RequestParams<FeedBacks> {
        val params = RequestParams<FeedBacks>()
        params.checkings.add { CheckObj(it.id_client_from != null && !Clients.repo_clients.isHaveData(it.id_client_from!!), EnumHttpCode.NOT_FOUND, 201, "Не существует Клиента с id ${it.id_client_from}") }
        params.checkings.add { CheckObj(it.id_employee_to != null && !Employees.repo_employees.isHaveData(it.id_employee_to!!), EnumHttpCode.NOT_FOUND, 202, "Не существует Клиента с id ${it.id_employee_to}") }
        params.checkings.add { CheckObj(it.value != null && it.value!! < 0, EnumHttpCode.INCORRECT_PARAMETER, 203, "Оценка не может быть меньше 0") }
        params.checkings.add { CheckObj(it.value != null && it.value!! > 10, EnumHttpCode.INCORRECT_PARAMETER, 204, "Оценка не может быть больше 10") }
        return params
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<FeedBacks>, serializer: KSerializer<List<FeedBacks>>): ResultResponse {
        params.checkings.add { CheckObj(it.text.isNullOrEmpty(), EnumHttpCode.INCORRECT_PARAMETER, 301, "Необходимо указать Текст отзыва") }
        params.checkings.add { CheckObj(it.value.isNullOrZero(), EnumHttpCode.INCORRECT_PARAMETER, 302, "Необходимо указать Оценку отзыва") }
        params.checkings.add { CheckObj(it.id_client_from.isNullOrZero(), EnumHttpCode.INCORRECT_PARAMETER, 303, "Необходимо указать id Клиента который оставляет отзыв") }
        params.checkings.add { CheckObj(it.id_employee_to.isNullOrZero(), EnumHttpCode.INCORRECT_PARAMETER, 304, "Необходимо указать id Сотрудника, которому составляется отзыв") }
        params.checkings.add { CheckObj(it.value!! < 0, EnumHttpCode.INCORRECT_PARAMETER, 305, "Оценка не может быть меньше 0") }
        params.checkings.add { CheckObj(it.value!! > 10, EnumHttpCode.INCORRECT_PARAMETER, 306, "Оценка не может быть больше 10") }
        params.checkings.add { CheckObj(!Clients.repo_clients.isHaveData(it.id_client_from!!), EnumHttpCode.NOT_FOUND, 307, "Не существует Клиента с id ${it.id_client_from}") }
        params.checkings.add { CheckObj(!Employees.repo_employees.isHaveData(it.id_employee_to!!), EnumHttpCode.NOT_FOUND, 308, "Не существует Сотрудника с id ${it.id_employee_to}") }

        params.defaults.add { it::value to 0.toByte() }

        return super.post(call, params, serializer)
    }
}

