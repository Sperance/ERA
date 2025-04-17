package com.example.datamodel.records

import com.example.helpers.CommentField
import com.example.helpers.Recordsdata
import com.example.currectDatetime
import com.example.datamodel.BaseRepository
import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.clients.Clients
import com.example.helpers.getSize
import com.example.datamodel.services.Services
import com.example.isNullOrEmpty
import com.example.isNullOrZero
import com.example.nullDatetime
import com.example.printTextLog
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Список записей на приём
 */
@Serializable
@KomapperEntity
@KomapperTable("tbl_records")
data class Records(
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "record_id")
    val id: Int = 0,
    @CommentField("Идентификатор клиента который записался на приём", true)
    var id_client_from: Int? = null,
    @CommentField("Идентификатор сотрудника, к которому записались на приём", true)
    var id_client_to: Int? = null,
    @CommentField("Идентификатор услуги", true)
    var id_service: Int? = null,
    @CommentField("Номер заказа (генерируется автоматически)", false)
    var number: String? = null,
    @CommentField("Дата и время на которую записан клиент", true)
    var dateRecord: LocalDateTime? = null,
    @CommentField("Статус записи (по умолчанию '0 - Создана')", false)
    var status: Int? = null,
    @CommentField("Просмотрен ли заказ после изменения статуса (при смене статуса ставится false)", false)
    var statusViewed: Boolean? = null,
    @CommentField("Стоимость записи", false)
    var price: Double? = null,
    @CommentField("Тип оплаты", false)
    var payType: String? = null,
    @CommentField("Статус оплаты", false)
    var payStatus: Int? = null,
    @Transient
    @KomapperVersion
    val version: Int = 0,
    @Transient
    @CommentField("Дата создания строки", false)
    val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntBaseDataImpl<Records>() {

    companion object {
        val tbl_records = Meta.records
        val repo_records = BaseRepository(Records())
    }

    override fun getBaseId() = id

    suspend fun getFilledRecords() : ArrayList<Recordsdata> {
        val listResults = ArrayList<Recordsdata>()
        val listClients = Clients.repo_clients.getRepositoryData()
        val listServices = Services.repo_services.getRepositoryData()

        repo_records.getRepositoryData().forEach {
            listResults.add(
                Recordsdata(
                clientFrom = listClients.find { f -> f.id == it.id_client_from },
                clientTo = listClients.find { f -> f.id == it.id_client_to },
                service = listServices.find { f -> f.id == it.id_service },
                record = it)
            )
        }
        return listResults
    }

    override suspend fun get(call: ApplicationCall, params: RequestParams<Records>): ResultResponse {
        return ResultResponse.Success(HttpStatusCode.OK, getFilledRecords())
    }

    override suspend fun update(call: ApplicationCall, params: RequestParams<Records>, serializer: KSerializer<Records>): ResultResponse {
        params.checkings.add { CheckObj(it.id_client_from != null && !Clients.repo_clients.isHaveData(it.id_client_from!!), 441, "Не существует Клиента с id ${it.id_client_from}") }
        params.checkings.add { CheckObj(it.id_client_to != null && !Clients.repo_clients.isHaveData(it.id_client_to!!), 442, "Не существует Клиента с id ${it.id_client_to}") }
        params.checkings.add { CheckObj(it.id_service != null && !Services.repo_services.isHaveData(it.id_service!!), 443, "Не существует Услуги с id ${it.id_service}") }

        params.checkOnUpdate = { old: Records, new: Records ->
            if (new.status != null && old.status != new.status) {
                new.statusViewed = false
                printTextLog("[Records - Update statusViewed]: $new")
            }
        }

        return super.update(call, params, serializer)
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<Records>, serializer: KSerializer<List<Records>>): ResultResponse {
        params.checkings.add { CheckObj(it.id_client_from.isNullOrZero(), 430, "Необходимо указать id Клиента который записывается на услугу") }
        params.checkings.add { CheckObj(it.id_client_to.isNullOrZero(), 431, "Необходимо указать id Клиента который будет выполнять услугу") }
        params.checkings.add { CheckObj(it.id_service.isNullOrZero(), 432, "Необходимо указать id Услуги") }
        params.checkings.add { CheckObj(it.dateRecord.isNullOrEmpty(), 433, "Необходимо указать Дату записи") }
        params.checkings.add { CheckObj(it.dateRecord!! <= LocalDateTime.currectDatetime(), 434, "Дата записи не может быть меньше текущей") }
        params.checkings.add { CheckObj(it.id_client_from == it.id_client_to, 435, "ID клиента и сотрудника не могут быть одинаковыми(${it.id_client_from})") }
        params.checkings.add { CheckObj(!Clients.repo_clients.isHaveData(it.id_client_from!!), 441, "Не существует Клиента с id ${it.id_client_from}") }
        params.checkings.add { CheckObj(!Clients.repo_clients.isHaveData(it.id_client_to!!), 442, "Не существует Клиента с id ${it.id_client_to}") }
        params.checkings.add { CheckObj(!Services.repo_services.isHaveData(it.id_service!!), 443, "Не существует Услуги с id ${it.id_service}") }

        params.defaults.add { it::dateRecord to LocalDateTime.nullDatetime() }
        params.defaults.add { it::status to 0 }
        params.defaults.add { it::statusViewed to false }
        params.defaults.add { it::number to generateShortOrderNumber(Records().getSize()) }

        return super.post(call, params, serializer)
    }

    private fun generateShortOrderNumber(allRecords: Long): String {
        val dateFormat = SimpleDateFormat("yyMMdd", Locale.getDefault())
        val datePart = dateFormat.format(Date())
        val randomPart = allRecords % 100 + 1
        return "$datePart-$randomPart"
    }
}