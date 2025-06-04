package com.example.datamodel.clientsschelude

import com.example.basemodel.CheckObjCondition
import com.example.datamodel.clientsschelude.ClientsSchelude.Companion.repo_clientsschelude
import com.example.datamodel.employees.Employees.Companion.repo_employees
import com.example.isNullOrEmpty
import com.example.isNullOrZero

object ClientsScheludeErrors {

    val ERROR_EMPLOYEE = CheckObjCondition<ClientsSchelude>(200,
        { "Необходимо указать id сотрудника 'idEmployee' для графика работы" },
        { it.idEmployee.isNullOrZero() })

    val ERROR_EMPLOYEE_DUPLICATE = CheckObjCondition<ClientsSchelude>(201,
        { "Не найден сотрудник с id ${it.idEmployee}" },
        { !repo_employees.isHaveData(it.idEmployee) })

    val ERROR_EMPLOYEE_DUPLICATE_NOTNULL = CheckObjCondition<ClientsSchelude>(201,
        { "Не найден сотрудник с id ${it.idEmployee}" },
        { it.idEmployee != null && ERROR_EMPLOYEE_DUPLICATE.condition.invoke(it) })

    val ERROR_SCHELUDEDATESTART = CheckObjCondition<ClientsSchelude>(202,
        { "Необходимо указать Дату/время начала работы" },
        { it.scheludeDateStart.isNullOrEmpty() })

    val ERROR_SCHELUDEDATEEND = CheckObjCondition<ClientsSchelude>(203,
        { "Необходимо указать Дату/время окончания работы" },
        { it.scheludeDateEnd.isNullOrEmpty() })

    val ERROR_SCHELUDEDATESCOMPARE = CheckObjCondition<ClientsSchelude>(204,
        { "Дата/время начала работы не может быть больше Даты/времени конца работы" },
        { it.scheludeDateStart!! > it.scheludeDateEnd!! })

    val ERROR_SCHELUDEDATESCOMPARE_NOTNULL = CheckObjCondition<ClientsSchelude>(204,
        { "Дата/время начала работы не может быть больше Даты/времени конца работы" },
        { it.scheludeDateStart != null && it.scheludeDateEnd != null && ERROR_SCHELUDEDATESCOMPARE.condition.invoke(it) })

    val ERROR_CURRENTDATE = CheckObjCondition<ClientsSchelude>(205,
        { "Даты в графике начала и окончания работ должны быть одинаковы (один день)" },
        { it.scheludeDateStart!!.dayOfYear != it.scheludeDateEnd!!.dayOfYear })

    val ERROR_CURRENTDATE_NOTNULL = CheckObjCondition<ClientsSchelude>(205,
        { "Даты в графике начала и окончания работ должны быть одинаковы (один день)" },
        { it.scheludeDateStart != null && it.scheludeDateEnd != null && ERROR_CURRENTDATE.condition.invoke(it) })

    val ERROR_CURRENTDAY = CheckObjCondition<ClientsSchelude>(206,
        { "На выбранную дату уже есть занятый слот. Редактируйте его" },
        { it.scheludeDateStart!!.dayOfYear == it.scheludeDateEnd!!.dayOfYear && repo_clientsschelude.getRepositoryData().find { dt -> dt.scheludeDateStart!!.dayOfYear == it.scheludeDateStart!!.dayOfYear && dt.idEmployee == it.idEmployee } != null })

    val ERROR_CURRENTDAY_NOTNULL = CheckObjCondition<ClientsSchelude>(206,
        { "На выбранную дату уже есть занятый слот. Редактируйте его" },
        { it.scheludeDateStart != null && it.scheludeDateEnd != null && ERROR_CURRENTDAY.condition.invoke(it) })

}