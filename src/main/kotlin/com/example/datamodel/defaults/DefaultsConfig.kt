package com.example.datamodel.defaults

import com.example.datamodel.catalogs.Catalogs
import com.example.datamodel.employees.Employees
import com.example.enums.EnumBearerRoles
import com.example.helpers.createBatch
import com.example.helpers.getSize
import com.example.security.AESEncryption
import com.example.security.generateSalt
import kotlinx.coroutines.runBlocking

fun defaultsConfig() = runBlocking {
    defaultCatalogs()
    defaultEmployees()
}

private suspend fun defaultEmployees() {
    if (Employees().getSize() == 0L) {
        Employees().createBatch("defaultEmployees", listOf(
            Employees().apply {
                firstName = AESEncryption.encrypt("admin")
                lastName = AESEncryption.encrypt("admin")
                login = "admin"
                phone = AESEncryption.encrypt("999")
                salt = generateSalt()
                email = AESEncryption.encrypt("adm@adm.ru")
                role = AESEncryption.encrypt(EnumBearerRoles.ADMIN.name + "_1")
                setNewPassword("Password123.")
            },
            Employees().apply {
                firstName = AESEncryption.encrypt("Dmitriy")
                lastName = AESEncryption.encrypt("MMM")
                login = "tandine"
                phone = AESEncryption.encrypt("+79779999999")
                salt = generateSalt()
                email = AESEncryption.encrypt("mde@mde.ru")
                role = AESEncryption.encrypt(EnumBearerRoles.ADMIN.name + "_2")
                setNewPassword("Eme12345678.")
            })
        )
        Employees.repo_employees.resetData()
    }
}

private suspend fun defaultCatalogs() {
    if (Catalogs().getSize() == 0L) {
        Catalogs().createBatch("defaultCatalogs", listOf(
            Catalogs().apply {
                type = "Типы работ"
                category = "Женские стрижки"
                value = "Женские стрижки"
            },
            Catalogs().apply {
                type = "Типы работ"
                category = "Женские стрижки"
                value = "Укладка"
            },
            Catalogs().apply {
                type = "Типы работ"
                category = "Женские стрижки"
                value = "Краска волос 1 тон"
            },
            Catalogs().apply {
                type = "Типы работ"
                category = "Женские стрижки"
                value = "Мелирование"
            },
            Catalogs().apply {
                type = "Типы работ"
                category = "Женские стрижки"
                value = "Осветление"
            },
            Catalogs().apply {
                type = "Типы работ"
                category = "Женские стрижки"
                value = "Мелирование + тонирование"
            },
            Catalogs().apply {
                type = "Типы работ"
                category = "Женские стрижки"
                value = "Осветление + тонирование"
            },
            Catalogs().apply {
                type = "Типы работ"
                category = "Женские стрижки"
                value = "Шлифовка волос"
            },
            Catalogs().apply {
                type = "Типы работ"
                category = "Мужские стрижки"
                value = "Мужские стрижки"
            },
            Catalogs().apply {
                type = "Типы работ"
                category = "Маникюр"
                value = "Маникюр"
            },
            Catalogs().apply {
                type = "Типы работ"
                category = "Маникюр"
                value = "Педикюр"
            },
            Catalogs().apply {
                type = "Типы работ"
                category = "Маникюр"
                value = "Укрепление ногтей"
            },
            Catalogs().apply {
                type = "Типы работ"
                category = "Брови"
                value = "Оформление бровей"
            },
            Catalogs().apply {
                type = "Типы работ"
                category = "Ресницы"
                value = "Наращивание ресниц"
            },
            Catalogs().apply {
                type = "Типы должностей"
                category = "Должность"
                value = "Сотрудник"
            },
            Catalogs().apply {
                type = "Типы должностей"
                category = "Должность"
                value = "Старший сотрудник"
            },
            Catalogs().apply {
                type = "Типы должностей"
                category = "Должность"
                value = "Администратор"
            })
        )
        Catalogs.repo_catalogs.resetData()
    }
}