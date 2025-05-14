package com.example.datamodel.authentications

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.basemodel.BaseRepository
import com.example.basemodel.IntBaseDataImpl
import com.example.currectDatetime
import com.example.currentZeroDate
import com.example.datamodel.clients.Clients
import com.example.enums.EnumBearerRoles
import com.example.helpers.create
import com.example.isNullOrZero
import com.example.logging.DailyLogger.printTextLog
import com.example.minus
import com.example.plugins.JWT_AUDIENCE
import com.example.plugins.JWT_HMAC
import com.example.plugins.JWT_ISSUER
import com.example.plus
import io.ktor.server.auth.jwt.JWTPrincipal
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

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
            printTextLog("[Authentications::createToken] Создаем токен для пользователя id ${client.id}")
            val role = EnumBearerRoles.getFromName(client.clientType)
            val tokenDuration = LocalDateTime.currectDatetime().plus(role.tokenDuration)
            val auth = Authentications(
                clientId = client.id,
                token = generateJWTToken(client, tokenDuration),
                dateExpired = tokenDuration,
                dateUsed = LocalDateTime.currectDatetime(),
                role = role.name
            )
            val newauth = auth.create("Authentications::createToken")
            repo_authentications.addData(newauth)
            return newauth
        }

        private fun generateJWTToken(client: Clients, tokenDuration: LocalDateTime): String {
            val role = EnumBearerRoles.getFromName(client.clientType)
            val token = JWT.create()
                .withAudience(JWT_AUDIENCE)
                .withIssuer(JWT_ISSUER)
                .withClaim("clientId", client.id)
                .withClaim("clientRole", role.name)
                .withExpiresAt(Date(tokenDuration.toInstant(TimeZone.UTC).toEpochMilliseconds()))
                .sign(Algorithm.HMAC256(JWT_HMAC))
            return token
        }

        private fun generateBearerToken(): String {
            return "era_" + UUID.randomUUID().toString().substringAfter("-").replace("-", "")
        }

        suspend fun getTokenFromClient(client: Clients): Authentications? {
            return repo_authentications.getRepositoryData().find { it.clientId == client.id }
        }

        fun checkCorrectJWT(jwt: JWTPrincipal): AuthData {
            try {
                if (jwt.payload.getClaim("clientId") == null || jwt.payload.getClaim("clientId").toString().toInt().isNullOrZero())
                    return AuthData("Не указан параметр токена 'clientId'", null, null)
                val clientId = jwt.payload.getClaim("clientId").toString().toInt()

                if (jwt.payload.getClaim("clientRole") == null || jwt.payload.getClaim("clientRole").toString().isBlank())
                    return AuthData("Не указан параметр токена 'clientRole'", clientId, null)
                val clientRole = jwt.payload.getClaim("clientRole").toString()

                val roleEnum = EnumBearerRoles.getFromName(clientRole)
                return AuthData(null, clientId, roleEnum)
            }catch (e: Exception) {
                e.printStackTrace()
                printTextLog("[checkCorrectJWT] ERROR: ${e.localizedMessage}")
                return AuthData(e.localizedMessage, null, null)
            }
        }
    }

    override fun getTable() = tbl_authentications
    override fun getRepository() = repo_authentications

    fun isExpires(): Boolean {
        if (dateExpired == null) return true
        return dateExpired!! <= LocalDateTime.currectDatetime()
    }

    fun getEnumRole(): EnumBearerRoles? {
        if (role == null) return null
        return EnumBearerRoles.valueOf(role!!)
    }

    override fun toString(): String {
        return "Authentications(id=$id, clientId=$clientId, token=$token, dateExpired=$dateExpired, dateUsed=$dateUsed, version=$version)"
    }
}

@Serializable
data class AuthData(val errorText: String?, val clientId: Int?, val clientRole: EnumBearerRoles?)