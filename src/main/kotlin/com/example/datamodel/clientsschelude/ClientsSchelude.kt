package com.example.datamodel.clientsschelude

import com.example.CommentField
import com.example.currectDatetime
import com.example.currentZeroDate
import com.example.datamodel.BaseRepository
import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.catalogs.Catalogs.Companion.repo_catalogs
import com.example.datamodel.clients.Clients.Companion.repo_clients
import com.example.datamodel.delete
import com.example.isNullOrEmpty
import com.example.isNullOrZero
import com.example.minus
import com.example.nullDatetime
import com.example.plus
import com.example.printTextLog
import com.example.toIntPossible
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import kotlinx.datetime.LocalDateTime
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

/**
 * График работы сотрудников
 */
@Serializable
@KomapperEntity
@KomapperTable("tbl_clientsschelude")
data class ClientsSchelude(
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "clientsschelude_id")
    val id: Int = 0,
    @CommentField("Клиент", true)
    var idClient: Int? = null,
    @CommentField("Работа начало", true)
    var scheludeDateStart: LocalDateTime? = null,
    @CommentField("Работа конец", true)
    var scheludeDateEnd: LocalDateTime? = null,
    @Transient
    @KomapperVersion
    val version: Int = 0,
    @CommentField("Дата создания строки", false)
    val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntBaseDataImpl<ClientsSchelude>() {

    companion object {
        val tbl_clientsschelude = Meta.clientsSchelude
        val repo_clientsschelude = BaseRepository(ClientsSchelude())
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<ClientsSchelude>, serializer: KSerializer<List<ClientsSchelude>>): ResultResponse {
        params.checkings.add { CheckObj(it.idClient.isNullOrZero(), 431, "Необходимо указать id Клиента для графика работы") }
        params.checkings.add { CheckObj(it.scheludeDateStart.isNullOrEmpty(), 432, "Необходимо указать Дату/время начала работы") }
        params.checkings.add { CheckObj(it.scheludeDateEnd.isNullOrEmpty(), 433, "Необходимо указать Дату/время конца работы") }
        params.checkings.add { CheckObj(it.scheludeDateStart!! >= it.scheludeDateEnd!!, 434, "Дата/время начала работы не может быть равна или больше Даты/времени конца работы") }
        params.checkings.add { CheckObj(repo_clientsschelude.getRepositoryData().find { fil -> fil.idClient == it.idClient && fil.scheludeDateStart == it.scheludeDateStart && fil.scheludeDateEnd == it.scheludeDateEnd } != null, 435, "Запись с передаваемыми параметрами уже присутствует в базе данных") }
        params.checkings.add { CheckObj(repo_clients.getRepositoryData().find { fin -> fin.id == it.idClient } == null, 436, "Не найден Client с id ${it.idClient}") }

        params.onAfterCompleted = {
            val currentDate = LocalDateTime.currentZeroDate().minus((60).days)
            val dataRemove = repo_clientsschelude.getRepositoryData().filter { fil -> fil.scheludeDateEnd!! < currentDate }
            dataRemove.forEach { dat ->
                printTextLog("[DELETE] $dat - on over older by date (${dat.scheludeDateEnd}) < $currentDate")
                dat.delete()
            }
            repo_clientsschelude.resetData()
        }

        return super.post(call, params, serializer)
    }

    suspend fun getFromClient(call: ApplicationCall): ResultResponse {
        try {
            val _idClient = call.parameters["idClient"]

            if (_idClient == null || !_idClient.toIntPossible())
                return ResultResponse.Error(HttpStatusCode(431, ""), "Необходимо указать id Клиента(idClient) записи")

            return ResultResponse.Success(HttpStatusCode.OK, repo_clientsschelude.getRepositoryData().filter { fil -> fil.idClient == _idClient.toInt() })
        } catch (e: Exception) {
            return ResultResponse.Error(HttpStatusCode.Conflict, e.localizedMessage)
        }
    }
}

