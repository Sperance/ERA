package com.example.datamodel.employees

import com.example.setToken
import com.example.basemodel.IntBaseDataImpl
import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
import com.example.currectDatetime
import com.example.currentZeroDate
import com.example.datamodel.authentications.Authentications
import com.example.datamodel.clientsschelude.ClientsSchelude
import com.example.datamodel.clientsschelude.ClientsSchelude.Companion.tbl_clientsschelude
import com.example.datamodel.feedbacks.FeedBacks
import com.example.datamodel.records.Records
import com.example.datamodel.records.Records.Companion.tbl_records
import com.example.datamodel.services.Services
import com.example.enums.EnumBearerRoles
import com.example.generateMapError
import com.example.helpers.CommentField
import com.example.helpers.clearLinks
import com.example.helpers.delete
import com.example.helpers.getData
import com.example.helpers.getDataFromId
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
import org.komapper.annotation.KomapperUpdatedAt
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
    var first_name: String? = null,
    @CommentField("Фамилия")
    var last_name: String? = null,
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
    var date_birthday: LocalDateTime? = null,
    @CommentField("Дата принятия на работу")
    var date_work_in: LocalDateTime? = null,
    @CommentField("Дата увольнения")
    var date_work_out: LocalDateTime? = null,
    @CommentField("Должность")
    var position: Int? = null,
    @CommentField("Описание")
    var description: String? = null,
    @CommentField("Роль сотрудника")
    var role: String? = null,
    @CommentField("Пол сотрудника")
    var gender: Byte? = null,
    @CommentField("Прямая ссылка на картинку")
    var image_link: String? = null,
    @Transient
    var image_format: String? = null,
    @CommentField("Массив ссылок на работы сотрудника")
    var array_type_work: Array<Int>? = null,
    @Transient
    var salt: String? = null,
    @Transient
    @KomapperVersion
    override val version: Int = 0,
    @Transient
    @CommentField("Дата создания строки")
    override val created_at: LocalDateTime = LocalDateTime.currectDatetime(),
    @Transient
    @KomapperUpdatedAt
    override val updated_at: LocalDateTime = LocalDateTime.currectDatetime(),
    @Transient
    override val deleted: Boolean = false
) : IntBaseDataImpl<Employees>() {

    companion object {
        val tbl_employees = Meta.employees
    }

    override fun getTable() = tbl_employees
    override fun isValidLine(): Boolean {
        return first_name != null && login != null && password != null && role != null && salt != null
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
//        params.checkings.add { EmployeesErrors.ERROR_ARRAYTYPEWORK_DUPLICATE_NOTNULL.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_SALT_NOTNULL.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_ROLE_ENUM.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_ROLE_ADMIN.toCheckObj(it) }

        params.onBeforeCompleted = { obj ->
            val size = Employees().getSize()
            if (obj.last_name != null) obj.last_name = AESEncryption.encrypt(obj.last_name)
            if (obj.first_name != null) obj.first_name = AESEncryption.encrypt(obj.first_name)
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
//        params.checkings.add { EmployeesErrors.ERROR_ARRAYTYPEWORK_DUPLICATE_NOTNULL.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_SALT_NOTNULL.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_EMAIL_DUPLICATE_NOTNULL.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_ROLE_ADMIN_NOTNULL.toCheckObj(it) }
        params.checkings.add { EmployeesErrors.ERROR_LOGIN_DUPLICATE_NOTNULL.toCheckObj(it) }

        params.onBeforeCompleted = { obj ->
            if (obj.last_name != null) obj.last_name = AESEncryption.encrypt(obj.last_name)
            if (obj.first_name != null) obj.first_name = AESEncryption.encrypt(obj.first_name)
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
            Records().clearLinks(Records::id_employee_to, obj.id)
            ClientsSchelude().clearLinks(ClientsSchelude::id_employee, obj.id)
            FeedBacks().clearLinks(FeedBacks::id_employee_to, obj.id)

            Authentications.getTokenFromEmployee(obj)?.delete()
            true
        }

        return super.delete(call, params)
    }

    suspend fun auth(call: ApplicationCall): ResultResponse {
        try {
            val user = call.receive<Employees>()

            if (user.login.isNullOrEmpty())
                return EmployeesErrors.ERROR_LOGIN.toResultResponse(call, this)

            if (user.password.isNullOrEmpty())
                return EmployeesErrors.ERROR_PASSWORD.toResultResponse(call, this)

            val employee = getDataOne({ tbl_employees.login eq user.login })
            if (employee == null)
                return EmployeesErrors.ERROR_LOGINPASSWORD.toResultResponse(call, this)
            val verifyPass = verifyPassword(employee.password, employee.salt, user.password)
            if (!verifyPass)
                return EmployeesErrors.ERROR_LOGINPASSWORD.toResultResponse(call, employee)

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

            call.response.setToken(token.token?:"", GMTDate(token.date_expired!!.toInstant(TimeZone.UTC).toEpochMilliseconds()))

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

        val currentRecords = Records().getData({ tbl_records.id_employee_to eq employeeId ; tbl_records.date_record between startDate..endDate ; tbl_records.status lessEq 100 })
        val currentSheludes = ClientsSchelude().getData({ tbl_clientsschelude.id_employee eq employeeId })
        val allServices = Services().getData()

        val stockPeriod = 30
        val removePeriodMin = 1
        val blockSlots = servceLength * stockPeriod

        val arrayClosed = ArrayList<ClosedRange<LocalDateTime>>()
        currentRecords.filter { it.date_record!! in startDate..endDate }.forEach {
            val servLen = allServices.find { serv -> serv.id == it.id_service }!!
            val servDateBegin = it.date_record!!.minus((blockSlots - removePeriodMin).minutes)
            val servDateEnd = it.date_record!!.plus((servLen.duration!! * stockPeriod - removePeriodMin).minutes)
            arrayClosed.add(servDateBegin..servDateEnd)
        }

        var stockDate = startDate
        val maxDateTime = endDate.minus((blockSlots - removePeriodMin).minutes)
        val araResult = ArrayList<LocalDateTime>()
        while (true) {
            if (stockDate >= maxDateTime) break
            val finded = arrayClosed.find { ar -> stockDate in ar }
            val sheludeDate = currentSheludes.find { she -> she.schelude_date_start!!.dayOfMonth == stockDate.dayOfMonth }
            if (sheludeDate != null && finded == null && stockDate in sheludeDate.schelude_date_start!!..sheludeDate.schelude_date_end!!) {
                if (stockDate.plus(blockSlots.minutes) <= sheludeDate.schelude_date_end!!)
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

            val currentDayRecords = Records().getData({ tbl_records.id_employee_to eq id ; tbl_records.date_record between dateStart..dateEnd })

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

            val findedEmployee = getDataFromId(id.toIntOrNull())
            if (findedEmployee == null)
                return EmployeesErrors.ERROR_ID_DONTFIND.toResultResponse(call, this)

            val key = Authentications.getTokenFromEmployee(findedEmployee)
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
        return "Employees(id=$id, first_name=$first_name, last_name=$last_name, patronymic=$patronymic, login=$login, password=$password, phone=$phone, email=$email, position=$position, role=$role, image_link=$image_link, image_format=$image_format, array_type_work=${array_type_work?.contentToString()}, deleted=$deleted)"
    }
}
