package com.example.datamodel.employees

import com.example.setToken
import com.example.basemodel.BaseRepository
import com.example.basemodel.IntBaseDataImpl
import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
import com.example.currectDatetime
import com.example.currentZeroDate
import com.example.datamodel.authentications.Authentications
import com.example.datamodel.authentications.Authentications.Companion.tbl_authentications
import com.example.datamodel.clients.Clients.Companion.repo_clients
import com.example.datamodel.clients.ClientsErrors
import com.example.datamodel.clientsschelude.ClientsSchelude
import com.example.datamodel.clientsschelude.ClientsSchelude.Companion.repo_clientsschelude
import com.example.datamodel.feedbacks.FeedBacks
import com.example.datamodel.records.Records
import com.example.datamodel.records.Records.Companion.repo_records
import com.example.datamodel.records.Records.Companion.tbl_records
import com.example.datamodel.services.Services.Companion.repo_services
import com.example.enums.EnumBearerRoles
import com.example.generateMapError
import com.example.helpers.CommentField
import com.example.helpers.delete
import com.example.helpers.getData
import com.example.helpers.getDataOne
import com.example.helpers.getField
import com.example.helpers.getSize
import com.example.helpers.haveField
import com.example.helpers.putField
import com.example.logging.DailyLogger.printTextLog
import com.example.minus
import com.example.plus
import com.example.security.AESEncryption
import com.example.security.generateSalt
import com.example.security.hashString
import com.example.security.verifyPassword
import com.example.toDateTimePossible
import com.example.toIntPossible
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.util.date.GMTDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
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
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

@Serializable
@KomapperEntity
@KomapperTable("tbl_employees")
data class Employees(
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "employee_id")
    override val id: Int = 0,
    @CommentField("Имя")
    var firstName: String? = null,
    @CommentField("Фамилия")
    var lastName: String? = null,
    @CommentField("Отчество")
    var patronymic: String? = null,
    @CommentField("Логин от личного кабинета")
    var login: String? = null,
    @CommentField("Пароль от личного кабинета")
    var password: String? = null,
    @CommentField("Контактный телефон")
    var phone: String? = null,
    @CommentField("Почтовый адрес")
    var email: String? = null,
    @CommentField("Дата рождения")
    var dateBirthday: LocalDateTime? = null,
    @CommentField("Дата принятия на работу")
    var dateWorkIn: LocalDateTime? = null,
    @CommentField("Дата увольнения")
    var dateWorkOut: LocalDateTime? = null,
    @CommentField("Должность")
    var position: Int? = null,
    @CommentField("Описание")
    var description: String? = null,
    @CommentField("Роль сотрудника")
    var role: String? = null,
    @CommentField("Пол сотрудника")
    var gender: Byte? = null,
    @CommentField("Прямая ссылка на картинку")
    var imageLink: String? = null,
    @Transient
    var imageFormat: String? = null,
    @CommentField("Массив ссылок на работы сотрудника")
    var arrayTypeWork: Array<Int>? = null,
    @Transient
    var salt: String? = null,
    @Transient
    @KomapperVersion
    override val version: Int = 0,
    @Transient
    @CommentField("Дата создания строки")
    override val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
    @Transient
    override val deleted: Boolean = false
) : IntBaseDataImpl<Employees>() {

    companion object {
        val tbl_employees = Meta.employees
        val repo_employees = BaseRepository(Employees())
    }

    override fun getRepository() = repo_employees
    override fun getTable() = tbl_employees
    override fun isValidLine(): Boolean {
        return firstName != null && login != null && password != null && role != null && salt != null
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<Employees>, serializer: KSerializer<List<Employees>>): ResultResponse {
        params.checkings.add { EmployeesErrors.ERROR_FIRSTNAME.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_LASTNAME.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_PHONE.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_EMAIL.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_GENDER.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_LOGIN.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_ROLE.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_PHONE_DUPLICATE.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_EMAIL_DUPLICATE.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_LOGIN_DUPLICATE.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_POSITION_DUPLICATE_NOTNULL.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_ARRAYTYPEWORK_DUPLICATE_NOTNULL.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_SALT_NOTNULL.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_ROLE_ENUM.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_ROLE_ADMIN.toCheckObj(it) }

        params.onBeforeCompleted = { obj ->
            val size = Employees().getSize()
            if (obj.lastName != null) obj.lastName = AESEncryption.encrypt(obj.lastName)
            if (obj.firstName != null) obj.firstName = AESEncryption.encrypt(obj.firstName)
            if (obj.patronymic != null) obj.patronymic = AESEncryption.encrypt(obj.patronymic)
            if (obj.email != null) obj.email = AESEncryption.encrypt(obj.email)
            if (obj.phone != null) obj.phone = AESEncryption.encrypt(obj.phone)
            if (obj.password != null) obj.setNewPassword(obj.password!!)
            if (obj.role != null) obj.role = AESEncryption.encrypt(obj.role + "_" + size)
        }

        params.defaults.add { it::salt to generateSalt() }

        return super.post(call, params, serializer)
    }

    override suspend fun update(call: ApplicationCall, params: RequestParams<Employees>, serializer: KSerializer<Employees>): ResultResponse {
        params.checkings.add { EmployeesErrors.ERROR_POSITION_DUPLICATE_NOTNULL.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_ARRAYTYPEWORK_DUPLICATE_NOTNULL.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_SALT_NOTNULL.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_LOGIN_NOTNULL.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_ROLE_NOTNULL.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_PHONE_NOTNULL.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_EMAIL_DUPLICATE_NOTNULL.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_ROLE_ADMIN_NOTNULL.toCheckObj(it) }

        params.onBeforeCompleted = { obj ->
            if (obj.lastName != null) obj.lastName = AESEncryption.encrypt(obj.lastName)
            if (obj.firstName != null) obj.firstName = AESEncryption.encrypt(obj.firstName)
            if (obj.patronymic != null) obj.patronymic = AESEncryption.encrypt(obj.patronymic)
            if (obj.email != null) obj.email = AESEncryption.encrypt(obj.email)
            if (obj.phone != null) obj.phone = AESEncryption.encrypt(obj.phone)
            if (obj.password != null) obj.setNewPassword(obj.password!!)
            if (obj.role != null) obj.role = AESEncryption.encrypt(obj.role + "_" + obj.id)
        }

        params.checkOnUpdate = { finded, new ->
            if (new.haveField("salt") && finded.haveField("salt")) {
                new.putField("salt", finded.getField("salt"))
            }
            if (new.role != null && EnumBearerRoles.getFromNameOrNull(finded.role) != EnumBearerRoles.getFromNameOrNull(new.role)) {
                printTextLog("[Employees::update] Обновление Роли у сотрудника id: ${finded.id} логин: ${finded.login}")
                val token = Authentications.getTokenFromEmployee(finded)
                token?.delete()
            }
        }
        return super.update(call, params, serializer)
    }

    override suspend fun delete(call: ApplicationCall, params: RequestParams<Employees>): ResultResponse {
        params.onBeforeCompleted = { obj ->
            Records.repo_records.clearLinkEqual(Records::id_employee_to, obj.id)
            ClientsSchelude.repo_clientsschelude.clearLinkEqual(ClientsSchelude::idEmployee, obj.id)
            FeedBacks.repo_feedbacks.clearLinkEqual(FeedBacks::id_employee_to, obj.id)

            val token = Authentications.getTokenFromEmployee(obj)
            token?.delete()
            true
        }

        return super.delete(call, params)
    }

    suspend fun getByRole(call: ApplicationCall): ResultResponse {
        try {
            val _role = call.parameters["role"]

            if (_role.isNullOrEmpty()) return EmployeesErrors.ERROR_ROLE.toResultResponse(call, this)
            val role = EnumBearerRoles.getFromNameOrNull(_role.uppercase())
            if (role == null) return EmployeesErrors.ERROR_ROLE_ENUM.toResultResponse(call, this)

            return ResultResponse.Success(repo_employees.getRepositoryData().filter { it.getRoleAsEnum() == role })
        } catch (e: Exception) {
            return ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage))
        }
    }

    suspend fun auth(call: ApplicationCall): ResultResponse {
        try {
            val user = call.receive<Employees>()

            if (user.login.isNullOrEmpty())
                return EmployeesErrors.ERROR_LOGIN.toResultResponse(call, this)

            if (user.password.isNullOrEmpty())
                return EmployeesErrors.ERROR_PASSWORD.toResultResponse(call, this)

            val employee = repo_employees.getRepositoryData().find { it.login?.lowercase() == user.login?.lowercase() && verifyPassword(it.password, it.salt, user.password) }
            if (employee == null)
                return EmployeesErrors.ERROR_LOGINPASSWORD.toResultResponse(call, this)

            var token = Authentications.getTokenFromEmployee(employee)
            printTextLog("[Employees::auth] token: $token")
            if (token == null) {
                token = Authentications.createToken(employee.id, true, employee.getRoleAsEnum(), call)
            } else {
                if (token.isExpires()) {
                    printTextLog("[Employees] Токен просрочен. Удаляем и создаём новый")
                    token.delete()
                    token = Authentications.createToken(employee.id, true, employee.getRoleAsEnum(), call)
                }
            }

            call.response.setToken(token.token?:"", GMTDate(token.dateExpired!!.toInstant(TimeZone.UTC).toEpochMilliseconds()))

            return ResultResponse.Success(employee)
        } catch (e: Exception) {
            e.printStackTrace()
            return ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage))
        }
    }

    private suspend fun _getTimeSlots(employeeId: Int, servceLength: Int) : ArrayList<LocalDateTime> {

        val daysLoaded = 30
        val startDate = LocalDateTime.currentZeroDate()
        val endDate = startDate.plus((daysLoaded).days)

        val currentRecords = repo_records.getRepositoryData().filter { it.id_employee_to == employeeId && it.dateRecord!! in startDate..endDate && it.status!! <= 100 }
        val currentSheludes = repo_clientsschelude.getRepositoryData().filter { it.idEmployee == employeeId }
        val allServices = repo_services.getRepositoryData()

        val stockPeriod = 30
        val removePeriodMin = 1
        val blockSlots = servceLength * stockPeriod

        val arrayClosed = ArrayList<ClosedRange<LocalDateTime>>()
        currentRecords.filter { it.dateRecord!! in startDate..endDate }.forEach {
            val servLen = allServices.find { serv -> serv.id == it.id_service }!!
            val servDateBegin = it.dateRecord!!.minus((blockSlots - removePeriodMin).minutes)
            val servDateEnd = it.dateRecord!!.plus((servLen.duration!! * stockPeriod - removePeriodMin).minutes)
            arrayClosed.add(servDateBegin..servDateEnd)
        }

        var stockDate = startDate
        val maxDateTime = endDate.minus((blockSlots - removePeriodMin).minutes)
        val araResult = ArrayList<LocalDateTime>()
        while (true) {
            if (stockDate >= maxDateTime) break
            val finded = arrayClosed.find { ar -> stockDate in ar }
            val sheludeDate = currentSheludes.find { she -> she.scheludeDateStart!!.dayOfMonth == stockDate.dayOfMonth }
            if (sheludeDate != null && finded == null && stockDate in sheludeDate.scheludeDateStart!!..sheludeDate.scheludeDateEnd!!) {
                if (stockDate.plus(blockSlots.minutes) <= sheludeDate.scheludeDateEnd!!)
                    araResult.add(stockDate)
            }
            stockDate = stockDate.plus((stockPeriod).minutes)
        }
        return araResult
    }

    suspend fun getTimeSlots(call: ApplicationCall): ResultResponse {
        try {
            val _clientId = call.parameters["clientId"]
            val _servceLength = call.parameters["servceLength"]

            if (_clientId == null || !_clientId.toIntPossible()) return EmployeesErrors.ERROR_CLIENTID_PARAMETER.toResultResponse(call, this)
            if (_servceLength == null || !_servceLength.toIntPossible()) return EmployeesErrors.ERROR_SERVICELENGTH_PARAMETER.toResultResponse(call, this)

            return ResultResponse.Success(_getTimeSlots(_clientId.toInt(), _servceLength.toInt()))
        } catch (e: Exception) {
            return ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage))
        }
    }

    suspend fun getSlots(call: ApplicationCall): ResultResponse {
        try {
            val _id = call.parameters["id"]
            val _data = call.parameters["data"]

            if (_id == null || !_id.toIntPossible())
                return EmployeesErrors.ERROR_ID_PARAMETER.toResultResponse(call, this)
            if (_data.isNullOrEmpty())
                return EmployeesErrors.ERROR_DATA_PARAMETER.toResultResponse(call, this)
            if (!_data.toDateTimePossible())
                return EmployeesErrors.ERROR_DATA_INCORRECT_PARAMETER.toResultResponse(call, this)

            val id = _id.toInt()
            val data = LocalDateTime.parse(_data)

            val dateStart = LocalDateTime(data.year, data.monthNumber, data.dayOfMonth, 0, 0, 0)
            val dateEnd = LocalDateTime(data.year, data.monthNumber, data.dayOfMonth, 23, 59, 0)

            val currentDayRecords = repo_records.getRepositoryData().filter { it.id_employee_to == id && it.dateRecord!! in dateStart..dateEnd }

            return ResultResponse.Success(currentDayRecords)
        } catch (e: Exception) {
            return ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage))
        }
    }

    suspend fun onExitSite(call: ApplicationCall): ResultResponse {
        try {
            val id = call.parameters["id"]
            if (id.isNullOrEmpty())
                return EmployeesErrors.ERROR_ID_PARAMETER.toResultResponse(call, this)

            if (!id.toIntPossible())
                return EmployeesErrors.ERROR_ID_NOT_INT_PARAMETER.toResultResponse(call, this)

            val findedClient = repo_clients.getDataFromId(id.toIntOrNull())
            if (findedClient == null)
                return EmployeesErrors.ERROR_ID_DONTFIND.toResultResponse(call, this)

            val key = Authentications().getDataOne({ tbl_authentications.clientId eq id.toIntOrNull() ; tbl_authentications.employee eq true })
            if (key == null) {
                return EmployeesErrors.ERROR_LOGINKEY_DONTFIND.toResultResponse(call, this)
            }
            key.delete()

            return ResultResponse.Success("")
        } catch (e: Exception) {
            return ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage))
        }
    }

    fun getRoleAsEnum() : EnumBearerRoles {
        return EnumBearerRoles.getFromName(role)
    }

    fun setNewPassword(newPass: String) {
        password = hashString(newPass, salt!!)
    }

    override fun toString(): String {
        return "Employees(login=$login, id=$id, firstName=$firstName, lastName=$lastName, password=$password, phone=$phone, email=$email, position=$position, role=$role, imageLink=$imageLink, imageFormat=$imageFormat, arrayTypeWork=${arrayTypeWork?.contentToString()})"
    }
}
