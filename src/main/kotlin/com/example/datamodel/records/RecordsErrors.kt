package com.example.datamodel.records

import com.example.basemodel.CheckObjCondition
import com.example.currectDatetime
import com.example.datamodel.clients.Clients
import com.example.datamodel.employees.Employees
import com.example.datamodel.services.Services
import com.example.helpers.getDataFromId
import com.example.isNullOrEmpty
import com.example.isNullOrZero
import kotlinx.datetime.LocalDateTime

object RecordsErrors {

    val ERROR_IDCLIENTFROM = CheckObjCondition<Records>(200,
        { "Необходимо указать id Клиента 'id_client_from' который записывается на услугу" },
        { it.id_client_from.isNullOrZero() })

    val ERROR_IDEMPLOYEETO = CheckObjCondition<Records>(201,
        { "Необходимо указать id Сотрудника 'id_employee_to' который будет выполнять услугу" },
        { it.id_employee_to.isNullOrZero() })

    val ERROR_IDSERVICE = CheckObjCondition<Records>(202,
        { "Необходимо указать id Услуги 'id_service'" },
        { it.id_service.isNullOrZero() })

    val ERROR_DATERECORD = CheckObjCondition<Records>(203,
        { "Необходимо указать Дату записи 'dateRecord'" },
        { it.date_record.isNullOrEmpty() })

    val ERROR_DATERECORD_LOWCURRENT = CheckObjCondition<Records>(204,
        { "Дата записи 'dateRecord' не может быть меньше текущей" },
        { it.date_record!! <= LocalDateTime.currectDatetime() })

    val ERROR_IDCLIENTFROM_DUPLICATE = CheckObjCondition<Records>(205,
        { "Не существует Клиента с id ${it.id_client_from}" },
        { Clients().getDataFromId(it.id_client_from) == null })

    val ERROR_IDCLIENTFROM_DUPLICATE_NOTNULL = CheckObjCondition<Records>(205,
        { "Не существует Клиента с id ${it.id_client_from}" },
        { it.id_client_from != null && ERROR_IDCLIENTFROM_DUPLICATE.condition.invoke(it) })

    val ERROR_IDEMPLOYEETO_DUPLICATE = CheckObjCondition<Records>(206,
        { "Не существует Сотрудника с id ${it.id_employee_to}" },
        { Employees().getDataFromId(it.id_employee_to) == null })

    val ERROR_IDEMPLOYEETO_DUPLICATE_NOTNULL = CheckObjCondition<Records>(206,
        { "Не существует Сотрудника с id ${it.id_employee_to}" },
        { it.id_employee_to != null && ERROR_IDEMPLOYEETO_DUPLICATE.condition.invoke(it) })

    val ERROR_IDSERVICE_DUPLICATE = CheckObjCondition<Records>(207,
        { "Не существует Услуги с id ${it.id_service}" },
        { Services().getDataFromId(it.id_service) == null })

    val ERROR_IDSERVICE_DUPLICATE_NOTNULL = CheckObjCondition<Records>(207,
        { "Не существует Услуги с id ${it.id_service}" },
        { it.id_service != null && ERROR_IDSERVICE_DUPLICATE.condition.invoke(it) })

    val ERROR_PRICE_LOW0_NOTNULL = CheckObjCondition<Records>(208,
        { "Стоимость услуги 'price'(${it.price}) не может быть меньше 0" },
        { it.price != null && it.price!! < 0 })

}