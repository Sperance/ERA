package com.example.datamodel.clients

import com.example.currectDatetime
import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.getDataOne
import com.example.datamodel.isDuplicate
import com.example.isNullOrZero
import com.example.nullDatetime
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
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
 * Список клиентов.
 */
@Serializable
@KomapperEntity
@KomapperTable("tbl_clients")
data class Clients(
    /**
     * Идентификатор клиента в БД (заполняется автоматически)
     */
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "client_id")
    val id: Int = 0,
    /**
     * Имя клиента (обязательно к заполнению)
     */
    val firstName: String? = null,
    /**
     * Фамилия клиента (обязательно к заполнению)
     */
    val lastName: String? = null,
    /**
     * Отчество клиента (при наличии)
     */
    val patronymic: String? = null,
    /**
     * Логин от личного кабинета клиента (обязательно к заполнению)
     */
    val login: String? = null,
    /**
     * Пароль от личного кабинета клиента
     */
    val password: String? = null,
    /**
     * Контактный телефон клиента (обязательно к заполнению)
     */
    val phone: String? = null,
    /**
     * Почта клиента
     */
    val email: String? = null,
    /**
     * Дата рождения клиента (вида "2000-01-01T00:00")
     */
    val dateBirthday: LocalDateTime? = null,
    /**
     * Дата принятия на работу сотрудника (вида "2000-01-01T00:00")
     */
    val dateWorkIn: LocalDateTime? = null,
    /**
     * Дата увольнения сотрудника (вида "2000-01-01T00:00")
     */
    val dateWorkOut: LocalDateTime? = null,
    /**
     * Должность
     */
    val position: String? = null,
    /**
     * Описание сотрудника
     */
    val description: String? = null,
    /**
     * Тип клиента
     */
    val clientType: String? = null,
    /**
     * Пол клиента (0 - Мужской, 1 - Женский) (обязательно к заполнению)
     */
    val gender: Byte? = null,
    /**
     * Версия обновлений записи клиента (заполняется автоматически)
     */
    @KomapperVersion
    val version: Int = 0,
    /**
     * Дата создания записи (заполняется автоматически)
     */
    val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntBaseDataImpl<Clients>() {

    companion object {
        val tbl_clients = Meta.clients
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

    override suspend fun post(call: ApplicationCall, checkings: ArrayList<suspend (Clients) -> CheckObj>): ResultResponse {
        checkings.add { CheckObj(it.firstName.isNullOrEmpty(), 431, "Необходимо указать Имя") }
        checkings.add { CheckObj(it.lastName.isNullOrEmpty(), 432, "Необходимо указать Фамилию") }
        checkings.add { CheckObj(it.phone.isNullOrEmpty(), 433, "Необходимо указать Телефон") }
        checkings.add { CheckObj(it.login.isNullOrEmpty(), 434, "Необходимо указать Логин") }
        checkings.add { CheckObj(it.password.isNullOrEmpty(), 435, "Необходимо указать Пароль") }
        checkings.add { CheckObj(it.email.isNullOrEmpty(), 436, "Необходимо указать Email") }
        checkings.add { CheckObj(it.gender.isNullOrZero(), 437, "Необходимо указать Пол") }
        checkings.add { CheckObj(it.isDuplicate { tbl_clients.login eq it.login }, 441, "Клиент с указанным Логином уже существует") }
        checkings.add { CheckObj(it.isDuplicate { tbl_clients.phone eq it.phone }, 442, "Клиент с указанным Номером телефона уже существует") }
        checkings.add { CheckObj(it.isDuplicate { tbl_clients.email eq it.email }, 443, "Клиент с указанным Почтовым адресом уже существует") }
        return super.post(call, checkings)
    }
}