package com.example.datamodel.clientsschelude

import com.example.helpers.CommentField
import com.example.currectDatetime
import com.example.basemodel.BaseRepository
import com.example.basemodel.IntBaseDataImpl
import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
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
import org.komapper.annotation.KomapperUpdatedAt
import org.komapper.annotation.KomapperVersion
import org.komapper.core.dsl.Meta

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
    @CommentField("Клиент")
    var idEmployee: Int? = null,
    @CommentField("Работа начало")
    var scheludeDateStart: LocalDateTime? = null,
    @CommentField("Работа конец")
    var scheludeDateEnd: LocalDateTime? = null,
    @Transient
    @KomapperVersion
    override val version: Int = 0,
    @Transient
    @CommentField("Дата создания строки")
    override val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
    @Transient
    @KomapperUpdatedAt
    override val updatedAt: LocalDateTime = LocalDateTime.currectDatetime(),
    @Transient
    override val deleted: Boolean = false
) : IntBaseDataImpl<ClientsSchelude>() {

    companion object {
        val tbl_clientsschelude = Meta.clientsSchelude
        val repo_clientsschelude = BaseRepository(ClientsSchelude())
    }

    override fun getTable() = tbl_clientsschelude
    override fun getRepository() = repo_clientsschelude
    override fun isValidLine(): Boolean {
        return idEmployee != null && scheludeDateStart != null && scheludeDateEnd != null
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<ClientsSchelude>, serializer: KSerializer<List<ClientsSchelude>>): ResultResponse {
        params.checkings.add { ClientsScheludeErrors.ERROR_EMPLOYEE.toCheckObj(it) }
        params.checkings.add { ClientsScheludeErrors.ERROR_EMPLOYEE_DUPLICATE.toCheckObj(it) }
        params.checkings.add { ClientsScheludeErrors.ERROR_SCHELUDEDATESTART.toCheckObj(it) }
        params.checkings.add { ClientsScheludeErrors.ERROR_SCHELUDEDATEEND.toCheckObj(it) }
        params.checkings.add { ClientsScheludeErrors.ERROR_SCHELUDEDATESCOMPARE.toCheckObj(it) }
        params.checkings.add { ClientsScheludeErrors.ERROR_CURRENTDATE.toCheckObj(it) }
        params.checkings.add { ClientsScheludeErrors.ERROR_CURRENTDAY.toCheckObj(it) }

        return super.post(call, params, serializer)
    }

    override suspend fun update(call: ApplicationCall, params: RequestParams<ClientsSchelude>, serializer: KSerializer<ClientsSchelude>): ResultResponse {
        params.checkings.add { ClientsScheludeErrors.ERROR_EMPLOYEE_DUPLICATE_NOTNULL.toCheckObj(it) }
        params.checkings.add { ClientsScheludeErrors.ERROR_SCHELUDEDATESCOMPARE_NOTNULL.toCheckObj(it) }
        params.checkings.add { ClientsScheludeErrors.ERROR_CURRENTDATE_NOTNULL.toCheckObj(it) }

        return super.update(call, params, serializer)
    }
}

