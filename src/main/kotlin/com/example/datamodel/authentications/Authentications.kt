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
import com.example.logging.DailyLogger.printTextLog
import com.example.plugins.JWT_AUDIENCE
import com.example.plugins.JWT_HMAC
import com.example.plugins.JWT_ISSUER
import com.example.plus
import com.example.security.AESEncryption
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
    @Transient
    @KomapperVersion
    override val version: Int = 0,
    @Transient
    override val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntBaseDataImpl<Authentications>() {

    companion object {
        val tbl_authentications = Meta.authentications
        val repo_authentications = BaseRepository(Authentications())

        suspend fun createToken(client: Clients): Authentications {
            printTextLog("[Authentications::createToken::Clients] Создаем токен для пользователя id ${client.id} type: ${client.role} encr: ${AESEncryption.decrypt(client.role)}")
            val role = EnumBearerRoles.getFromName(client.role)
            val tokenDuration = LocalDateTime.currectDatetime().plus(role.tokenDuration)
            val auth = Authentications(
                clientId = client.id,
                token = generateJWTToken(client, tokenDuration),
                dateExpired = tokenDuration,
                dateUsed = LocalDateTime.currectDatetime(),
                role = role.name,
                employee = false
            )
            val newauth = auth.create("Authentications::createToken")
            repo_authentications.addData(newauth)
            return newauth
        }

        suspend fun createToken(employee: Employees): Authentications {
            printTextLog("[Authentications::createToken::Employees] Создаем токен для пользователя id ${employee.id} type: ${employee.role} encr: ${AESEncryption.decrypt(employee.role)}")
            val role = employee.getRoleAsEnum()
            val tokenDuration = LocalDateTime.currectDatetime().plus(role.tokenDuration)
            val auth = Authentications(
                clientId = employee.id,
                token = generateJWTToken(employee, tokenDuration),
                dateExpired = tokenDuration,
                dateUsed = LocalDateTime.currectDatetime(),
                role = employee.role,
                employee = true
            )
            val newauth = auth.create("Authentications::createToken")
            repo_authentications.addData(newauth)
            return newauth
        }

        private fun generateJWTToken(employee: Employees, tokenDuration: LocalDateTime): String {
            val token = JWT.create()
                .withAudience(JWT_AUDIENCE)
                .withIssuer(JWT_ISSUER)
                .withClaim("userId", employee.id)
                .withClaim("employee", true)
                .withExpiresAt(Date(tokenDuration.toInstant(TimeZone.UTC).toEpochMilliseconds()))
                .sign(Algorithm.HMAC256(JWT_HMAC))
            return token
        }

        private fun generateJWTToken(client: Clients, tokenDuration: LocalDateTime): String {
            val token = JWT.create()
                .withAudience(JWT_AUDIENCE)
                .withIssuer(JWT_ISSUER)
                .withClaim("userId", client.id)
                .withClaim("employee", false)
                .withExpiresAt(Date(tokenDuration.toInstant(TimeZone.UTC).toEpochMilliseconds()))
                .sign(Algorithm.HMAC256(JWT_HMAC))
            return token
        }

        suspend fun getTokenFromClient(client: Clients): Authentications? {
            return repo_authentications.getRepositoryData().find { it.clientId == client.id && it.employee == false }
        }

        suspend fun getTokenFromEmployee(employee: Employees): Authentications? {
            return repo_authentications.getRepositoryData().find { it.clientId == employee.id && it.employee == true }
        }
    }

    override fun getTable() = tbl_authentications
    override fun getRepository() = repo_authentications
    override fun isValidLine(): Boolean {
        return clientId != null && token != null && dateExpired != null && dateUsed != null && role != null && employee != null
    }

    fun isExpires(): Boolean {
        if (dateExpired == null) return true
        return dateExpired!! <= LocalDateTime.currectDatetime()
    }

    override fun toString(): String {
        return "Authentications(id=$id, clientId=$clientId, token=$token, dateExpired=$dateExpired, dateUsed=$dateUsed, employee=$employee version=$version)"
    }
}