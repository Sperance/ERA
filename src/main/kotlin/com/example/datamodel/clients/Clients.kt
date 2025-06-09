package com.example.datamodel.clients

import com.example.setToken
import com.example.helpers.CommentField
import com.example.currectDatetime
import com.example.basemodel.IntBaseDataImpl
import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
import com.example.datamodel.authentications.Authentications
import com.example.datamodel.feedbacks.FeedBacks
import com.example.helpers.getSize
import com.example.datamodel.records.Records
import com.example.enums.EnumBearerRoles
import com.example.generateMapError
import com.example.helpers.update
import com.example.helpers.GMailSender
import com.example.helpers.clearLinks
import com.example.helpers.delete
import com.example.helpers.getDataFromId
import com.example.helpers.getDataOne
import com.example.helpers.getField
import com.example.helpers.haveField
import com.example.helpers.putField
import com.example.logging.DailyLogger.printTextLog
import com.example.security.AESEncryption
import com.example.security.generateSalt
import com.example.security.hashString
import com.example.security.verifyPassword
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
    var first_name: String? = null,
    @CommentField("Фамилия клиента")
    var last_name: String? = null,
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
    var image_link: String? = null,
    @Transient
    var image_format: String? = null,
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
) : IntBaseDataImpl<Clients>() {

    companion object {
        val tbl_clients = Meta.clients
    }

    override fun getTable() = tbl_clients
    override fun isValidLine(): Boolean {
        return first_name != null && login != null && password != null && role != null && salt != null
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<Clients>, serializer: KSerializer<List<Clients>>): ResultResponse {
        params.checkings.add { ClientsErrors.ERROR_NAME.toCheckObj(it) }
        params.checkings.add { ClientsErrors.ERROR_SURNAME.toCheckObj(it) }
        params.checkings.add { ClientsErrors.ERROR_PHONE.toCheckObj(it) }
        params.checkings.add { ClientsErrors.ERROR_EMAIL.toCheckObj(it) }
        params.checkings.add { ClientsErrors.ERROR_GENDER.toCheckObj(it) }
        params.checkings.add { ClientsErrors.ERROR_PHONE_DUPLICATE.toCheckObj(it) }
        params.checkings.add { ClientsErrors.ERROR_EMAIL_DUPLICATE.toCheckObj(it) }
        params.checkings.add { ClientsErrors.ERROR_SALT_NOTNULL.toCheckObj(it) }
        params.checkings.add { ClientsErrors.ERROR_LOGIN_DUPLICATE_NOTNULL.toCheckObj(it) }

        params.onBeforeCompleted = { obj ->
            val size = Clients().getSize()
            if (obj.last_name != null) obj.last_name = AESEncryption.encrypt(obj.last_name)
            if (obj.first_name != null) obj.first_name = AESEncryption.encrypt(obj.first_name)
            if (obj.patronymic != null) obj.patronymic = AESEncryption.encrypt(obj.patronymic)
            if (obj.email != null) obj.email = AESEncryption.encrypt(obj.email)
            if (obj.phone != null) obj.phone = AESEncryption.encrypt(obj.phone)
            if (obj.password != null) obj.setNewPassword(obj.password!!)
            if (obj.role != null) obj.role = AESEncryption.encrypt(obj.role + "_" + size)
        }

        val size = Clients().getSize()
        params.defaults.add { it::login to "client_$size" }
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
                return ClientsErrors.ERROR_LOGIN.toResultResponse(call, user)

            if (user.password.isNullOrEmpty())
                return ClientsErrors.ERROR_PASSWORD.toResultResponse(call, user)

            val client = getDataOne({ tbl_clients.login eq user.login })
            if (client == null)
                return ClientsErrors.ERROR_LOGINPASSWORD.toResultResponse(call, user)
            val verifyPass = verifyPassword(client.password, client.salt, user.password)
            if (!verifyPass)
                return ClientsErrors.ERROR_LOGINPASSWORD.toResultResponse(call, user)

            var token = Authentications.getTokenFromClient(client)
            if (token == null) {
                token = Authentications.createToken(client.id, false, client.getRoleAsEnum(), call)
            } else {
                if (token.isExpires()) {
                    printTextLog("[Clients] Токен просрочен. Удаляем и создаём новый")
                    token.delete()
                    token = Authentications.createToken(client.id, false, client.getRoleAsEnum(), call)
                }
            }

            call.response.setToken(token.token?:"", GMTDate(token.date_expired!!.toInstant(TimeZone.UTC).toEpochMilliseconds()))

            return ResultResponse.Success(client)
        } catch (e: Exception) {
            printTextLog("[ERROR] ${e.localizedMessage}")
            return ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage))
        }
    }

    suspend fun changePasswordFromEmail(call: ApplicationCall): ResultResponse {
        try {
            val email = call.parameters["email"]
            val password = call.parameters["password"]

            if (email.isNullOrEmpty())
                return ClientsErrors.ERROR_EMAIL.toResultResponse(call, this)
            if (password.isNullOrEmpty())
                return ClientsErrors.ERROR_PASSWORD.toResultResponse(call, this)

            val findedClient = getDataOne({ tbl_clients.email eq AESEncryption.encrypt(email) })
            if (findedClient == null)
                return ClientsErrors.ERROR_EMAIL_DONTFIND.toResultResponse(call, this)

            findedClient.setNewPassword(password)
            val updated = findedClient.update("Clients::changePasswordFromEmail")

            return ResultResponse.Success(updated)
        } catch (e: Exception) {
            return ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage))
        }
    }

    suspend fun onExitSite(call: ApplicationCall): ResultResponse {
        try {
            val id = call.parameters["id"]
            if (id.isNullOrEmpty())
                return ClientsErrors.ERROR_ID_PARAMETER.toResultResponse(call, this)

            if (!id.toIntPossible())
                return ClientsErrors.ERROR_ID_NOT_INT_PARAMETER.toResultResponse(call, this)

            val findedClient = getDataFromId(id.toIntOrNull())
            if (findedClient == null)
                return ClientsErrors.ERROR_ID_DONTFIND.toResultResponse(call, this)

            val key = Authentications.getTokenFromClient(findedClient)
            if (key == null) {
                return ClientsErrors.ERROR_LOGINKEY_DONTFIND.toResultResponse(call, this)
            }
            key.delete()

            return ResultResponse.Success("")
        } catch (e: Exception) {
            return ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage))
        }
    }

    suspend fun postRecoveryPassword(call: ApplicationCall): ResultResponse {
        try {
            val email = call.parameters["email"]
            val send = call.parameters["send"]

            if (email.isNullOrEmpty())
                return ClientsErrors.ERROR_EMAIL.toResultResponse(call, this)

            val findedClient = getDataOne({ tbl_clients.email eq AESEncryption.encrypt(email) })
            if (findedClient == null)
                return ClientsErrors.ERROR_EMAIL_DONTFIND.toResultResponse(call, this)

            val generatedPassword = generateEmailRecoveryCode()
            if (send != null) {
                val boolSend = send.toBooleanStrictOrNull()
                if (boolSend == null) {
                    return ClientsErrors.ERROR_SEND_PARAMETER.toResultResponse(call, this)
                }
                if (boolSend) {
                    GMailSender().sendMail("Восстановление пароля", "Код для восстановления пароля: $generatedPassword", email)
                }
            }

            return ResultResponse.Success(generatedPassword)
        } catch (e: Exception) {
            return ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage))
        }
    }

    override suspend fun delete(call: ApplicationCall, params: RequestParams<Clients>): ResultResponse {
        params.onBeforeCompleted = { obj ->
            Records().clearLinks(Records::id_client_from, obj.id)
            FeedBacks().clearLinks(FeedBacks::id_client_from, obj.id)

            Authentications.getTokenFromClient(obj)?.delete()
            true
        }

        return super.delete(call, params)
    }

    override suspend fun update(call: ApplicationCall, params: RequestParams<Clients>, serializer: KSerializer<Clients>): ResultResponse {
        params.checkings.add { ClientsErrors.ERROR_SALT_NOTNULL.toCheckObj(it) }
        params.checkings.add { ClientsErrors.ERROR_ROLE_NOTNULL.toCheckObj(it) }
        params.checkings.add { ClientsErrors.ERROR_LOGIN_DUPLICATE_NOTNULL.toCheckObj(it) }
        params.checkings.add { ClientsErrors.ERROR_PHONE_DUPLICATE_NUTNULL.toCheckObj(it) }
        params.checkings.add { ClientsErrors.ERROR_EMAIL_DUPLICATE_NOTNULL.toCheckObj(it) }

        params.onBeforeCompleted = { obj ->
            if (obj.last_name != null) obj.last_name = AESEncryption.encrypt(obj.last_name)
            if (obj.first_name != null) obj.first_name = AESEncryption.encrypt(obj.first_name)
            if (obj.patronymic != null) obj.patronymic = AESEncryption.encrypt(obj.patronymic)
            if (obj.email != null) obj.email = AESEncryption.encrypt(obj.email)
            if (obj.phone != null) obj.phone = AESEncryption.encrypt(obj.phone)
            printTextLog("OLD PASS: ${obj.password}")
            if (obj.password != null) obj.setNewPassword(obj.password!!)
            printTextLog("NEW PASS: ${obj.password}")
        }

        params.checkOnUpdate = { finded, new ->
            if (new.haveField("salt") && finded.haveField("salt")) {
                new.putField("salt", finded.getField("salt"))
            }
            true
        }
        return super.update(call, params, serializer)
    }

    private fun setNewPassword(newPass: String) {
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
        return "Clients(id=$id, first_name=$first_name, last_name=$last_name, patronymic=$patronymic, login=$login, password=$password, phone=$phone, email=$email, role=$role, image_link=$image_link, image_format=$image_format, deleted=$deleted)"
    }
}