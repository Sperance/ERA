package com.example.datamodel.authentications

import com.example.basemodel.CheckObjCondition

object AuthenticationsErrors {

    val ERROR_ID_PARAM = CheckObjCondition<Authentications>(100,
        { "Необходимо указать id пользователя 'id'" },
        { true })

    val ERROR_ID_NOT_INT_PARAM = CheckObjCondition<Authentications>(101,
        { "Параметр 'id' должен быть Integer" },
        { true })

    val ERROR_EMPLOYEE_PARAM = CheckObjCondition<Authentications>(102,
        { "Необходимо указать тип пользователя 'employee' (true/false)" },
        { true })

    val ERROR_EMPLOYEE_NOT_BOOL_PARAM = CheckObjCondition<Authentications>(103,
        { "Параметр 'employee' должен быть Boolean" },
        { true })

    val ERROR_AUTH_NOTFOUND = CheckObjCondition<Authentications>(104,
        { "Не найдена запись с указанным id" },
        { true })

    val ERROR_JWT = CheckObjCondition<Authentications>(105,
        { "RoleAwareJWT token is null" },
        { true })

}