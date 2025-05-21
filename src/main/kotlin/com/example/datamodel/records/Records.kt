package com.example.datamodel.records

import com.example.helpers.CommentField
import com.example.helpers.Recordsdata
import com.example.currectDatetime
import com.example.basemodel.BaseRepository
import com.example.basemodel.CheckObj
import com.example.basemodel.IntBaseDataImpl
import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
import com.example.datamodel.clients.Clients
import com.example.datamodel.employees.Employees
import com.example.helpers.getSize
import com.example.datamodel.services.Services
import com.example.enums.EnumHttpCode
import com.example.generateMapError
import com.example.isNullOrEmpty
import com.example.isNullOrZero
import com.example.logging.DailyLogger.printTextLog
import com.example.nullDatetime
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
    override fun baseParams(): RequestParams<Records> {
        val params = RequestParams<Records>()
        params.checkings.add { CheckObj(it.id_client_from != null && !Clients.repo_clients.isHaveData(it.id_client_from!!), EnumHttpCode.NOT_FOUND, 201, "Не существует Клиента с id ${it.id_client_from}") }
        params.checkings.add { CheckObj(it.id_employee_to != null && !Employees.repo_employees.isHaveData(it.id_employee_to!!), EnumHttpCode.NOT_FOUND, 202, "Не существует Клиента с id ${it.id_employee_to}") }
        params.checkings.add { CheckObj(it.id_service != null && !Services.repo_services.isHaveData(it.id_service!!), EnumHttpCode.NOT_FOUND, 203, "Не существует Услуги с id ${it.id_service}") }
        return params
    }

    suspend fun getFilledRecords() : ArrayList<Recordsdata> {
        val listResults = ArrayList<Recordsdata>()
        val listClients = Clients.repo_clients.getRepositoryData()
        val listEmployees = Employees.repo_employees.getRepositoryData()
        val listServices = Services.repo_services.getRepositoryData()

        repo_records.getRepositoryData().forEach {
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

    suspend fun getFromId(call: ApplicationCall): ResultResponse {
        val id = call.parameters["id"]
        if (id == null || !id.toIntPossible()) {
            return ResultResponse.Error(EnumHttpCode.INCORRECT_PARAMETER, generateMapError(call, 101 to "Incorrect parameter 'id'($id). This parameter must be 'Int' type"))
        }

        return ResultResponse.Success(EnumHttpCode.COMPLETED, getFilledRecords().find { it.record?.id == id.toIntOrNull() })
    }

    override suspend fun get(call: ApplicationCall, params: RequestParams<Records>): ResultResponse {
        return ResultResponse.Success(EnumHttpCode.COMPLETED, getFilledRecords())
    }

    override suspend fun update(call: ApplicationCall, params: RequestParams<Records>, serializer: KSerializer<Records>): ResultResponse {
        params.checkOnUpdate = { old: Records, new: Records ->
            if (new.status != null && old.status != new.status) {
                new.statusViewed = false
                printTextLog("[Records - Update statusViewed]: $new")
            }
        }

        return super.update(call, params, serializer)
    }

    override suspend fun post(call: ApplicationCall, params: RequestParams<Records>, serializer: KSerializer<List<Records>>): ResultResponse {
        params.checkings.add { CheckObj(it.id_client_from.isNullOrZero(), EnumHttpCode.INCORRECT_PARAMETER, 301, "Необходимо указать id Клиента который записывается на услугу") }
        params.checkings.add { CheckObj(it.id_employee_to.isNullOrZero(), EnumHttpCode.INCORRECT_PARAMETER, 302, "Необходимо указать id Сотрудника который будет выполнять услугу") }
        params.checkings.add { CheckObj(it.id_service.isNullOrZero(), EnumHttpCode.INCORRECT_PARAMETER, 303, "Необходимо указать id Услуги") }
        params.checkings.add { CheckObj(it.dateRecord.isNullOrEmpty(), EnumHttpCode.INCORRECT_PARAMETER, 304, "Необходимо указать Дату записи") }
        params.checkings.add { CheckObj(it.dateRecord!! <= LocalDateTime.currectDatetime(), EnumHttpCode.INCORRECT_PARAMETER, 305, "Дата записи не может быть меньше текущей") }
        params.checkings.add { CheckObj(!Clients.repo_clients.isHaveData(it.id_client_from!!), EnumHttpCode.NOT_FOUND, 307, "Не существует Клиента с id ${it.id_client_from}") }
        params.checkings.add { CheckObj(!Employees.repo_employees.isHaveData(it.id_employee_to!!), EnumHttpCode.NOT_FOUND, 308, "Не существует Сотрудника с id ${it.id_employee_to}") }
        params.checkings.add { CheckObj(!Services.repo_services.isHaveData(it.id_service!!), EnumHttpCode.NOT_FOUND, 309, "Не существует Услуги с id ${it.id_service}") }

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

