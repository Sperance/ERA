package com.example.datamodel.records

import com.example.currectDatetime
import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.clients.Clients
import com.example.datamodel.isHaveData
import com.example.datamodel.services.Services
import com.example.isNullOrEmpty
import com.example.isNullOrZero
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
    val id_client_from: Int? = null,
    /**
     * Идентификатор сотрудника записи
     */
    val id_client_to: Int? = null,
    /**
     * Идентификатор услуги на которую записался клиент
     */
    val id_service: Int? = null,
    /**
     * Номер записи для отображения клиенту
     */
    val number: String? = null,
    /**
     * Дата время на какое число создана запись
     */
    val dateRecord: LocalDateTime? = null,
    /**
     * Статус записи
     */
    val status: String? = null,
    /**
     * Окончательная стоимость записи
     */
    val price: Double? = null,
    /**
     * Тип оплаты
     */
    val payType: String? = null,
    /**
     * Статус оплаты
     */
    val payStatus: Int? = null,
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

    override suspend fun post(call: ApplicationCall, checkings: ArrayList<suspend (Records) -> CheckObj>): ResultResponse {
        checkings.add { CheckObj(it.id_client_from.isNullOrZero(), 430, "Необходимо указать id Клиента который записывается на услугу") }
        checkings.add { CheckObj(it.id_client_to.isNullOrZero(), 431, "Необходимо указать id Клиента который будет выполнять услугу") }
        checkings.add { CheckObj(it.id_service.isNullOrZero(), 432, "Необходимо указать id Услуги") }
        checkings.add { CheckObj(it.number.isNullOrEmpty(), 433, "Необходимо указать Номер записи для клиента") }
        checkings.add { CheckObj(it.dateRecord.isNullOrEmpty(), 434, "Необходимо указать Дату записи") }
        checkings.add { CheckObj(Clients().isHaveData(it.id_client_from!!), 441, "Не существует Клиента с id ${it.id_client_from}") }
        checkings.add { CheckObj(Clients().isHaveData(it.id_client_to!!), 442, "Не существует Клиента с id ${it.id_client_to}") }
        checkings.add { CheckObj(Services().isHaveData(it.id_service!!), 443, "Не существует Услуги с id ${it.id_service}") }
        return super.post(call, checkings)
    }
}