package com.example.datamodel.catalogs

import com.example.basemodel.CheckObjCondition
import com.example.datamodel.catalogs.Catalogs.Companion.repo_catalogs
import com.example.logging.DailyLogger.printTextLog
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

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
        { repo_catalogs.getRepositoryData().find { fin -> fin.category == it.category && fin.value == it.value } != null })
}