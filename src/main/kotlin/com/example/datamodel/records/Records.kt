package com.example.datamodel.records

import com.example.Recordsdata
import com.example.currectDatetime
import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.clients.Clients
import com.example.datamodel.clients.Clients.Companion.tbl_clients
import com.example.datamodel.getData
import com.example.datamodel.getDataOne
import com.example.datamodel.getFromId
import com.example.datamodel.getSize
import com.example.datamodel.isHaveData
import com.example.datamodel.services.Services
import com.example.isNullOrEmpty
import com.example.isNullOrZero
import com.example.nullDatetime
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
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
import java.util.UUID

/**
 * Список записей на приём
 */
@Serializable
@KomapperEntity
@KomapperTable("tbl_records")
data class Records(
    /**
     * Идентификатор записи в БД (заполняется автоматически)
     */
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "record_id")
    val id: Int = 0,
    /**
     * Идентификатор клиента создавшего запись
     */
    var id_client_from: Int? = null,
    /**
     * Идентификатор сотрудника записи
     */
    var id_client_to: Int? = null,
    /**
     * Идентификатор услуги на которую записался клиент
     */
    var id_service: Int? = null,
    /**
     * Номер записи для отображения клиенту
     */
    var number: String? = null,
    /**
     * Дата время на какое число создана запись
     */
    var dateRecord: LocalDateTime? = null,
    /**
     * Статус записи
     */
    var status: String? = null,
    /**
     * Окончательная стоимость записи
     */
    var price: Double? = null,
    /**
     * Тип оплаты
     */
    var payType: String? = null,
    /**
     * Статус оплаты
     */
    var payStatus: Int? = null,
    /**
     * Версия обновлений записи (заполняется автоматически)
     */
    @KomapperVersion
    val version: Int = 0,
    /**
     * Дата создания записи (заполняется автоматически)
     */
    val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntBaseDataImpl<Records>() {

    companion object {
        val tbl_records = Meta.records
    }

    override suspend fun get(
        call: ApplicationCall,
        params: RequestParams<Records>
    ): ResultResponse {
        
        val listResults = ArrayList<Recordsdata>()
        val listClients = Clients().getData()

        getData().forEach {
            listResults.add(Recordsdata(
                clientFrom = listClients.find { f -> f.id == it.id_client_from },
                clientTo = listClients.find { f -> f.id == it.id_client_to },
                service = Services().getFromId(it.id_service),
                record = it))
        }

        return ResultResponse.Success(HttpStatusCode.OK, listResults)
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<Records>): ResultResponse {
        params.checkings.add { CheckObj(it.id_client_from.isNullOrZero(), 430, "Необходимо указать id Клиента который записывается на услугу") }
        params.checkings.add { CheckObj(it.id_client_to.isNullOrZero(), 431, "Необходимо указать id Клиента который будет выполнять услугу") }
        params.checkings.add { CheckObj(it.id_service.isNullOrZero(), 432, "Необходимо указать id Услуги") }
        params.checkings.add { CheckObj(it.dateRecord.isNullOrEmpty(), 433, "Необходимо указать Дату записи") }
        params.checkings.add { CheckObj(it.dateRecord!! <= LocalDateTime.currectDatetime(), 434, "Дата записи не может быть меньше текущей") }
        params.checkings.add { CheckObj(it.id_client_from == it.id_client_to, 435, "ID клиента и сотрудника не могут быть одинаковыми(${it.id_client_from})") }
        params.checkings.add { CheckObj(Clients().isHaveData(it.id_client_from!!), 441, "Не существует Клиента с id ${it.id_client_from}") }
        params.checkings.add { CheckObj(Clients().isHaveData(it.id_client_to!!), 442, "Не существует Клиента с id ${it.id_client_to}") }
        params.checkings.add { CheckObj(Services().isHaveData(it.id_service!!), 443, "Не существует Услуги с id ${it.id_service}") }

        params.defaults.add { it::dateRecord to LocalDateTime.nullDatetime() }
        params.defaults.add { it::status to "Создана" }
        params.defaults.add { it::number to generateShortOrderNumber(Records().getSize()) }

        return super.post(call, params)
    }

    private fun generateShortOrderNumber(allRecords: Long): String {
        val dateFormat = SimpleDateFormat("yyMMdd", Locale.getDefault())
        val datePart = dateFormat.format(Date())
        val randomPart = allRecords % 100 + 1
        return "$datePart-$randomPart"
    }
}