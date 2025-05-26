package com.example.datamodel.employees

import com.example.setToken
import com.example.basemodel.BaseRepository
import com.example.basemodel.CheckObj
import com.example.basemodel.IntBaseDataImpl
import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
import com.example.currectDatetime
import com.example.currentZeroDate
import com.example.datamodel.authentications.Authentications
import com.example.datamodel.catalogs.Catalogs
import com.example.datamodel.clientsschelude.ClientsSchelude
import com.example.datamodel.feedbacks.FeedBacks
import com.example.datamodel.records.Records
import com.example.datamodel.records.Records.Companion.tbl_records
import com.example.datamodel.services.Services.Companion.repo_services
import com.example.enums.EnumBearerRoles
import com.example.enums.EnumHttpCode
import com.example.generateMapError
import com.example.helpers.CommentField
import com.example.helpers.delete
import com.example.helpers.getData
import com.example.helpers.getField
import com.example.helpers.getSize
import com.example.helpers.haveField
import com.example.helpers.putField
import com.example.isNullOrZero
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
        params.checkings.add { CheckObj(it.firstName.isNullOrEmpty(), EnumHttpCode.INCORRECT_PARAMETER, 301, "Необходимо указать Имя") }
        params.checkings.add { CheckObj(it.lastName.isNullOrEmpty(), EnumHttpCode.INCORRECT_PARAMETER, 302, "Необходимо указать Фамилию") }
        params.checkings.add { CheckObj(it.phone.isNullOrEmpty(), EnumHttpCode.INCORRECT_PARAMETER, 303, "Необходимо указать Телефон") }
        params.checkings.add { CheckObj(it.email.isNullOrEmpty(), EnumHttpCode.INCORRECT_PARAMETER, 304, "Необходимо указать Email") }
        params.checkings.add { CheckObj(it.gender.isNullOrZero(), EnumHttpCode.INCORRECT_PARAMETER, 305, "Необходимо указать Пол") }
        params.checkings.add { CheckObj(it.login.isNullOrEmpty(), EnumHttpCode.INCORRECT_PARAMETER, 306, "Необходимо указать login") }
        params.checkings.add { CheckObj(it.role.isNullOrEmpty(), EnumHttpCode.INCORRECT_PARAMETER, 307, "Необходимо указать Роль") }
        params.checkings.add { CheckObj(repo_employees.isHaveDataField(Employees::phone, it.phone), EnumHttpCode.DUPLICATE, 308, "Сотрудник с указанным Номером телефона уже существует") }
        params.checkings.add { CheckObj(repo_employees.isHaveDataField(Employees::email, it.email), EnumHttpCode.DUPLICATE, 309, "Сотрудник с указанным Почтовым адресом уже существует") }
        params.checkings.add { CheckObj(repo_employees.isHaveDataField(Employees::login, it.login), EnumHttpCode.DUPLICATE, 310, "Сотрудник с указанным Логином уже существует") }
        params.checkings.add { CheckObj(it.position != null && !Catalogs.repo_catalogs.isHaveData(it.position), EnumHttpCode.NOT_FOUND, 311, "Не найдена Должность с id ${it.position}") }
        params.checkings.add { CheckObj(it.arrayTypeWork != null && !Catalogs.repo_catalogs.isHaveData(it.arrayTypeWork?.toList()), EnumHttpCode.NOT_FOUND, 312, "Не найдены Категории с arrayTypeWork ${it.arrayTypeWork?.joinToString()}") }
        params.checkings.add { CheckObj(it.salt != null, EnumHttpCode.BAD_REQUEST, 313, "Попытка модификации системных данных. Информация о запросе передана Администраторам") }
        params.checkings.add { CheckObj(repo_employees.isHaveDataField(Employees::login, it.login), EnumHttpCode.DUPLICATE, 314, "Сотрудник с указанным Логином уже существует") }
        params.checkings.add { CheckObj(EnumBearerRoles.getFromNameOrNull(it.role) == null, EnumHttpCode.INCORRECT_PARAMETER, 315, "Роль Сотрудника 'role - ${it.role}' не соответствует одному из доступных: ${EnumBearerRoles.entries.joinToString { role -> role.name }}") }

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
        params.checkings.add { CheckObj(it.position != null && !Catalogs.repo_catalogs.isHaveData(it.position), EnumHttpCode.NOT_FOUND, 301, "Не найдена Должность с id ${it.position}") }
        params.checkings.add { CheckObj(it.arrayTypeWork != null && !Catalogs.repo_catalogs.isHaveData(it.arrayTypeWork?.toList()), EnumHttpCode.NOT_FOUND, 302, "Не найдены Категории с arrayTypeWork ${it.arrayTypeWork?.joinToString()}") }
        params.checkings.add { CheckObj(it.salt != null, EnumHttpCode.BAD_REQUEST, 303, "Попытка модификации системных данных. Информация о запросе передана Администраторам") }
        params.checkings.add { CheckObj(it.login != null && repo_employees.isHaveDataField(Employees::login, it.login), EnumHttpCode.DUPLICATE, 304, "Сотрудник с указанным Логином уже существует") }
        params.checkings.add { CheckObj(it.role != null && EnumBearerRoles.getFromNameOrNull(it.role) == null, EnumHttpCode.INCORRECT_PARAMETER, 305, "Роль Сотрудника 'role - ${it.role}' не соответствует одному из доступных: ${EnumBearerRoles.entries.joinToString { role -> role.name }}") }

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

        params.checkOnUpdate = { finded, new ->
            if (new.haveField("salt") && finded.haveField("salt")) {
                new.putField("salt", finded.getField("salt"))
            }
            if (new.password != null) new.setNewPassword(new.password!!)
            if (new.role != null && EnumBearerRoles.getFromNameOrNull(finded.role) != EnumBearerRoles.getFromNameOrNull(new.role)) {
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

            if (_role.isNullOrEmpty()) return ResultResponse.Error(EnumHttpCode.INCORRECT_PARAMETER, generateMapError(call, 101 to "Incorrect parameter 'role'. This parameter must be 'String' type"))
            val role = EnumBearerRoles.getFromNameOrNull(_role.uppercase())
            if (role == null) return ResultResponse.Error(EnumHttpCode.NOT_FOUND, generateMapError(call, 102 to "Dont find role with name $_role"))

            return ResultResponse.Success(EnumHttpCode.COMPLETED, repo_employees.getRepositoryData().filter { it.getRoleAsEnum() == role })
        } catch (e: Exception) {
            return ResultResponse.Error(EnumHttpCode.BAD_REQUEST, generateMapError(call, 440 to e.localizedMessage))
        }
    }

    suspend fun auth(call: ApplicationCall): ResultResponse {
        try {
            val user = call.receive<Employees>()

            if (user.login.isNullOrEmpty())
                return ResultResponse.Error(EnumHttpCode.INCORRECT_PARAMETER, generateMapError(call, 101 to "Необходимо указать Логин(login)"))

            if (user.password.isNullOrEmpty())
                return ResultResponse.Error(EnumHttpCode.INCORRECT_PARAMETER, generateMapError(call, 102 to "Необходимо указать Пароль(password)"))

            val employee = repo_employees.getRepositoryData().find { it.login?.lowercase() == user.login?.lowercase() && verifyPassword(it.password, it.salt, user.password) }
            if (employee == null)
                return ResultResponse.Error(EnumHttpCode.NOT_FOUND, generateMapError(call, 103 to "Не найден сотрудник с указанным Логином и Паролем"))

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

            return ResultResponse.Success(EnumHttpCode.COMPLETED, employee)
        } catch (e: Exception) {
            e.printStackTrace()
            return ResultResponse.Error(EnumHttpCode.BAD_REQUEST, generateMapError(call, 440 to e.localizedMessage))
        }
    }

    private suspend fun _getTimeSlots(employeeId: Int, servceLength: Int) : ArrayList<LocalDateTime> {

        val daysLoaded = 30
        val startDate = LocalDateTime.currentZeroDate()
        val endDate = startDate.plus((daysLoaded).days)

        val currentRecords = Records().getData({ tbl_records.id_employee_to eq employeeId ; tbl_records.dateRecord.between(startDate..endDate) ; tbl_records.status lessEq 100 })
        val currentSheludes = ClientsSchelude().getData({ ClientsSchelude().getTable().idEmployee eq employeeId })
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

            if (_clientId == null || !_clientId.toIntPossible()) return ResultResponse.Error(EnumHttpCode.INCORRECT_PARAMETER, generateMapError(call, 101 to "Incorrect parameter 'clientId'($_clientId). This parameter must be 'Int' type"))
            if (_servceLength == null || !_servceLength.toIntPossible()) return ResultResponse.Error(EnumHttpCode.INCORRECT_PARAMETER, generateMapError(call, 102 to "Incorrect parameter 'servceLength'($_servceLength). This parameter must be 'Int' type"))

            return ResultResponse.Success(EnumHttpCode.COMPLETED, _getTimeSlots(_clientId.toInt(), _servceLength.toInt()))
        } catch (e: Exception) {
            return ResultResponse.Error(EnumHttpCode.BAD_REQUEST, generateMapError(call, 440 to e.localizedMessage))
        }
    }

    suspend fun getSlots(call: ApplicationCall): ResultResponse {
        try {
            val _id = call.parameters["id"]
            val _data = call.parameters["data"]

            if (_id == null || !_id.toIntPossible())
                return ResultResponse.Error(EnumHttpCode.INCORRECT_PARAMETER, generateMapError(call, 101 to "Incorrect parameter 'id'($_id). This parameter must be 'Int' type"))
            if (_data.isNullOrEmpty())
                return ResultResponse.Error(EnumHttpCode.INCORRECT_PARAMETER, generateMapError(call, 102 to "Необходимо указать дату"))
            if (!_data.toDateTimePossible())
                return ResultResponse.Error(EnumHttpCode.INCORRECT_PARAMETER, generateMapError(call, 103 to "Неверный формат даты"))

            val id = _id.toInt()
            val data = LocalDateTime.parse(_data)

            val dateStart = LocalDateTime(data.year, data.monthNumber, data.dayOfMonth, 0, 0, 0)
            val dateEnd = LocalDateTime(data.year, data.monthNumber, data.dayOfMonth, 23, 59, 0)

            val currentDayRecords = Records().getData({ tbl_records.id_employee_to eq id ; tbl_records.dateRecord.between(dateStart..dateEnd) })

            return ResultResponse.Success(EnumHttpCode.COMPLETED, currentDayRecords)
        } catch (e: Exception) {
            return ResultResponse.Error(EnumHttpCode.BAD_REQUEST, generateMapError(call, 440 to e.localizedMessage))
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
