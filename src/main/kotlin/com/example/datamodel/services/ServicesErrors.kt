package com.example.datamodel.services

import com.example.basemodel.CheckObjCondition
import com.example.datamodel.catalogs.Catalogs
import com.example.helpers.getDataFromId
import com.example.isNullOrZero

object ServicesErrors {

    val ERROR_NAME = CheckObjCondition<Services>(200,
        { "Необходимо указать Наименование услуги 'name'" },
        { it.name.isNullOrEmpty() })

    val ERROR_PRICELOW = CheckObjCondition<Services>(201,
        { "Необходимо указать Минимальную стоимость услуги 'priceLow'" },
        { it.price_low.isNullOrZero() })

    val ERROR_DURATION = CheckObjCondition<Services>(202,
        { "Необходимо указать Продолжительность услуги 'duration'" },
        { it.duration.isNullOrZero() })

    val ERROR_CATEGORY = CheckObjCondition<Services>(203,
        { "Необходимо указать Категорию услуги 'category'" },
        { it.category.isNullOrZero() })

    val ERROR_PRICELOWMAX_NOTNULL = CheckObjCondition<Services>(204,
        { "Максимальная стоимость услуги 'priceMax'(${it.price_max}) не может быть меньше минимальной 'price_low'(${it.price_low})" },
        { (it.price_max != null && it.price_low != null) && (it.price_max!! < it.price_low!!) })

    val ERROR_CATEGORY_DUPLICATE = CheckObjCondition<Services>(205,
        { "Не найдена Категория 'category' с id ${it.category}" },
        { Catalogs().getDataFromId(it.category) == null })

    val ERROR_CATEGORY_DUPLICATE_NOTNULL = CheckObjCondition<Services>(205,
        { "Не найдена Категория 'category' с id ${it.category}" },
        { it.category != null && ERROR_CATEGORY_DUPLICATE.condition.invoke(it) })
}