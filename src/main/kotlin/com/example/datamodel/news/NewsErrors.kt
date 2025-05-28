package com.example.datamodel.news

import com.example.basemodel.CheckObjCondition

object NewsErrors {

    val ERROR_MAINTEXT = CheckObjCondition<News>(200,
        { "Необходимо указать Текст новости 'mainText'" },
        { it.mainText.isNullOrEmpty() })

    val ERROR_NAME = CheckObjCondition<News>(201,
        { "Необходимо указать Наименование новости 'name'" },
        { it.name.isNullOrEmpty() })

}