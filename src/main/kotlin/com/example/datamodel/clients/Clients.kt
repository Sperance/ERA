package com.example.datamodel.clients

import com.example.helpers.CommentField
import com.example.currectDatetime
import com.example.currentZeroDate
import com.example.datamodel.BaseRepository
import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.catalogs.Catalogs
import com.example.datamodel.clientsschelude.ClientsSchelude
import com.example.datamodel.clientsschelude.ClientsSchelude.Companion.tbl_clientsschelude
import com.example.datamodel.feedbacks.FeedBacks
import com.example.helpers.getData
import com.example.helpers.getSize
import com.example.helpers.isDuplicate
import com.example.datamodel.records.Records
import com.example.datamodel.records.Records.Companion.tbl_records
import com.example.datamodel.serverhistory.ServerHistory
import com.example.datamodel.services.Services.Companion.repo_services
import com.example.helpers.update
import com.example.isNullOrZero
import com.example.minus
import com.example.nullDatetime
import com.example.helpers.GMailSender
import com.example.helpers.getField
import com.example.helpers.haveField
import com.example.helpers.putField
import com.example.plus
import com.example.security.generateSalt
import com.example.security.hashPassword
import com.example.security.verifyPassword
import com.example.toDateTimePossible
import com.example.toIntPossible
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.r2dbc.spi.R2dbcType
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.komapper.annotation.*
import org.komapper.core.dsl.Meta
import org.komapper.core.type.ClobString
import java.sql.JDBCType
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
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
    var position: Int? = null,
    @CommentField("Описание сотрудника", false)
    var description: String? = null,
    @CommentField("Тип клиента", false)
    var clientType: String? = null,
    @CommentField("Пол клиента", true)
    var gender: Byte? = null,
    @CommentField("Прямая ссылка на картинку", false)
    var imageLink: String? = null,
    @Transient
    var imageFormat: String? = null,
    @CommentField("Массив ссылок на работы сотрудника", false)
    var arrayTypeWork: Array<Int>? = null,
    @Transient
    @CommentField("Соль для шифрования", false)
    var salt: String? = null,
    @Transient
    @KomapperVersion
    val version: Int = 0,
    @Transient
    @CommentField("Дата создания строки", false)
    val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntBaseDataImpl<Clients>() {

    companion object {
        val tbl_clients = Meta.clients
        val repo_clients = BaseRepository(Clients())
    }

    override fun getBaseId() = id
    override fun baseParams(): RequestParams<Clients> {
        val params = RequestParams<Clients>()
        params.checkings.add { CheckObj(it.position != null && !Catalogs.repo_catalogs.isHaveData(it.position), 441, "Не найдена Должность с id ${it.position}") }
        params.checkings.add { CheckObj(it.arrayTypeWork != null && !Catalogs.repo_catalogs.isHaveData(it.arrayTypeWork?.toList()), 442, "Не найдены Категории с arrayTypeWork ${it.arrayTypeWork?.joinToString()} ALL: ${Catalogs.repo_catalogs.getRepositoryData().joinToString("\n")}") }
        params.checkings.add { CheckObj(it.salt != null, 443, "Попытка модификации системных данных. Информация о запросе передана Администраторам") }
        return params
    }

    private fun generateEmailRecoveryCode() : String {
        val randomSuffix = Random.nextInt(10000, 99999)
        return "CL-$randomSuffix"
    }

    private suspend fun _getTimeSlots(clientId: Int, servceLength: Int) : ArrayList<LocalDateTime> {

        val daysLoaded = 30
        val startDate = LocalDateTime.currentZeroDate()
        val endDate = startDate.plus((daysLoaded).days)

        val currentRecords = Records().getData({ tbl_records.id_client_to eq clientId ; tbl_records.dateRecord.between(startDate..endDate) ; tbl_records.status lessEq 100 })
        val currentSheludes = ClientsSchelude().getData({ tbl_clientsschelude.idClient eq clientId })
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

            if (_clientId == null || !_clientId.toIntPossible()) return ResultResponse.Error(HttpStatusCode(430, ""), "Incorrect parameter 'clientId'($_clientId). This parameter must be 'Int' type")
            if (_servceLength == null || !_servceLength.toIntPossible()) return ResultResponse.Error(HttpStatusCode(431, ""), "Incorrect parameter 'servceLength'($_servceLength). This parameter must be 'Int' type")

            return ResultResponse.Success(HttpStatusCode.OK, _getTimeSlots(_clientId.toInt(), _servceLength.toInt()))
        } catch (e: Exception) {
            return ResultResponse.Error(HttpStatusCode.Conflict, e.localizedMessage?:"")
        }
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

    suspend fun auth(call: ApplicationCall): ResultResponse {
        try {
            val user = call.receive<Clients>()

            if (user.login.isNullOrEmpty())
                return ResultResponse.Error(HttpStatusCode(431, ""), "Необходимо указать Логин(login)")

            if (user.password.isNullOrEmpty())
                return ResultResponse.Error(HttpStatusCode(432, ""), "Необходимо указать Пароль(password)")

            val client = repo_clients.getRepositoryData().find { it.login == user.login && verifyPassword(it.password, it.salt, user.password) }
            if (client == null)
                return ResultResponse.Error(HttpStatusCode.BadRequest, "Не найден пользователь с указанным Логином и Паролем")

            ServerHistory.addRecord(11, "Авторизация пользователя ${client.id}", client.toStringLow())
            return ResultResponse.Success(HttpStatusCode.OK, client)
        } catch (e: Exception) {
            return ResultResponse.Error(HttpStatusCode.Conflict, e.localizedMessage)
        }
    }

    suspend fun changePasswordFromEmail(call: ApplicationCall): ResultResponse {
        try {
            val email = call.parameters["email"]
            val password = call.parameters["password"]

            if (email.isNullOrEmpty())
                return ResultResponse.Error(HttpStatusCode(431, ""), "Incorrect parameter 'email'($email). This parameter must be 'String' type")
            if (password.isNullOrEmpty())
                return ResultResponse.Error(HttpStatusCode(432, ""), "Incorrect parameter 'password'($password). This parameter must be 'String' type")

            val findedClient = repo_clients.getRepositoryData().find { it.email == email }
            if (findedClient == null)
                return ResultResponse.Error(HttpStatusCode(433, ""), "Не найден Клиент с адресом $email")

            findedClient.setNewPassword(password)
            val updated = findedClient.update()

            repo_clients.resetData()

            ServerHistory.addRecord(12, "Изменение пароля пользователя ${updated.id}", password)

            return ResultResponse.Success(HttpStatusCode.OK, updated)
        } catch (e: Exception) {
            return ResultResponse.Error(HttpStatusCode.BadRequest, e.localizedMessage)
        }
    }

    suspend fun postRecoveryPassword(call: ApplicationCall): ResultResponse {
        try {
            val email = call.parameters["email"]
            val send = call.parameters["send"]

            if (email.isNullOrEmpty())
                return ResultResponse.Error(HttpStatusCode(431, ""), "Incorrect parameter 'email'($email). This parameter must be 'String' type")

            val findedClient = repo_clients.getRepositoryData().find { it.email == email }
            if (findedClient == null)
                return ResultResponse.Error(HttpStatusCode(432, ""), "Не найден Клиент с адресом $email")

            val generatedPassword = generateEmailRecoveryCode()
            if (send != null) {
                val boolSend = send.toBooleanStrictOrNull()
                if (boolSend == null) {
                    return ResultResponse.Error(HttpStatusCode(433, ""), "Параметр send($send) должен быть boolean")
                }
                if (boolSend) {
                    GMailSender().sendMail("Восстановление пароля", "Код для восстановления пароля: $generatedPassword", email)
                }
            }

            ServerHistory.addRecord(13, "Отправлен запрос на $email на восстановление пароля", generatedPassword)

            return ResultResponse.Success(HttpStatusCode.OK, generatedPassword)

        } catch (e: Exception) {
            return ResultResponse.Error(HttpStatusCode.BadRequest, e.localizedMessage)
        }
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<Clients>, serializer: KSerializer<List<Clients>>): ResultResponse {
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
        params.checkings.add { CheckObj(it.position != null && !Catalogs.repo_catalogs.isHaveData(it.position), 444, "Не найдена Должность с id ${it.position}") }
        params.checkings.add { CheckObj(it.arrayTypeWork != null && !Catalogs.repo_catalogs.isHaveData(it.arrayTypeWork?.toList()), 445, "Не найдены Категории с arrayTypeWork ${it.arrayTypeWork?.joinToString()}") }

        val size = Clients().getSize()
        params.defaults.add { it::dateBirthday to LocalDateTime.nullDatetime() }
        params.defaults.add { it::dateWorkIn to LocalDateTime.nullDatetime() }
        params.defaults.add { it::dateWorkOut to LocalDateTime.nullDatetime() }
        params.defaults.add { it::login to "base_client_$size" }
        params.defaults.add { it::password to generateShortClientPassword(size) }
        params.defaults.add { it::arrayTypeWork to arrayOf<Int>() }
        params.defaults.add { it::salt to generateSalt() }

        params.onBeforeCompleted = { obj ->
            obj!!.setNewPassword(obj.password!!)
        }

        return super.post(call, params, serializer)
    }

    override suspend fun delete(call: ApplicationCall, params: RequestParams<Clients>): ResultResponse {
        params.onBeforeCompleted = { obj ->
            Records.repo_records.clearLinkEqual(Records::id_client_to, obj?.id)
            Records.repo_records.clearLinkEqual(Records::id_client_from, obj?.id)
            ClientsSchelude.repo_clientsschelude.clearLinkEqual(ClientsSchelude::idClient, obj?.id)
            FeedBacks.repo_feedbacks.clearLinkEqual(FeedBacks::id_client_to, obj?.id)
            FeedBacks.repo_feedbacks.clearLinkEqual(FeedBacks::id_client_from, obj?.id)
        }

        return super.delete(call, params)
    }

    override suspend fun update(call: ApplicationCall, params: RequestParams<Clients>, serializer: KSerializer<Clients>): ResultResponse {
        params.checkOnUpdate = { finded, new ->
            if (new.haveField("salt") && finded.haveField("salt")) {
                new.putField("salt", finded.getField("salt"))
            }
            if (new.password != null) new.setNewPassword(new.password!!)
        }
        return super.update(call, params, serializer)
    }

    fun setNewPassword(newPass: String) {
        password = hashPassword(newPass, salt!!)
    }

    private fun generateShortClientPassword(allRecords: Long): String {
        val randomPass = Random(System.currentTimeMillis()).nextInt(10000..100000)
        return "PWD_" + (randomPass + allRecords)
    }

    private fun toStringLow(): String {
        return "{id=$id, firstName=$firstName, login=$login, password=$password, email=$email, clientType=$clientType}"
    }
}