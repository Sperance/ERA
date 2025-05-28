package com.example.datamodel.feedbacks

import com.example.basemodel.CheckObjCondition
import com.example.datamodel.clients.Clients.Companion.repo_clients
import com.example.datamodel.employees.Employees.Companion.repo_employees
import com.example.isNullOrZero

object FeedBacksErrors {
    val ERROR_TEXT = CheckObjCondition<FeedBacks>(200,
        { "Необходимо указать Текст отзыва 'text'" },
        { it.text.isNullOrEmpty() })

    val ERROR_VALUE = CheckObjCondition<FeedBacks>(201,
        { "Необходимо указать Оценку отзыва 'value'" },
        { it.value.isNullOrZero() })

    val ERROR_IDCLIENTFROM = CheckObjCondition<FeedBacks>(202,
        { "Необходимо указать id Клиента 'id_client_from' который оставляет отзыв" },
        { it.id_client_from.isNullOrZero() })

    val ERROR_IDEMPLOYEETO = CheckObjCondition<FeedBacks>(203,
        { "Необходимо указать id Сотрудника 'id_employee_to', которому составляется отзыв" },
        { it.id_employee_to.isNullOrZero() })

    val ERROR_VALUE_LOW0 = CheckObjCondition<FeedBacks>(204,
        { "Оценка(${it.value}) не может быть меньше 0" },
        { it.value!! < 0 })

    val ERROR_VALUE_LOW0_NOTNULL = CheckObjCondition<FeedBacks>(204,
        { "Оценка(${it.value}) не может быть меньше 0" },
        { it.value != null && it.value!! < 0 })

    val ERROR_VALUE_GREAT10 = CheckObjCondition<FeedBacks>(205,
        { "Оценка(${it.value}) не может быть больше 10" },
        { it.value!! > 10 })

    val ERROR_VALUE_GREAT10_NOTNULL = CheckObjCondition<FeedBacks>(205,
        { "Оценка(${it.value}) не может быть больше 10" },
        { it.value != null && it.value!! > 10 })

    val ERROR_IDCLIENTFROM_DUPLICATE = CheckObjCondition<FeedBacks>(206,
        { "Не существует Клиента с id ${it.id_client_from}" },
        { !repo_clients.isHaveData(it.id_client_from!!) })

    val ERROR_IDCLIENTFROM_DUPLICATE_NOTNULL = CheckObjCondition<FeedBacks>(206,
        { "Не существует Клиента с id ${it.id_client_from}" },
        { it.id_client_from != null && !repo_clients.isHaveData(it.id_client_from!!) })

    val ERROR_IDEMPLOYEETO_DUPLICATE = CheckObjCondition<FeedBacks>(207,
        { "Не существует Сотрудника с id ${it.id_employee_to}" },
        { !repo_employees.isHaveData(it.id_employee_to!!) })

    val ERROR_IDEMPLOYEETO_DUPLICATE_NOTNULL = CheckObjCondition<FeedBacks>(207,
        { "Не существует Сотрудника с id ${it.id_employee_to}" },
        { it.id_employee_to != null && !repo_employees.isHaveData(it.id_employee_to!!) })
}