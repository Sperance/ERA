package com.example.datamodel.clients

import com.example.setToken
import com.example.helpers.CommentField
import com.example.currectDatetime
import com.example.basemodel.BaseRepository
import com.example.basemodel.CheckObj
import com.example.basemodel.IntBaseDataImpl
import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
import com.example.datamodel.authentications.Authentications
import com.example.datamodel.feedbacks.FeedBacks
import com.example.helpers.getSize
import com.example.datamodel.records.Records
import com.example.enums.EnumBearerRoles
import com.example.enums.EnumHttpCode
import com.example.generateMapError
import com.example.helpers.update
import com.example.isNullOrZero
import com.example.helpers.GMailSender
import com.example.helpers.delete
import com.example.helpers.getField
import com.example.helpers.haveField
import com.example.helpers.putField
import com.example.logging.DailyLogger.printTextLog
import com.example.security.AESEncryption
import com.example.security.generateSalt
import com.example.security.hashString
import com.example.security.verifyPassword
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.util.date.GMTDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.komapper.annotation.*
import org.komapper.core.dsl.Meta
import kotlin.random.Random
import kotlin.random.nextInt

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
    override val id: Int = 0,
    @CommentField("Имя клиента")
    var firstName: String? = null,
    @CommentField("Фамилия клиента")
    var lastName: String? = null,
    @CommentField("Отчество клиента")
    var patronymic: String? = null,
    @CommentField("Логин от личного кабинета")
    var login: String? = null,
    @CommentField("Пароль от личного кабинета")
    var password: String? = null,
    @CommentField("Контактный телефон")
    var phone: String? = null,
    @CommentField("Почтовый адрес")
    var email: String? = null,
    @CommentField("Описание")
    var description: String? = null,
    @Transient
    var role: String? = null,
    @CommentField("Пол клиента")
    var gender: Byte? = null,
    @CommentField("Прямая ссылка на картинку")
    var imageLink: String? = null,
    @Transient
    var imageFormat: String? = null,
    @Transient
    var salt: String? = null,
    @Transient
    @KomapperVersion
    override val version: Int = 0,
    @Transient
    @CommentField("Дата создания строки")
    override val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntBaseDataImpl<Clients>() {

    companion object {
        val tbl_clients = Meta.clients
        val repo_clients = BaseRepository(Clients())
    }

    override fun getTable() = tbl_clients
    override fun getRepository() = repo_clients
    override fun isValidLine(): Boolean {
        return firstName != null && login != null && password != null && role != null && salt != null
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<Clients>, serializer: KSerializer<List<Clients>>): ResultResponse {
        params.checkings.add { CheckObj(it.firstName.isNullOrEmpty(), EnumHttpCode.INCORRECT_PARAMETER, 301, "Необходимо указать Имя") }
        params.checkings.add { CheckObj(it.lastName.isNullOrEmpty(), EnumHttpCode.INCORRECT_PARAMETER, 302, "Необходимо указать Фамилию") }
        params.checkings.add { CheckObj(it.phone.isNullOrEmpty(), EnumHttpCode.INCORRECT_PARAMETER, 303, "Необходимо указать Телефон") }
        params.checkings.add { CheckObj(it.email.isNullOrEmpty(), EnumHttpCode.INCORRECT_PARAMETER, 304, "Необходимо указать Email") }
        params.checkings.add { CheckObj(it.gender.isNullOrZero(), EnumHttpCode.INCORRECT_PARAMETER, 305, "Необходимо указать Пол") }
        params.checkings.add { CheckObj(repo_clients.isHaveDataField(Clients::phone, it.phone), EnumHttpCode.DUPLICATE, 306, "Клиент с указанным Номером телефона уже существует") }
        params.checkings.add { CheckObj(repo_clients.isHaveDataField(Clients::email, it.email), EnumHttpCode.DUPLICATE, 307, "Клиент с указанным Почтовым адресом уже существует") }
        params.checkings.add { CheckObj(it.salt != null, EnumHttpCode.BAD_REQUEST, 308, "Попытка модификации системных данных. Информация о запросе передана Администраторам") }
        params.checkings.add { CheckObj(it.role != null, EnumHttpCode.INCORRECT_PARAMETER, 309, "Попытка модификации системных данных") }
        params.checkings.add { CheckObj(it.login != null && repo_clients.isHaveDataField(Clients::login, it.login), EnumHttpCode.DUPLICATE, 310, "Клиент с указанным Логином уже существует") }

        params.onBeforeCompleted = { obj ->
            val size = Clients().getSize()
            if (obj.lastName != null) obj.lastName = AESEncryption.encrypt(obj.lastName)
            if (obj.firstName != null) obj.firstName = AESEncryption.encrypt(obj.firstName)
            if (obj.patronymic != null) obj.patronymic = AESEncryption.encrypt(obj.patronymic)
            if (obj.email != null) obj.email = AESEncryption.encrypt(obj.email)
            if (obj.phone != null) obj.phone = AESEncryption.encrypt(obj.phone)
            if (obj.password != null) obj.setNewPassword(obj.password!!)
            if (obj.role != null) obj.role = AESEncryption.encrypt(obj.role + "_" + size)
        }

        val size = Clients().getSize()
        params.defaults.add { it::login to "base_client_$size" }
        params.defaults.add { it::password to generateShortClientPassword(size) }
        params.defaults.add { it::salt to generateSalt() }
        params.defaults.add { it::role to EnumBearerRoles.USER.name }

        return super.post(call, params, serializer)
    }

    private fun generateEmailRecoveryCode() : String {
        val randomSuffix = Random.nextInt(10000, 99999)
        return "CL-$randomSuffix"
    }

    suspend fun auth(call: ApplicationCall): ResultResponse {
        try {
            val user = call.receive<Clients>()

            if (user.login.isNullOrEmpty())
                return ResultResponse.Error(EnumHttpCode.INCORRECT_PARAMETER, generateMapError(call, 101 to "Необходимо указать Логин(login)"))

            if (user.password.isNullOrEmpty())
                return ResultResponse.Error(EnumHttpCode.INCORRECT_PARAMETER, generateMapError(call, 102 to "Необходимо указать Пароль(password)"))

            val client = repo_clients.getRepositoryData().find { it.login?.lowercase() == user.login?.lowercase() && verifyPassword(it.password, it.salt, user.password) }
            if (client == null)
                return ResultResponse.Error(EnumHttpCode.NOT_FOUND, generateMapError(call, 103 to "Не найден пользователь с указанным Логином и Паролем"))

            var token = Authentications.getTokenFromClient(client)
            printTextLog("[Clients::auth] token: $token")
            if (token == null) {
                token = Authentications.createToken(client)
            } else {
                if (token.isExpires()) {
                    printTextLog("[Clients] Токен просрочен. Удаляем и создаём новый")
                    val deleteId = token.id
                    token.delete()
                    Authentications.repo_authentications.deleteData(deleteId)
                    token = Authentications.createToken(client)
                }
            }

            call.response.setToken(token.token?:"", GMTDate(token.dateExpired!!.toInstant(TimeZone.UTC).toEpochMilliseconds()))

            return ResultResponse.Success(EnumHttpCode.COMPLETED, client)
        } catch (e: Exception) {
            e.printStackTrace()
            return ResultResponse.Error(EnumHttpCode.BAD_REQUEST, generateMapError(call, 440 to e.localizedMessage))
        }
    }

    suspend fun changePasswordFromEmail(call: ApplicationCall): ResultResponse {
        try {
            val email = call.parameters["email"]
            val password = call.parameters["password"]

            if (email.isNullOrEmpty())
                return ResultResponse.Error(EnumHttpCode.INCORRECT_PARAMETER, generateMapError(call, 101 to "Incorrect parameter 'email'($email). This parameter must be 'String' type"))
            if (password.isNullOrEmpty())
                return ResultResponse.Error(EnumHttpCode.INCORRECT_PARAMETER, generateMapError(call, 102 to "Incorrect parameter 'password'($password). This parameter must be 'String' type"))

            val findedClient = repo_clients.getRepositoryData().find { it.email == AESEncryption.encrypt(email) }
            if (findedClient == null)
                return ResultResponse.Error(EnumHttpCode.NOT_FOUND, generateMapError(call, 103 to "Не найден Клиент с адресом $email"))

            findedClient.setNewPassword(password)
            val updated = findedClient.update("Clients::changePasswordFromEmail")

            repo_clients.updateData(updated)

            return ResultResponse.Success(EnumHttpCode.COMPLETED, updated)
        } catch (e: Exception) {
            return ResultResponse.Error(EnumHttpCode.BAD_REQUEST, generateMapError(call, 440 to e.localizedMessage))
        }
    }

    suspend fun postRecoveryPassword(call: ApplicationCall): ResultResponse {
        try {
            val email = call.parameters["email"]
            val send = call.parameters["send"]

            if (email.isNullOrEmpty())
                return ResultResponse.Error(EnumHttpCode.INCORRECT_PARAMETER, generateMapError(call, 101 to "Incorrect parameter 'email'($email). This parameter must be 'String' type"))

            val findedClient = repo_clients.getRepositoryData().find { it.email == AESEncryption.encrypt(email) }
            if (findedClient == null)
                return ResultResponse.Error(EnumHttpCode.NOT_FOUND, generateMapError(call, 102 to "Не найден Клиент с адресом $email"))

            val generatedPassword = generateEmailRecoveryCode()
            if (send != null) {
                val boolSend = send.toBooleanStrictOrNull()
                if (boolSend == null) {
                    return ResultResponse.Error(EnumHttpCode.INCORRECT_PARAMETER, generateMapError(call, 103 to "Параметр send($send) должен быть boolean"))
                }
                if (boolSend) {
                    GMailSender().sendMail("Восстановление пароля", "Код для восстановления пароля: $generatedPassword", email)
                }
            }

            return ResultResponse.Success(EnumHttpCode.COMPLETED, generatedPassword)

        } catch (e: Exception) {
            return ResultResponse.Error(EnumHttpCode.BAD_REQUEST, generateMapError(call, 440 to e.localizedMessage))
        }
    }

    override suspend fun delete(call: ApplicationCall, params: RequestParams<Clients>): ResultResponse {
        params.onBeforeCompleted = { obj ->
            Records.repo_records.clearLinkEqual(Records::id_client_from, obj.id)
            FeedBacks.repo_feedbacks.clearLinkEqual(FeedBacks::id_client_from, obj.id)

            val token = Authentications.getTokenFromClient(obj)
            token?.delete()
            Authentications.repo_authentications.deleteData(token)
        }

        return super.delete(call, params)
    }

    override suspend fun update(call: ApplicationCall, params: RequestParams<Clients>, serializer: KSerializer<Clients>): ResultResponse {
        params.checkings.add { CheckObj(it.salt != null, EnumHttpCode.BAD_REQUEST, 301, "Попытка модификации системных данных. Информация о запросе передана Администраторам") }
        params.checkings.add { CheckObj(it.role != null, EnumHttpCode.INCORRECT_PARAMETER, 302, "Попытка модификации системных данных") }
        params.checkings.add { CheckObj(it.login != null && repo_clients.isHaveDataField(Clients::login, it.login), EnumHttpCode.DUPLICATE, 303, "Клиент с указанным Логином уже существует") }

        params.onBeforeCompleted = { obj ->
            val size = Clients().getSize()
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
        }
        return super.update(call, params, serializer)
    }

    fun setNewPassword(newPass: String) {
        password = hashString(newPass, salt!!)
    }

    private fun generateShortClientPassword(allRecords: Long): String {
        val randomPass = Random(System.currentTimeMillis()).nextInt(10000..100000)
        return "PWD_" + (randomPass + allRecords)
    }

    fun getRoleAsEnum() : EnumBearerRoles {
        return EnumBearerRoles.getFromName(role)
    }

    override fun toString(): String {
        return "Clients(id=$id, firstName=$firstName, lastName=$lastName, patronymic=$patronymic, login=$login, password=$password, phone=$phone, email=$email, description=$description, imageLink=$imageLink)"
    }
}