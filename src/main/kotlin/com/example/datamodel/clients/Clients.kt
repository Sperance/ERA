package com.example.datamodel.clients

import com.example.currectDatetime
import com.example.datamodel.IntBaseData
import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.create
import com.example.datamodel.delete
import com.example.datamodel.getData
import com.example.datamodel.getDataOne
import com.example.datamodel.update
import com.example.nullDatetime
import com.example.plugins.db
import com.example.toIntPossible
import com.example.updateFromNullable
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.request.receiveText
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable
import org.komapper.annotation.KomapperVersion
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.operator.count
import kotlin.reflect.KClass

@Serializable
@Suppress
data class ClientsNullbale(
    val id: Int? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var patronymic: String? = null,
    var login: String? = null,
    var password: String? = null,
    var phone: String? = null,
    var email: String? = null,
    var dateBirthday: LocalDateTime? = null,
    var gender: Byte? = null
)

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
    var firstName: String = "",
    /**
     * Фамилия клиента (обязательно к заполнению)
     */
    var lastName: String = "",
    /**
     * Отчество клиента (при наличии)
     */
    var patronymic: String = "",
    /**
     * Логин от личного кабинета клиента (обязательно к заполнению)
     */
    var login: String = "",
    /**
     * Пароль от личного кабинета клиента
     */
    var password: String = "",
    /**
     * Контактный телефон клиента (обязательно к заполнению)
     */
    var phone: String = "",
    /**
     * Почта клиента
     */
    var email: String = "",
    /**
     * Дата рождения клиента (вида "2000-01-01T00:00")
     */
    var dateBirthday: LocalDateTime = LocalDateTime.nullDatetime(),
    /**
     * Пол клиента (0 - Мужской, 1 - Женский) (обязательно к заполнению)
     */
    var gender: Byte = -1,
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

    /**
     * Задаётся условие [declaration] для поиска записей по нему в БД.
     * @return нашлась ли хотя бы одна запись с заданным условием.
     */
    private suspend fun isDuplicate(declaration: WhereDeclaration): Boolean {
        return db.runQuery { QueryDsl.from(tbl_clients).where(declaration).select(count()) } != 0L
    }

    /**
     * Метод на получение всех записей [Clients]
     *
     * <b>Sample usage</b>
     * ```
     * GET : /clients/all
     * ```
     *
     * <b>Return codes</b>
     *
     * 400 при возникновении необработанной ошибки
     *
     * @return 200 при успешной отправке ответа
     */
    override suspend fun get(call: ApplicationCall): ResultResponse {
        return super.get(call)
    }

    /**
     * Метод на получение записи [Clients] по параметру [id]
     *
     * <b>Sample usage</b>
     * ```
     * GET : /clients/1
     * ```
     *
     * <b>Return codes</b>
     *
     * 400 при ошибке входящего параметра [id]
     *
     * 404 если запись [Clients] по запрашиваемоу [id] не найдена в БД
     *
     * @return 200 при успешной отправке ответа
     */
    override suspend fun getId(call: ApplicationCall): ResultResponse {
        return super.getId(call)
    }

    suspend fun auth(call: ApplicationCall): ResultResponse {
        try {
            val user = call.receive<ClientsNullbale>()

            if (user.login.isNullOrBlank())
                return ResultResponse.Error(HttpStatusCode(401, "1"), "Incorrect parameter 'login'")

            if (user.password.isNullOrBlank())
                return ResultResponse.Error(HttpStatusCode(402, "2"), "Incorrect parameter 'password'")

            val client = getDataOne({ tbl_clients.login eq user.login ; tbl_clients.password eq user.password})
            if (client == null)
                return ResultResponse.Error(HttpStatusCode.NotFound, "Такого пользователя не существует")

            return ResultResponse.Success(HttpStatusCode.OK, client)
        } catch (e: Exception) {
            return ResultResponse.Error(HttpStatusCode.Conflict, e.localizedMessage)
        }
    }

    /**
     * Метод на создание записи класса [Clients]
     *
     * <b>Return codes</b>
     *
     * 433 Необходимо указать Имя пользователя [Clients.firstName]
     *
     * 434 Необходимо указать Фамилию пользователя [Clients.lastName]
     *
     * 435 Необходимо указать Телефон пользователя [Clients.phone]
     *
     * 436 Необходимо указать Логин пользователя [Clients.login]
     *
     * 437 Необходимо указать Пол пользователя [Clients.gender]
     *
     * 431 Клиент с логином [Clients.login] уже существует
     *
     * 432 Клиент с номером телефона [Clients.phone] уже существует
     *
     * 400 при возникновении необработанной ошибки
     *
     * @return 201 при успешном создании объекта
     */
    override suspend fun post(call: ApplicationCall): ResultResponse {
        try {
            val newUser = call.receive<Clients>()

            if (newUser.firstName.isBlank())
                return ResultResponse.Error(HttpStatusCode(433, "firstName must be selected"), "Необходимо указать Имя пользователя")

            if (newUser.lastName.isBlank())
                return ResultResponse.Error(HttpStatusCode(434, "lastName must be selected"), "Необходимо указать Фамилию пользователя")

            if (newUser.phone.isBlank())
                return ResultResponse.Error(HttpStatusCode(435, "phone must be selected"), "Необходимо указать Телефон пользователя")

            if (newUser.login.isBlank())
                return ResultResponse.Error(HttpStatusCode(436, "login must be selected"), "Необходимо указать Логин пользователя")

            if (newUser.gender == (-1).toByte())
                return ResultResponse.Error(HttpStatusCode(437, "gender must be selected"), "Необходимо указать Пол пользователя")

            if (newUser.isDuplicate { tbl_clients.login eq newUser.login })
                return ResultResponse.Error(HttpStatusCode(431, "Login already exists"), "Клиент с логином ${newUser.login} уже существует")

            if (newUser.isDuplicate { tbl_clients.phone eq newUser.phone })
                return ResultResponse.Error(HttpStatusCode(432, "Phone already exists"), "Клиент с номером телефона ${newUser.phone} уже существует")

            val created = newUser.create(null).result
            return ResultResponse.Success(HttpStatusCode.Created, "Successfully created Client with id ${created.id}")
        } catch (e: Exception) {
            return ResultResponse.Error(HttpStatusCode.BadRequest, e.localizedMessage)
        }
    }

    /**
     * Метод на обновление объекта записи [Clients] по параметру [id]
     *
     * Обязательно наличие поля [id] в запросе. Обновление записи происходит по нему.
     * Далее передавайте только те поля "ключ:значение" - которые необходимо обновить.
     * Другие поля которые вы не указывали в запросе - обновлены не будут
     *
     * <b>Return codes</b>
     *
     * 400 при ошибке входящего параметра [id]
     *
     * 404 если запись [Clients] по запрашиваемоу [id] не найдена в БД
     *
     * 400 при возникновении необработанной ошибки
     *
     * @return 200 при успешном обновлении объекта
     */
    override suspend fun update(call: ApplicationCall, kclass: KClass<*>): ResultResponse {
        return super.update(call, kclass)
    }

    /**
     * Метод на удаление объекта записи [Clients] по параметру [id]
     *
     * <b>Sample usage</b>
     * ```
     * DELETE : /clients?id=1
     * ```
     *
     * <b>Return codes</b>
     *
     * 400 при ошибке входящего параметра [id]
     *
     * 404 если запись [Clients] по запрашиваемоу [id] не найдена в БД
     *
     * 400 при возникновении необработанной ошибки
     *
     * @return 204 при успешном удалении объекта
     */
     override suspend fun delete(call: ApplicationCall): ResultResponse {
        return super.delete(call)
    }
}