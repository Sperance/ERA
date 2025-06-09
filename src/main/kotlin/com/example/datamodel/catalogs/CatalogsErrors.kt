package com.example.datamodel.catalogs

import com.example.basemodel.CheckObjCondition
import com.example.datamodel.catalogs.Catalogs.Companion.tbl_catalogs
import com.example.helpers.getDataOne

object CatalogsErrors {

    val ERROR_TYPE = CheckObjCondition<Catalogs>(200,
        { "Необходимо указать Тип категории 'type'" },
        { it.type.isNullOrEmpty() })

    val ERROR_TYPE_NOTNULL = CheckObjCondition<Catalogs>(200,
        { "Необходимо указать Тип категории 'type'" },
        { it.type != null && it.type.isNullOrEmpty() })

    val ERROR_CATEGORY = CheckObjCondition<Catalogs>(201,
        { "Необходимо указать Категорию 'category'" },
        { it.category.isNullOrEmpty() })

    val ERROR_VALUE = CheckObjCondition<Catalogs>(202,
        { "Необходимо указать Значение категории 'value'"},
        { it.value.isNullOrEmpty() })

    val ERROR_DUPLICATE_ALL = CheckObjCondition<Catalogs>(203,
        { "В БД уже присутствует категория '${it.category}' со значением '${it.value}'. Добавление дубля отменено сервером" },
        { Catalogs().getDataOne({ tbl_catalogs.category eq it.category ; tbl_catalogs.value eq it.value }) != null })
}