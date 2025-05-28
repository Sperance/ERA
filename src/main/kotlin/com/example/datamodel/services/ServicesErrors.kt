package com.example.datamodel.services

import com.example.basemodel.CheckObjCondition
import com.example.datamodel.catalogs.Catalogs.Companion.repo_catalogs
import com.example.isNullOrZero

object ServicesErrors {

    val ERROR_NAME = CheckObjCondition<Services>(200,
        { "Необходимо указать Наименование услуги 'name'" },
        { it.name.isNullOrEmpty() })

    val ERROR_PRICELOW = CheckObjCondition<Services>(201,
        { "Необходимо указать Минимальную стоимость услуги 'priceLow'" },
        { it.priceLow.isNullOrZero() })

    val ERROR_DURATION = CheckObjCondition<Services>(202,
        { "Необходимо указать Продолжительность услуги 'duration'" },
        { it.duration.isNullOrZero() })

    val ERROR_CATEGORY = CheckObjCondition<Services>(203,
        { "Необходимо указать Категорию услуги 'category'" },
        { it.category.isNullOrZero() })

    val ERROR_PRICELOWMAX_NOTNULL = CheckObjCondition<Services>(204,
        { "Максимальная стоимость услуги 'priceMax'(${it.priceMax}) не может быть меньше минимальной 'priceLow'(${it.priceLow})" },
        { (it.priceMax != null && it.priceLow != null) && (it.priceMax!! < it.priceLow!!) })

    val ERROR_CATEGORY_DUPLICATE = CheckObjCondition<Services>(205,
        { "Не найдена Категория 'category' с id ${it.category}" },
        { !repo_catalogs.isHaveData(it.category) })

    val ERROR_CATEGORY_DUPLICATE_NOTNULL = CheckObjCondition<Services>(205,
        { "Не найдена Категория 'category' с id ${it.category}" },
        { it.category != null && !repo_catalogs.isHaveData(it.category) })
}