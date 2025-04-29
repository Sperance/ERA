package com.example.datamodel.clientsschelude

import com.example.helpers.CommentField
import com.example.currectDatetime
import com.example.basemodel.BaseRepository
import com.example.basemodel.CheckObj
import com.example.basemodel.IntBaseDataImpl
import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
import com.example.datamodel.clients.Clients.Companion.repo_clients
import com.example.enums.EnumHttpCode
import com.example.isNullOrEmpty
import com.example.isNullOrZero
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
import org.komapper.core.dsl.metamodel.EntityMetamodel

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
    override val id: Int = 0,
    @CommentField("Клиент", true)
    var idClient: Int? = null,
    @CommentField("Работа начало", true)
    var scheludeDateStart: LocalDateTime? = null,
    @CommentField("Работа конец", true)
    var scheludeDateEnd: LocalDateTime? = null,
    @Transient
    @KomapperVersion
    override val version: Int = 0,
    @Transient
    @CommentField("Дата создания строки", false)
    override val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntBaseDataImpl<ClientsSchelude>() {

    companion object {
        val tbl_clientsschelude = Meta.clientsSchelude
        val repo_clientsschelude = BaseRepository(ClientsSchelude())
    }

    override fun getTable() = tbl_clientsschelude
    override fun getRepository() = repo_clientsschelude
    override fun baseParams(): RequestParams<ClientsSchelude> {
        val params = RequestParams<ClientsSchelude>()
        params.checkings.add { CheckObj(it.idClient != null && !repo_clients.isHaveData(it.idClient), EnumHttpCode.NOT_FOUND, 201, "Не найден Client с id ${it.idClient}") }
        params.checkings.add { CheckObj(it.scheludeDateStart.isNullOrEmpty(), EnumHttpCode.INCORRECT_PARAMETER, 202, "Необходимо указать Дату/время начала работы") }
        params.checkings.add { CheckObj(it.scheludeDateEnd.isNullOrEmpty(), EnumHttpCode.INCORRECT_PARAMETER, 203, "Необходимо указать Дату/время конца работы") }
        params.checkings.add { CheckObj(it.scheludeDateStart!! >= it.scheludeDateEnd!!, EnumHttpCode.INCORRECT_PARAMETER, 204, "Дата/время начала работы не может быть равна или больше Даты/времени конца работы") }
        return params
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<ClientsSchelude>, serializer: KSerializer<List<ClientsSchelude>>): ResultResponse {
        params.checkings.add { CheckObj(it.idClient.isNullOrZero(), EnumHttpCode.INCORRECT_PARAMETER, 301, "Необходимо указать id Клиента для графика работы") }
        params.checkings.add { CheckObj(!repo_clients.isHaveData(it.idClient), EnumHttpCode.NOT_FOUND, 302, "Не найден Client с id ${it.idClient}") }

        return super.post(call, params, serializer)
    }
}

