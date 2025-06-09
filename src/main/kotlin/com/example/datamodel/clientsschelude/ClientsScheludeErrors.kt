package com.example.datamodel.clientsschelude

import com.example.basemodel.CheckObjCondition
import com.example.datamodel.clientsschelude.ClientsSchelude.Companion.tbl_clientsschelude
import com.example.datamodel.employees.Employees
import com.example.helpers.getData
import com.example.helpers.getDataFromId
import com.example.isNullOrEmpty
import com.example.isNullOrZero

object ClientsScheludeErrors {

    val ERROR_EMPLOYEE = CheckObjCondition<ClientsSchelude>(200,
        { "Необходимо указать id сотрудника 'id_employee' для графика работы" },
        { it.id_employee.isNullOrZero() })

    val ERROR_EMPLOYEE_DUPLICATE = CheckObjCondition<ClientsSchelude>(201,
        { "Не найден сотрудник с id ${it.id_employee}" },
        { Employees().getDataFromId(it.id_employee) == null })

    val ERROR_EMPLOYEE_DUPLICATE_NOTNULL = CheckObjCondition<ClientsSchelude>(201,
        { "Не найден сотрудник с id ${it.id_employee}" },
        { it.id_employee != null && ERROR_EMPLOYEE_DUPLICATE.condition.invoke(it) })

    val ERROR_SCHELUDEDATESTART = CheckObjCondition<ClientsSchelude>(202,
        { "Необходимо указать Дату/время начала работы 'schelude_date_start'" },
        { it.schelude_date_start.isNullOrEmpty() })

    val ERROR_SCHELUDEDATEEND = CheckObjCondition<ClientsSchelude>(203,
        { "Необходимо указать Дату/время окончания работы 'schelude_date_end'" },
        { it.schelude_date_end.isNullOrEmpty() })

    val ERROR_SCHELUDEDATESCOMPARE = CheckObjCondition<ClientsSchelude>(204,
        { "Дата/время начала работы не может быть больше Даты/времени конца работы" },
        { it.schelude_date_start!! > it.schelude_date_end!! })

    val ERROR_SCHELUDEDATESCOMPARE_NOTNULL = CheckObjCondition<ClientsSchelude>(204,
        { "Дата/время начала работы не может быть больше Даты/времени конца работы" },
        { it.schelude_date_start != null && it.schelude_date_end != null && ERROR_SCHELUDEDATESCOMPARE.condition.invoke(it) })

    val ERROR_CURRENTDATE = CheckObjCondition<ClientsSchelude>(205,
        { "Даты в графике начала и окончания работ должны быть одинаковы (один день)" },
        { it.schelude_date_start!!.dayOfYear != it.schelude_date_end!!.dayOfYear })

    val ERROR_CURRENTDATE_NOTNULL = CheckObjCondition<ClientsSchelude>(205,
        { "Даты в графике начала и окончания работ должны быть одинаковы (один день)" },
        { it.schelude_date_start != null && it.schelude_date_end != null && ERROR_CURRENTDATE.condition.invoke(it) })

    val ERROR_CURRENTDAY = CheckObjCondition<ClientsSchelude>(206,
        { "На выбранную дату уже есть занятый слот. Редактируйте его" },
        { it.schelude_date_start!!.dayOfYear == it.schelude_date_end!!.dayOfYear && ClientsSchelude().getData({ tbl_clientsschelude.id_employee eq it.id_employee }).find { dt -> dt.schelude_date_start!!.dayOfYear == it.schelude_date_start!!.dayOfYear } != null })

    val ERROR_CURRENTDAY_NOTNULL = CheckObjCondition<ClientsSchelude>(206,
        { "На выбранную дату уже есть занятый слот. Редактируйте его" },
        { it.schelude_date_start != null && it.schelude_date_end != null && ERROR_CURRENTDAY.condition.invoke(it) })

}