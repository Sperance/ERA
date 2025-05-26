package com.example.datamodel.authentications

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.basemodel.BaseRepository
import com.example.basemodel.IntBaseDataImpl
import com.example.currectDatetime
import com.example.datamodel.clients.Clients
import com.example.datamodel.employees.Employees
import com.example.enums.EnumBearerRoles
import com.example.helpers.create
import com.example.helpers.getDataOne
import com.example.interfaces.IntPostgreTable
import com.example.logging.DailyLogger.printTextLog
import com.example.plugins.JWT_AUDIENCE
import com.example.plugins.JWT_HMAC
import com.example.plugins.JWT_ISSUER
import com.example.plus
import com.example.security.AESEncryption
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
import org.komapper.annotation.KomapperVersion
import org.komapper.core.dsl.Meta
import org.komapper.core.type.ClobString
import java.util.Date
import java.util.UUID

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
    var clientId: Int? = null,
    @KomapperColumn(alternateType = ClobString::class)
    var token: String? = null,
    var dateExpired: LocalDateTime? = null,
    var dateUsed: LocalDateTime? = null,
    var role: String? = null,
    var employee: Boolean? = null,
    var addressIP: String? = null,
    var addressGeo: String? = null,
    var addressKey: String? = null,
    var addressName: String? = null,
    @Transient
    @KomapperVersion
    override val version: Int = 0,
    @Transient
    override val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntPostgreTable<Authentications> {

    companion object {
        val tbl_authentications = Meta.authentications

        suspend fun createToken(userId: Int, employee: Boolean, role: EnumBearerRoles, call: ApplicationCall): Authentications {
            printTextLog("[Authentications::createToken::Clients] Создаем токен для пользователя $userId employee: $employee role: $role")

            val _addressIP = call.request.header("Era-Auth-Ip")?.let { AESEncryption.decrypt(it) }
            val _addressGeo = call.request.header("Era-Auth-Geo")?.let { AESEncryption.decrypt(it) }
            val _addressKey = call.request.header("Era-Auth-Key")?.let { AESEncryption.decrypt(it) }
            val _addressName = call.request.header("Era-Auth-Name")?.let { AESEncryption.decrypt(it) }

            val tokenDuration = LocalDateTime.currectDatetime().plus(role.tokenDuration)
            val auth = Authentications(
                clientId = userId,
                token = generateJWTToken(userId, employee, tokenDuration),
                dateExpired = tokenDuration,
                dateUsed = LocalDateTime.currectDatetime(),
                role = role.name,
                employee = employee,
                addressIP = _addressIP,
                addressGeo = _addressGeo,
                addressKey = _addressKey,
                addressName = _addressName
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
            return Authentications().getDataOne({ tbl_authentications.clientId eq client.id ; tbl_authentications.employee eq false })
        }

        suspend fun getTokenFromEmployee(employee: Employees): Authentications? {
            return Authentications().getDataOne({ tbl_authentications.clientId eq employee.id ; tbl_authentications.employee eq true })
        }
    }

    override fun getTable() = tbl_authentications

    fun isExpires(): Boolean {
        if (dateExpired == null) return true
        return dateExpired!! <= LocalDateTime.currectDatetime()
    }

    override fun toString(): String {
        return "Authentications(id=$id, clientId=$clientId, token=$token, dateExpired=$dateExpired, dateUsed=$dateUsed, employee=$employee version=$version)"
    }
}