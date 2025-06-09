package com.example.datamodel.clients

import com.example.basemodel.CheckObjCondition
import com.example.helpers.isHaveDataField
import com.example.isNullOrZero
import com.example.security.AESEncryption

object ClientsErrors {

    val ERROR_NAME = CheckObjCondition<Clients>(200,
        { "Необходимо указать Имя клиента 'first_name'" },
        { it.first_name.isNullOrEmpty() })

    val ERROR_SURNAME = CheckObjCondition<Clients>(201,
        { "Необходимо указать Фамилию клиента 'last_name'" },
        { it.last_name.isNullOrEmpty() })

    val ERROR_PHONE = CheckObjCondition<Clients>(202,
        { "Необходимо указать Номер телефона клиента 'phone'" },
        { it.phone.isNullOrEmpty() })

    val ERROR_EMAIL = CheckObjCondition<Clients>(203,
        { "Необходимо указать Email клиента 'email'" },
        { it.email.isNullOrEmpty() })

    val ERROR_GENDER = CheckObjCondition<Clients>(204,
        { "Необходимо указать Пол клиента 'gender'" },
        { it.gender.isNullOrZero() })

    val ERROR_LOGIN = CheckObjCondition<Clients>(205,
        { "Необходимо указать Логин клиента 'login'" },
        { it.login.isNullOrEmpty() })

    val ERROR_PHONE_DUPLICATE = CheckObjCondition<Clients>(206,
        { "Клиент с указанным Номером телефона (${it.phone}) уже существует" },
        { Clients().isHaveDataField(Clients::phone, AESEncryption.encrypt(it.phone)) })

    val ERROR_PHONE_DUPLICATE_NUTNULL = CheckObjCondition<Clients>(206,
        { "Клиент с указанным Номером телефона (${it.phone}) уже существует" },
        { it.phone != null && ERROR_PHONE_DUPLICATE.condition.invoke(it) })

    val ERROR_EMAIL_DUPLICATE = CheckObjCondition<Clients>(207,
        { "Клиент с указанным Почтовым адресом (${it.email}) уже существует" },
        { Clients().isHaveDataField(Clients::email, AESEncryption.encrypt(it.email)) })

    val ERROR_EMAIL_DUPLICATE_NOTNULL = CheckObjCondition<Clients>(207,
        { "Клиент с указанным Почтовым адресом (${it.email}) уже существует" },
        { it.email != null && ERROR_EMAIL_DUPLICATE.condition.invoke(it) })

    val ERROR_SALT_NOTNULL = CheckObjCondition<Clients>(208,
        { "Попытка модификации системных данных. Информация о запросе передана Администраторам" },
        { it.salt != null })

    val ERROR_ROLE_NOTNULL = CheckObjCondition<Clients>(208,
        { "Попытка модификации системных данных. Информация о запросе передана Администраторам" },
        { it.role != null })

    val ERROR_LOGIN_DUPLICATE_NOTNULL = CheckObjCondition<Clients>(209,
        { "Клиент с указанным Логином (${it.login}) уже существует" },
        { it.login != null && Clients().isHaveDataField(Clients::login, it.login) })

    val ERROR_PASSWORD = CheckObjCondition<Clients>(210,
        { "Необходимо указать Пароль клиента 'password'" },
        { it.password.isNullOrEmpty() })

    /**************************************************************/

    val ERROR_LOGINPASSWORD = CheckObjCondition<Clients>(100,
        { "Не найден пользователь с указанным Логином и Паролем" },
        { true })

    val ERROR_EMAIL_DONTFIND = CheckObjCondition<Clients>(101,
        { "Не найден Клиент с почтовым адресом ${it.email}" },
        { true })

    val ERROR_SEND_PARAMETER = CheckObjCondition<Clients>(102,
        { "Параметр 'send' должен быть boolean" },
        { true })

    val ERROR_ID_PARAMETER = CheckObjCondition<Clients>(103,
        { "Не указан параметр идентификатора 'id'" },
        { true })

    val ERROR_ID_NOT_INT_PARAMETER = CheckObjCondition<Clients>(104,
        { "Параметр идентификатора 'id' должен быть Integer" },
        { true })

    val ERROR_ID_DONTFIND = CheckObjCondition<Clients>(105,
        { "Клент с указанным параметром 'id' не найден в базе данных" },
        { true })

    val ERROR_LOGINKEY_DONTFIND = CheckObjCondition<Clients>(106,
        { "Для указанного клиента не найден токен авторизации" },
        { true })
}