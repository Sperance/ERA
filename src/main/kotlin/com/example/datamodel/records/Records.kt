package com.example.datamodel.records

import com.example.helpers.CommentField
import com.example.helpers.Recordsdata
import com.example.currectDatetime
import com.example.basemodel.BaseRepository
import com.example.basemodel.IntBaseDataImpl
import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
import com.example.datamodel.clients.Clients
import com.example.datamodel.employees.Employees
import com.example.helpers.getSize
import com.example.datamodel.services.Services
import com.example.generateMapError
import com.example.logging.DailyLogger.printTextLog
import com.example.toIntPossible
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
    override val id: Int = 0,
    @CommentField("Идентификатор клиента который записался на приём")
    var id_client_from: Int? = null,
    @CommentField("Идентификатор сотрудника, к которому записались на приём")
    var id_employee_to: Int? = null,
    @CommentField("Идентификатор услуги")
    var id_service: Int? = null,
    @CommentField("Номер заказа (генерируется автоматически)")
    var number: String? = null,
    @CommentField("Дата и время на которую записан клиент")
    var dateRecord: LocalDateTime? = null,
    @CommentField("Статус записи (по умолчанию '0 - Создана')")
    var status: Int? = null,
    @CommentField("Просмотрен ли заказ после изменения статуса (при смене статуса ставится false)")
    var statusViewed: Boolean? = null,
    @CommentField("Стоимость записи")
    var price: Double? = null,
    @CommentField("Тип оплаты")
    var payType: String? = null,
    @CommentField("Статус оплаты")
    var payStatus: Int? = null,
    @Transient
    @KomapperVersion
    override val version: Int = 0,
    @Transient
    @CommentField("Дата создания строки")
    override val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
    @Transient
    override val deleted: Boolean = false
) : IntBaseDataImpl<Records>() {

    companion object {
        val tbl_records = Meta.records
        val repo_records = BaseRepository(Records())
    }

    override fun getTable() = tbl_records
    override fun getRepository() = repo_records
    override fun isValidLine(): Boolean {
        return id_client_from != null && id_employee_to != null && id_service != null && status != null
    }

    suspend fun toRecordsData(): Recordsdata {
        val listClients = Clients.repo_clients.getRepositoryData()
        val listEmployees = Employees.repo_employees.getRepositoryData()
        val listServices = Services.repo_services.getRepositoryData()

        return (Recordsdata(
            clientFrom = listClients.find { f -> f.id == this.id_client_from },
            employeeTo = listEmployees.find { f -> f.id == this.id_employee_to },
            service = listServices.find { f -> f.id == this.id_service },
            record = this)
        )
    }

    suspend fun getFilledRecords(statuses: Collection<Int>? = null) : ArrayList<Recordsdata> {
        val listResults = ArrayList<Recordsdata>()
        val listClients = Clients.repo_clients.getRepositoryData()
        val listEmployees = Employees.repo_employees.getRepositoryData()
        val listServices = Services.repo_services.getRepositoryData()

        repo_records.getRepositoryData().filter { statuses?.contains(it.status)?:true }.forEach {
            listResults.add(
                Recordsdata(
                clientFrom = listClients.find { f -> f.id == it.id_client_from },
                employeeTo = listEmployees.find { f -> f.id == it.id_employee_to },
                service = listServices.find { f -> f.id == it.id_service },
                record = it)
            )
        }
        return listResults
    }

    override suspend fun getFromId(call: ApplicationCall, params: RequestParams<Records>): ResultResponse {
        val id = call.parameters["id"]
        if (id == null || !id.toIntPossible()) {
            return ResultResponse.Error(generateMapError(call, 101 to "Incorrect parameter 'id'($id). This parameter must be 'Int' type"))
        }

        return ResultResponse.Success(getFilledRecords().find { it.record?.id == id.toIntOrNull() })
    }

    override suspend fun get(call: ApplicationCall): ResultResponse {
        return ResultResponse.Success(getFilledRecords())
    }

    override suspend fun update(call: ApplicationCall, params: RequestParams<Records>, serializer: KSerializer<Records>): ResultResponse {
        params.checkings.add { RecordsErrors.ERROR_PRICE_LOW0_NOTNULL.toCheckObj(it) }
        params.checkings.add { RecordsErrors.ERROR_IDCLIENTFROM_DUPLICATE_NOTNULL.toCheckObj(it) }
        params.checkings.add { RecordsErrors.ERROR_IDEMPLOYEETO_DUPLICATE_NOTNULL.toCheckObj(it) }
        params.checkings.add { RecordsErrors.ERROR_IDSERVICE_DUPLICATE_NOTNULL.toCheckObj(it) }

        params.checkOnUpdate = { old: Records, new: Records ->
            if (new.status != null && old.status != new.status) {
                new.statusViewed = false
                printTextLog("[Records::update] Update statusViewed: $new")
            }
        }

        return super.update(call, params, serializer)
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<Records>, serializer: KSerializer<List<Records>>): ResultResponse {
        params.checkings.add { RecordsErrors.ERROR_IDCLIENTFROM.toCheckObj(it) }
        params.checkings.add { RecordsErrors.ERROR_IDEMPLOYEETO.toCheckObj(it) }
        params.checkings.add { RecordsErrors.ERROR_IDSERVICE.toCheckObj(it) }
        params.checkings.add { RecordsErrors.ERROR_DATERECORD.toCheckObj(it) }
        params.checkings.add { RecordsErrors.ERROR_DATERECORD_LOWCURRENT.toCheckObj(it) }
        params.checkings.add { RecordsErrors.ERROR_PRICE_LOW0_NOTNULL.toCheckObj(it) }
        params.checkings.add { RecordsErrors.ERROR_IDCLIENTFROM_DUPLICATE.toCheckObj(it) }
        params.checkings.add { RecordsErrors.ERROR_IDEMPLOYEETO_DUPLICATE.toCheckObj(it) }
        params.checkings.add { RecordsErrors.ERROR_IDSERVICE_DUPLICATE.toCheckObj(it) }

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

