package com.example.datamodel.records

import com.example.helpers.CommentField
import com.example.helpers.Recordsdata
import com.example.currectDatetime
import com.example.basemodel.IntBaseDataImpl
import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
import com.example.datamodel.clients.Clients
import com.example.datamodel.clients.Clients.Companion.tbl_clients
import com.example.datamodel.employees.Employees
import com.example.datamodel.employees.Employees.Companion.tbl_employees
import com.example.helpers.getSize
import com.example.datamodel.services.Services
import com.example.datamodel.services.Services.Companion.tbl_services
import com.example.enums.EnumDataFilter
import com.example.generateMapError
import com.example.helpers.getData
import com.example.helpers.getDataFromId
import com.example.helpers.getDataPagination
import com.example.helpers.getWhereDeclarationFilter
import com.example.helpers.haveField
import com.example.logging.DailyLogger.printTextLog
import com.example.plugins.db
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
import org.komapper.annotation.KomapperUpdatedAt
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
    var date_record: LocalDateTime? = null,
    @CommentField("Статус записи (по умолчанию '0 - Создана')")
    var status: Int? = null,
    @CommentField("Просмотрен ли заказ после изменения статуса (при смене статуса ставится false)")
    var status_viewed: Boolean? = null,
    @CommentField("Стоимость записи")
    var price: Double? = null,
    @CommentField("Тип оплаты")
    var pay_type: String? = null,
    @CommentField("Статус оплаты")
    var pay_status: Int? = null,
    @Transient
    @KomapperVersion
    override val version: Int = 0,
    @CommentField("Дата создания строки")
    override val created_at: LocalDateTime = LocalDateTime.currectDatetime(),
    @Transient
    @KomapperUpdatedAt
    override val updated_at: LocalDateTime = LocalDateTime.currectDatetime(),
    @Transient
    override val deleted: Boolean = false
) : IntBaseDataImpl<Records>() {

    companion object {
        val tbl_records = Meta.records
    }

    override fun getTable() = tbl_records
    override fun isValidLine(): Boolean {
        return id_client_from != null && id_employee_to != null && id_service != null && status != null
    }

    suspend fun toRecordsData(): Recordsdata {
        val listClients = Clients().getDataFromId(this.id_client_from)
        val listEmployees = Employees().getDataFromId(this.id_employee_to)
        val listServices = Services().getDataFromId(this.id_service)

        return (Recordsdata(clientFrom = listClients, employeeTo = listEmployees, service = listServices, record = this))
    }

    suspend fun getFilledRecords(statuses: List<Int>? = null) : ArrayList<Recordsdata> {
        val listResults = ArrayList<Recordsdata>()

        val listRecords = getData({ tbl_records.status inList statuses!! })
        val setClients = listRecords.map { it.id_client_from }.toSet().toList()
        val setEmployees = listRecords.map { it.id_employee_to }.toSet().toList()
        val setServices = listRecords.map { it.id_service }.toSet().toList()

        val listClients = Clients().getData({ tbl_clients.id inList setClients })
        val listEmployees = Employees().getData({ tbl_employees.id inList setEmployees })
        val listServices = Services().getData({ tbl_services.id inList setServices })

        listRecords.forEach {
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

        return ResultResponse.Success(getDataFromId(id.toIntOrNull())?.toRecordsData())
    }

    override suspend fun get(call: ApplicationCall): ResultResponse {
        return try {
            val page = call.parameters["page"]?.toIntOrNull()
            if (page == null) {
                ResultResponse.Success(getFilledRecords())
            } else {
                ResultResponse.Success(getDataPagination(page = page).map { it.toRecordsData() })
            }
        } catch (e: Exception) {
            ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage.substringBefore("\n")))
        }
    }

    override suspend fun update(call: ApplicationCall, params: RequestParams<Records>, serializer: KSerializer<Records>): ResultResponse {
        params.checkings.add { RecordsErrors.ERROR_PRICE_LOW0_NOTNULL.toCheckObj(it) }
        params.checkings.add { RecordsErrors.ERROR_IDCLIENTFROM_DUPLICATE_NOTNULL.toCheckObj(it) }
        params.checkings.add { RecordsErrors.ERROR_IDEMPLOYEETO_DUPLICATE_NOTNULL.toCheckObj(it) }
        params.checkings.add { RecordsErrors.ERROR_IDSERVICE_DUPLICATE_NOTNULL.toCheckObj(it) }

        params.checkOnUpdate = { old: Records, new: Records ->
            if (new.status != null && old.status != new.status) {
                new.status_viewed = false
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
        params.defaults.add { it::status_viewed to false }
        params.defaults.add { it::number to generateShortOrderNumber(Records().getSize()) }

        return super.post(call, params, serializer)
    }

    override suspend fun getFilter(call: ApplicationCall): ResultResponse {
        return db.withTransaction { tx ->
            try {
                val field = call.parameters["field"]
                if (field.isNullOrEmpty()) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(generateMapError(call, 301 to "Incorrect parameter 'field'. This parameter must be 'String' type"))
                }

                val state = call.parameters["state"]
                if (state.isNullOrEmpty()) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(generateMapError(call, 302 to "Incorrect parameter 'state'. This parameter must be 'String' type"))
                }

                val value = call.parameters["value"]
                if (value.isNullOrEmpty()) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(generateMapError(call, 303 to "Incorrect parameter 'value'. This parameter must be 'String' type"))
                }

                if (!this.haveField(field)) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(generateMapError(call, 304 to "Class ${this::class.simpleName} dont have field '$field'"))
                }

                val stateEnum = EnumDataFilter.entries.find { it.name == state.uppercase() }
                if (stateEnum == null) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(generateMapError(call, 305 to "Incorrect parameter 'state'(${state}). This parameter must be 'String' type. Allowed: eq, ne, lt, gt, le, ge, contains, not_contains"))
                }
                val page = call.parameters["page"]?.toIntOrNull()?:0

                val resultList = getDataPagination(declaration = getWhereDeclarationFilter(field, stateEnum, value), page).map { it.toRecordsData() }
                return@withTransaction ResultResponse.Success(resultList as Collection<*>)
            } catch (e: Exception) {
                tx.setRollbackOnly()
                return@withTransaction ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage.substringBefore("\n")))
            }
        }
    }

    private fun generateShortOrderNumber(allRecords: Long): String {
        val dateFormat = SimpleDateFormat("yyMMdd", Locale.getDefault())
        val datePart = dateFormat.format(Date())
        val randomPart = allRecords % 100 + 1
        return "$datePart-$randomPart"
    }
}

