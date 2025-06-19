package com.example.datamodel.authentications

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.basemodel.ResultResponse
import com.example.currectDatetime
import com.example.datamodel.clients.Clients
import com.example.datamodel.employees.Employees
import com.example.enums.EnumBearerRoles
import com.example.generateMapError
import com.example.getClientIp
import com.example.helpers.create
import com.example.helpers.delete
import com.example.helpers.getData
import com.example.helpers.getDataOne
import com.example.helpers.getLocationByIp
import com.example.interfaces.IntPostgreTable
import com.example.logging.DailyLogger.printTextLog
import com.example.plugins.JWT_AUDIENCE
import com.example.plugins.JWT_HMAC
import com.example.plugins.JWT_ISSUER
import com.example.plugins.RoleAwareJWT
import com.example.plus
import com.example.toIntPossible
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header
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
import org.komapper.annotation.KomapperUpdatedAt
import org.komapper.annotation.KomapperVersion
import org.komapper.core.dsl.Meta
import org.komapper.core.type.ClobString
import ua_parser.Parser
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Date

/**
 * Справочник информации авторизации
 */
@Serializable
@KomapperEntity
@KomapperTable("tbl_authentications")
data class Authentications(
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "authentications_id")
    override val id: Int = 0,
    var client_id: Int? = null,
    @KomapperColumn(alternateType = ClobString::class)
    @Transient
    var token: String? = null,
    var date_expired: LocalDateTime? = null,
    var date_used: LocalDateTime? = null,
    var role: String? = null,
    var employee: Boolean? = null,
//    var request_ip: String? = null,
    var request_geo: String? = null,
    var request_user_agent: String? = null,
    var request_os: String? = null,
    var request_device: String? = null,
    @Transient
    @KomapperVersion
    override val version: Int = 0,
    @Transient
    override val created_at: LocalDateTime = LocalDateTime.currectDatetime(),
    @Transient
    @KomapperUpdatedAt
    override val updated_at: LocalDateTime = LocalDateTime.currectDatetime(),
    @Transient
    override val deleted: Boolean = false
) : IntPostgreTable<Authentications> {

    companion object {
        val tbl_authentications = Meta.authentications

        suspend fun createToken(userId: Int, employee: Boolean, role: EnumBearerRoles, call: ApplicationCall): Authentications {

//            val _addressIP = call.request.header("X-User-IP")
            val _addressIP = call.getClientIp()
            val _addressGeo = call.request.header("X-User-Geo")

            var decodedGeo = ""
            try {
                decodedGeo = _addressGeo?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.name()) }?:""
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (decodedGeo.isEmpty() && _addressIP != null) {
                decodedGeo = getLocationByIp(_addressIP).toFormatString()
            }

            printTextLog("[Authentications::createToken::Clients] Создаем токен для пользователя $userId employee: $employee role: $role\nGEO: $decodedGeo")

            val userAgent = call.request.headers["User-Agent"]
            val parser = Parser().parse(userAgent)
            var _requestUserAgent = ""
            var _requestOS = ""
            var _requestDevice = ""
            if (parser != null) {
                _requestUserAgent = "${parser.userAgent.family} ${parser.userAgent.major} ${parser.userAgent.minor}"
                _requestOS = "${parser.os.family} ${parser.os.major} ${parser.os.minor}"
                _requestDevice = parser.device.family
            }

            val tokenDuration = LocalDateTime.currectDatetime().plus(role.tokenDuration)
            val auth = Authentications(
                client_id = userId,
                token = generateJWTToken(userId, employee, tokenDuration),
                date_expired = tokenDuration,
                date_used = LocalDateTime.currectDatetime(),
                role = role.name,
                employee = employee,
//                request_ip = _addressIP,
                request_geo = decodedGeo,
                request_user_agent = _requestUserAgent,
                request_os = _requestOS,
                request_device = _requestDevice
            )
            val newauth = auth.create("Authentications::createToken")
            return newauth
        }

        private fun generateJWTToken(userId: Int, employee: Boolean, tokenDuration: LocalDateTime): String {
            val token = JWT.create()
                .withAudience(JWT_AUDIENCE)
                .withIssuer(JWT_ISSUER)
                .withClaim("userId", userId)
                .withClaim("employee", employee)
                .withExpiresAt(Date(tokenDuration.toInstant(TimeZone.UTC).toEpochMilliseconds()))
                .sign(Algorithm.HMAC256(JWT_HMAC))
            return token
        }

        suspend fun getTokenFromClient(client: Clients): Authentications? {
            return Authentications().getDataOne({ tbl_authentications.client_id eq client.id ; tbl_authentications.employee eq false ; tbl_authentications.deleted eq false })
        }

        suspend fun getTokenFromEmployee(employee: Employees): Authentications? {
            return Authentications().getDataOne({ tbl_authentications.client_id eq employee.id ; tbl_authentications.employee eq true ; tbl_authentications.deleted eq false })
        }
    }

    override fun getTable() = tbl_authentications

    suspend fun getByUser(call: ApplicationCall, roleAwareJWT: RoleAwareJWT?): ResultResponse {
        try {
            if (roleAwareJWT == null) {
                return AuthenticationsErrors.ERROR_JWT.toResultResponse(call, this)
            }
            val findedAuthArray = Authentications().getData({ tbl_authentications.employee eq roleAwareJWT.employee ; tbl_authentications.client_id eq roleAwareJWT.userId ; tbl_authentications.deleted eq false })
            return ResultResponse.Success(findedAuthArray.sortedBy { it.date_used })
        } catch (e: Exception) {
            return ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage))
        }
    }

    suspend fun deleteById(call: ApplicationCall): ResultResponse {
        try {
            val id = call.parameters["id"]

            if (id.isNullOrEmpty())
                return AuthenticationsErrors.ERROR_ID_PARAM.toResultResponse(call, this)

            if (!id.toIntPossible())
                return AuthenticationsErrors.ERROR_ID_NOT_INT_PARAM.toResultResponse(call, this)

            val _id = id.toInt()

            val findedAuthArray = Authentications().getDataOne({ tbl_authentications.id eq _id ; tbl_authentications.deleted eq false })

            if (findedAuthArray == null)
                return AuthenticationsErrors.ERROR_AUTH_NOTFOUND.toResultResponse(call, this)

            findedAuthArray.delete()
            return ResultResponse.Success("Successfully deleted")
        } catch (e: Exception) {
            return ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage))
        }
    }

    fun isExpires(): Boolean {
        if (date_expired == null) return true
        return date_expired!! <= LocalDateTime.currectDatetime()
    }

    override fun toString(): String {
        return "Authentications(id=$id, client_id=$client_id, token=$token, date_expired=$date_expired, date_used=$date_used, role=$role, employee=$employee, request_geo=$request_geo, request_user_agent=$request_user_agent, request_os=$request_os, request_device=$request_device, deleted=$deleted)"
    }
}