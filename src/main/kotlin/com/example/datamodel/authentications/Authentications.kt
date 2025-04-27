package com.example.datamodel.authentications

import com.example.basemodel.BaseRepository
import com.example.basemodel.IntBaseDataImpl
import com.example.currectDatetime
import com.example.currentZeroDate
import com.example.datamodel.clients.Clients
import com.example.datamodel.serverhistory.ServerHistory
import com.example.enums.EnumBearerRoles
import com.example.helpers.CommentField
import com.example.helpers.create
import com.example.logging.DailyLogger.printTextLog
import com.example.plus
import com.example.security.AESEncryption
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable
import org.komapper.annotation.KomapperVersion
import org.komapper.core.dsl.Meta
import java.util.UUID
import kotlin.time.Duration.Companion.days

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
    val id: Int = 0,
    var clientId: Int? = null,
    var token: String? = null,
    var dateExpired: LocalDateTime? = null,
    var dateUsed: LocalDateTime? = null,
    var role: String? = null,
    var description: String? = null,
    @Transient
    @KomapperVersion
    val version: Int = 0,
    @Transient
    val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntBaseDataImpl<Authentications>() {

    companion object {
        val tbl_authentications = Meta.authentications
        val repo_authentications = BaseRepository(Authentications())

        suspend fun createToken(client: Clients?, role: EnumBearerRoles, dateExpired: LocalDateTime? = null, description: String? = null): Authentications {
            printTextLog("[Authentications::createToken] Создаем токен для пользователя id ${client?.id} роль: ${role.name}")
            val auth = Authentications(
                clientId = client?.id,
                token = generateBearerToken(),
                dateExpired = dateExpired ?: LocalDateTime.currentZeroDate().plus((3).days),
                dateUsed = LocalDateTime.currectDatetime(),
                role = role.name,
                description = description
            )
            val newauth = auth.create(null)
            repo_authentications.addData(newauth)
            return newauth
        }

        private fun generateBearerToken(): String {
            return "era_" + UUID.randomUUID().toString().substringAfter("-").replace("-", "")
        }

        suspend fun getTokenFromClient(client: Clients): Authentications? {
            return repo_authentications.getRepositoryData().find { it.clientId == client.id }
        }
    }

    fun isExpires(): Boolean {
        if (dateExpired == null) return true
        return dateExpired!! <= LocalDateTime.currentZeroDate()
    }

    fun getEnumRole(): EnumBearerRoles? {
        if (role == null) return null
        return EnumBearerRoles.valueOf(role!!)
    }

    override fun getBaseId() = id
    override fun getTblCode() = "T_AUTH_"

    override fun toString(): String {
        return "Authentications(id=$id, clientId=$clientId, token=$token, dateExpired=$dateExpired, dateUsed=$dateUsed, description=$description, version=$version)"
    }
}