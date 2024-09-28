package com.example.datamodel.employees

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
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
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
data class EmployeesNullable(
    val id: Int? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var patronymic: String? = null,
    var login: String? = null,
    var password: String? = null,
    var phone: String? = null,
    var email: String? = null,
    var dateBirthday: LocalDateTime? = null,
    var dateWorkIn: LocalDateTime? = null,
    var dateWorkOut: LocalDateTime? = null,
    var imageProfileLink: String? = null,
    var position: String? = null,
    var description: String? = null
)

/**
 * Список работников.
 */
@Serializable
@KomapperEntity
@KomapperTable("tbl_employees")
data class Employees(
    /**
     * Идентификатор сотрудника в БД (заполняется автоматически)
     */
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "employee_id")
    val id: Int = 0,
    /**
     * Имя сотрудника (обязательно к заполнению)
     */
    var firstName: String = "",
    /**
     * Фамилия сотрудника (обязательно к заполнению)
     */
    var lastName: String = "",
    /**
     * Отчество сотрудника (при наличии)
     */
    var patronymic: String = "",
    /**
     * Логин сотрудника (обязательно к заполнению)
     */
    var login: String = "",
    /**
     * Пароль в личный кабинет сотрудника (обязательно к заполнению)
     */
    var password: String = "",
    /**
     * Мобильный телефон сотрудника
     */
    var phone: String = "",
    /**
     * Почта сотрудника
     */
    var email: String = "",
    /**
     * Дата рождения сотрудника (вида "2000-01-01T00:00")
     */
    var dateBirthday: LocalDateTime = LocalDateTime.nullDatetime(),
    /**
     * Дата принятия на работу сотрудника (вида "2000-01-01T00:00")
     */
    var dateWorkIn: LocalDateTime = LocalDateTime.nullDatetime(),
    /**
     * Дата увольнения сотрудника (вида "2000-01-01T00:00")
     */
    var dateWorkOut: LocalDateTime = LocalDateTime.nullDatetime(),
    /**
     * Ссылка на картинку работника (аватарка)
     */
    var imageProfileLink: String = "",
    /**
     * Должность
     */
    var position: String = "",
    /**
     * Описание сотрудника
     */
    var description: String = "",
    /**
     * Версия обновлений записи сотрудника (заполняется автоматически)
     */
    @KomapperVersion
    val version: Int = 0,
    /**
     * Дата создания записи (заполняется автоматически)
     */
    val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntBaseDataImpl<Employees>() {

    companion object {
        val tbl_employees = Meta.employees
    }

    /**
     * Задаётся условие [declaration] для поиска записей по нему в БД.
     * @return нашлась ли хотя бы одна запись с заданным условием.
     */
    private suspend fun isDuplicate(declaration: WhereDeclaration): Boolean {
        return db.runQuery { QueryDsl.from(tbl_employees).where(declaration).select(count()) } != 0L
    }

    /**
     * Метод на получение всех записей [Employees]
     *
     * <b>Sample usage</b>
     * ```
     * GET : /employees/all
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
     * Метод на получение записи [Employees] по параметру [id]
     *
     * <b>Sample usage</b>
     * ```
     * GET : /employees/1
     * ```
     *
     * <b>Return codes</b>
     *
     * 400 при ошибке входящего параметра [id]
     *
     * 404 если запись [Employees] по запрашиваемоу [id] не найдена в БД
     *
     * @return 200 при успешной отправке ответа
     */
    override suspend fun getId(call: ApplicationCall): ResultResponse {
        return super.getId(call)
    }

    /**
     * Метод на создание записи класса [Employees]
     *
     * <b>Return codes</b>
     *
     * 431 Необходимо указать Имя [Employees.firstName]
     *
     * 432 Необходимо указать Фамилию [Employees.lastName]
     *
     * 433 Необходимо указать Логин [Employees.login]
     *
     * 434 Необходимо указать Пароль [Employees.password]
     *
     * 435 Сотрудник с указанным логином уже существует в БД
     *
     * 400 при возникновении необработанной ошибки
     *
     * @return 201 при успешном создании объекта
     */
    override suspend fun post(call: ApplicationCall): ResultResponse {
        try {
            val newEmployee = call.receive<Employees>()

            if (newEmployee.firstName.isBlank())
                return ResultResponse.Error(HttpStatusCode(431, "firstName must be selected"), "Необходимо указать Имя сотрудника")

            if (newEmployee.lastName.isBlank())
                return ResultResponse.Error(HttpStatusCode(432, "lastName must be selected"), "Необходимо указать Фамилию сотрудника")

            if (newEmployee.login.isBlank())
                return ResultResponse.Error(HttpStatusCode(433, "login must be selected"), "Необходимо указать Логин сотрудника")

            if (newEmployee.password.isBlank())
                return ResultResponse.Error(HttpStatusCode(434, "password must be selected"), "Необходимо указать Пароль сотрудника")

            if (newEmployee.isDuplicate { tbl_employees.login eq newEmployee.login })
                return ResultResponse.Error(HttpStatusCode(435, "Login already exists"), "Сотрудник с логином ${newEmployee.login} уже существует")

            val finish = newEmployee.create(null).result
            return ResultResponse.Success(HttpStatusCode.Created, "Successfully created Employee with id ${finish.id}")
        } catch (e: Exception) {
            return ResultResponse.Error(HttpStatusCode.BadRequest, e.localizedMessage)
        }
    }

    /**
     * Метод на обновление объекта записи [Employees] по параметру [id]
     *
     * Обязательно наличие поля [id] в запросе. Обновление записи происходит по нему.
     * Далее передавайте только те поля "ключ:значение" - которые необходимо обновить.
     * Другие поля которые вы не указывали в запросе - обновлены не будут
     *
     * <b>Return codes</b>
     *
     * 400 при ошибке входящего параметра [id]
     *
     * 404 если запись [Employees] по запрашиваемоу [id] не найдена в БД
     *
     * 400 при возникновении необработанной ошибки
     *
     * @return 200 при успешном обновлении объекта
     */
    override suspend fun update(call: ApplicationCall, kclass: KClass<*>): ResultResponse {
        return super.update(call, kclass)
    }

    /**
     * Метод на удаление объекта записи [Employees] по параметру [id]
     *
     * <b>Sample usage</b>
     * ```
     * DELETE : /employees?id=1
     * ```
     *
     * <b>Return codes</b>
     *
     * 400 при ошибке входящего параметра [id]
     *
     * 404 если запись [Employees] по запрашиваемоу [id] не найдена в БД
     *
     * 400 при возникновении необработанной ошибки
     *
     * @return 204 при успешном удалении объекта
     */
    override suspend fun delete(call: ApplicationCall): ResultResponse {
        return this.delete(call)
    }
}