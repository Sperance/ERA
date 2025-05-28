package com.example.datamodel.stockfiles

import com.example.basemodel.CheckObjCondition
import com.example.datamodel.services.Services.Companion.repo_services

object StockfilesErrors {

    val ERROR_CATEGORY = CheckObjCondition<Stockfiles>(200,
        { "Необходимо указать Категорию файла 'category'" },
        { it.category.isNullOrEmpty() })

    val ERROR_SERVICE_DUPLICATE = CheckObjCondition<Stockfiles>(201,
        { "Не найдена Услуга с id ${it.service}" },
        { !repo_services.isHaveData(it.service) })

    val ERROR_SERVICE_DUPLICATE_NOTNULL = CheckObjCondition<Stockfiles>(201,
        { "Не найдена Услуга с id ${it.service}" },
        { it.service != null && !repo_services.isHaveData(it.service) })

}