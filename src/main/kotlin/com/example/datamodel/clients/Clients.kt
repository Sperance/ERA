package com.example.datamodel.clients

import com.example.CommentField
import com.example.currectDatetime
import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.getData
import com.example.datamodel.getDataOne
import com.example.datamodel.isDuplicate
import com.example.datamodel.records.Records
import com.example.datamodel.records.Records.Companion.tbl_records
import com.example.isNullOrZero
import com.example.nullDatetime
import com.example.toDateTimePossible
import com.example.toIntPossible
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.util.toLocalDateTime
import io.ktor.util.InternalAPI
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable
import org.komapper.annotation.KomapperVersion
import org.komapper.core.dsl.Meta
import java.util.Date

/**
 * Список клиентов.
 */
@Serializable
@KomapperEntity
@KomapperTable("tbl_clients")
data class Clients(
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "client_id")
    val id: Int = 0,
    @CommentField("Имя клиента", true)
    var firstName: String? = null,
    @CommentField("Фамилия клиента", true)
    var lastName: String? = null,
    @CommentField("Отчество клиента", false)
    var patronymic: String? = null,
    @CommentField("Логин от личного кабинета", true)
    var login: String? = null,
    @CommentField("Пароль от личного кабинета", true)
    var password: String? = null,
    @CommentField("Контактный телефон", true)
    var phone: String? = null,
    @CommentField("Почтовый адрес", true)
    var email: String? = null,
    @CommentField("Дата рождения", false)
    var dateBirthday: LocalDateTime? = null,
    @CommentField("Дата принятия на работу сотрудника", false)
    var dateWorkIn: LocalDateTime? = null,
    @CommentField("Дата увольнения сотрудника", false)
    var dateWorkOut: LocalDateTime? = null,
    @CommentField("Должность сотрудника", false)
    var position: String? = null,
    @CommentField("Описание сотрудника", false)
    var description: String? = null,
    @CommentField("Тип клиента", false)
    var clientType: String? = null,
    @CommentField("Пол клиента", true)
    var gender: Byte? = null,
    @Transient
    @KomapperVersion
    val version: Int = 0,
    @Transient
    val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntBaseDataImpl<Clients>() {

    companion object {
        val tbl_clients = Meta.clients
    }

    suspend fun getSlots(call: ApplicationCall): ResultResponse {
        try {
            val _id = call.parameters["id"]
            val _data = call.parameters["data"]

            if (_id == null || !_id.toIntPossible())
                return ResultResponse.Error(HttpStatusCode(431, ""), "Incorrect parameter 'id'($_id). This parameter must be 'Int' type")
            if (_data.isNullOrEmpty())
                return ResultResponse.Error(HttpStatusCode(432, ""), "Необходимо указать дату")
            if (!_data.toDateTimePossible())
                return ResultResponse.Error(HttpStatusCode(433, ""), "Неверный формат даты")

            val id = _id.toInt()
            val data = LocalDateTime.parse(_data)

            val dateStart = LocalDateTime(data.year, data.monthNumber, data.dayOfMonth, 0, 0, 0)
            val dateEnd = LocalDateTime(data.year, data.monthNumber, data.dayOfMonth, 23, 59, 0)

            val currentDayRecords = Records().getData({ tbl_records.id_client_to eq id ; tbl_records.dateRecord.between(dateStart..dateEnd) })

            return ResultResponse.Success(HttpStatusCode.OK, currentDayRecords)
        } catch (e: Exception) {
            return ResultResponse.Error(HttpStatusCode.Conflict, e.localizedMessage)
        }
    }

    suspend fun getFromType(call: ApplicationCall): ResultResponse {
        try {
            val clientType = call.parameters["clientType"]

            if (clientType.isNullOrEmpty())
                return ResultResponse.Error(HttpStatusCode(431, ""), "Необходимо указать Тип клиента")

            return ResultResponse.Success(HttpStatusCode.OK, getData({ tbl_clients.clientType eq clientType}))
        } catch (e: Exception) {
            return ResultResponse.Error(HttpStatusCode.Conflict, e.localizedMessage)
        }
    }

    suspend fun auth(call: ApplicationCall): ResultResponse {
        try {
            val user = call.receive<Clients>()

            if (user.login.isNullOrEmpty())
                return ResultResponse.Error(HttpStatusCode(431, ""), "Необходимо указать Логин")

            if (user.password.isNullOrEmpty())
                return ResultResponse.Error(HttpStatusCode(432, ""), "Необходимо указать Пароль")

            val client = getDataOne({ tbl_clients.login eq user.login ; tbl_clients.password eq user.password})
            if (client == null)
                return ResultResponse.Error(HttpStatusCode.NotFound, "Не найден пользователь с указанным Логином и Паролем")

            return ResultResponse.Success(HttpStatusCode.OK, client)
        } catch (e: Exception) {
            return ResultResponse.Error(HttpStatusCode.Conflict, e.localizedMessage)
        }
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<Clients>): ResultResponse {
        params.checkings.add { CheckObj(it.firstName.isNullOrEmpty(), 431, "Необходимо указать Имя") }
        params.checkings.add { CheckObj(it.lastName.isNullOrEmpty(), 432, "Необходимо указать Фамилию") }
        params.checkings.add { CheckObj(it.phone.isNullOrEmpty(), 433, "Необходимо указать Телефон") }
        params.checkings.add { CheckObj(it.login.isNullOrEmpty(), 434, "Необходимо указать Логин") }
        params.checkings.add { CheckObj(it.password.isNullOrEmpty(), 435, "Необходимо указать Пароль") }
        params.checkings.add { CheckObj(it.email.isNullOrEmpty(), 436, "Необходимо указать Email") }
        params.checkings.add { CheckObj(it.gender.isNullOrZero(), 437, "Необходимо указать Пол") }
        params.checkings.add { CheckObj(it.isDuplicate { tbl_clients.login eq it.login }, 441, "Клиент с указанным Логином уже существует") }
        params.checkings.add { CheckObj(it.isDuplicate { tbl_clients.phone eq it.phone }, 442, "Клиент с указанным Номером телефона уже существует") }
        params.checkings.add { CheckObj(it.isDuplicate { tbl_clients.email eq it.email }, 443, "Клиент с указанным Почтовым адресом уже существует") }

        params.defaults.add { it::dateBirthday to LocalDateTime.nullDatetime() }
        params.defaults.add { it::dateWorkIn to LocalDateTime.nullDatetime() }
        params.defaults.add { it::dateWorkOut to LocalDateTime.nullDatetime() }

        return super.post(call, params)
    }
}