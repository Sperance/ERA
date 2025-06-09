package com.example.datamodel.employees

import com.example.basemodel.CheckObjCondition
import com.example.datamodel.catalogs.Catalogs
import com.example.enums.EnumBearerRoles
import com.example.helpers.getDataFromId
import com.example.helpers.isHaveDataField
import com.example.isNullOrZero
import com.example.security.AESEncryption

object EmployeesErrors {

    val ERROR_FIRSTNAME = CheckObjCondition<Employees>(200,
        { "Необходимо указать Имя сотрудника 'firstName'" },
        { it.first_name.isNullOrEmpty() })

    val ERROR_LASTNAME = CheckObjCondition<Employees>(201,
        { "Необходимо указать Фамилию сотрудника 'lastName'" },
        { it.last_name.isNullOrEmpty() })

    val ERROR_PHONE = CheckObjCondition<Employees>(202,
        { "Необходимо указать Телефон сотрудника 'phone'" },
        { it.phone.isNullOrEmpty() })

    val ERROR_PHONE_NOTNULL = CheckObjCondition<Employees>(202,
        { "Необходимо указать Телефон сотрудника 'phone'" },
        { it.phone != null && it.phone.isNullOrEmpty() })

    val ERROR_EMAIL = CheckObjCondition<Employees>(203,
        { "Необходимо указать Почтовый адрес сотрудника 'email'" },
        { it.email.isNullOrEmpty() })

    val ERROR_GENDER = CheckObjCondition<Employees>(204,
        { "Необходимо указать Пол сотрудника 'gender'" },
        { it.gender.isNullOrZero() })

    val ERROR_LOGIN = CheckObjCondition<Employees>(205,
        { "Необходимо указать Логин сотрудника 'login'" },
        { it.login.isNullOrEmpty() })

    val ERROR_LOGIN_NOTNULL = CheckObjCondition<Employees>(205,
        { "Необходимо указать Логин сотрудника 'login'" },
        { it.login != null && it.login.isNullOrEmpty() })

    val ERROR_ROLE = CheckObjCondition<Employees>(206,
        { "Необходимо указать Роль сотрудника 'role'" },
        { it.role.isNullOrEmpty() })

    val ERROR_ROLE_NOTNULL = CheckObjCondition<Employees>(206,
        { "Необходимо указать Роль сотрудника 'role'" },
        { it.role != null && it.role.isNullOrEmpty() })

    val ERROR_PHONE_DUPLICATE = CheckObjCondition<Employees>(207,
        { "Сотрудник с указанным Номером телефона уже существует" },
        { Employees().isHaveDataField(Employees::phone, AESEncryption.encrypt(it.phone)) })

    val ERROR_EMAIL_DUPLICATE = CheckObjCondition<Employees>(208,
        { "Сотрудник с указанным Почтовым адресом уже существует" },
        { Employees().isHaveDataField(Employees::email, AESEncryption.encrypt(it.email)) })

    val ERROR_EMAIL_DUPLICATE_NOTNULL = CheckObjCondition<Employees>(208,
        { "Сотрудник с указанным Почтовым адресом уже существует" },
        { it.email != null && ERROR_EMAIL_DUPLICATE.condition.invoke(it) })

    val ERROR_LOGIN_DUPLICATE = CheckObjCondition<Employees>(209,
        { "Сотрудник с указанным Логином (${it.login}) уже существует" },
        { Employees().isHaveDataField(Employees::login, it.login) })

    val ERROR_LOGIN_DUPLICATE_NOTNULL = CheckObjCondition<Employees>(209,
        { "Сотрудник с указанным Логином (${it.login}) уже существует" },
        { it.login != null && ERROR_LOGIN_DUPLICATE.condition.invoke(it) })

    val ERROR_POSITION_DUPLICATE_NOTNULL = CheckObjCondition<Employees>(210,
        { "Не найдена Должность с id ${it.position}" },
        { it.position != null && Catalogs().getDataFromId(it.position) == null })

//    val ERROR_ARRAYTYPEWORK_DUPLICATE_NOTNULL = CheckObjCondition<Employees>(211,
//        { "Не найдены Категории с arrayTypeWork ${it.array_type_work?.joinToString()}" },
//        { it.array_type_work != null && !Catalogs.repo_catalogs.isHaveData(it.array_type_work?.toList()) })

    val ERROR_SALT_NOTNULL = CheckObjCondition<Employees>(212,
        { "Попытка модификации системных данных. Информация о запросе передана Администраторам" },
        { it.salt != null })

    val ERROR_ROLE_ENUM = CheckObjCondition<Employees>(213,
        { "Роль Сотрудника (${it.role}) не соответствует одному из доступных: ${EnumBearerRoles.entries.joinToString { role -> role.name }}" },
        { EnumBearerRoles.getFromNameOrNull(it.role) == null })

    val ERROR_ROLE_ADMIN = CheckObjCondition<Employees>(214,
        { "Создание Администраторов запрещено. Обратитесь к разработчикам" },
        { EnumBearerRoles.getFromNameOrNull(it.role) == EnumBearerRoles.ADMIN })

    val ERROR_ROLE_ADMIN_NOTNULL = CheckObjCondition<Employees>(214,
        { "Создание Администраторов запрещено. Обратитесь к разработчикам" },
        { it.role != null && ERROR_ROLE_ADMIN.condition.invoke(it) })

    /***********************************************************/

    val ERROR_LOGINPASSWORD = CheckObjCondition<Employees>(100,
        { "Не найден пользователь с указанным Логином и Паролем" },
        { true })

    val ERROR_PASSWORD = CheckObjCondition<Employees>(101,
        { "Необходимо указать Пароль сотрудника 'password'" },
        { true })

    val ERROR_CLIENTID_PARAMETER = CheckObjCondition<Employees>(102,
        { "Incorrect parameter 'clientId'. This parameter must be 'Int' type" },
        { true })

    val ERROR_SERVICELENGTH_PARAMETER = CheckObjCondition<Employees>(103,
        { "Incorrect parameter 'servceLength'. This parameter must be 'Int' type" },
        { true })

    val ERROR_ID_PARAMETER = CheckObjCondition<Employees>(104,
        { "Incorrect parameter 'id'. This parameter must be 'Int' type" },
        { true })

    val ERROR_DATA_PARAMETER = CheckObjCondition<Employees>(105,
        { "Необходимо указать параметр даты 'data'" },
        { true })

    val ERROR_DATA_INCORRECT_PARAMETER = CheckObjCondition<Employees>(106,
        { "Неверный формат даты" },
        { true })

    val ERROR_ID_NOT_INT_PARAMETER = CheckObjCondition<Employees>(107,
        { "Параметр идентификатора 'id' должен быть Integer" },
        { true })

    val ERROR_ID_DONTFIND = CheckObjCondition<Employees>(108,
        { "Клент с указанным параметром 'id' не найден в базе данных" },
        { true })

    val ERROR_LOGINKEY_DONTFIND = CheckObjCondition<Employees>(109,
        { "Для указанного клиента не найден токен авторизации" },
        { true })
}