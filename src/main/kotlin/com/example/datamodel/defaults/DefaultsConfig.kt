package com.example.datamodel.defaults

import com.example.currectDatetime
import com.example.datamodel.catalogs.Catalogs
import com.example.datamodel.clients.Clients
import com.example.helpers.createBatch
import com.example.helpers.isEmpty
import com.example.security.AESEncryption
import com.example.security.generateSalt
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime

fun defaultsConfig() = runBlocking {
    defaultCatalogs()
    defaultClients()
}

private suspend fun defaultClients() {
    if (Clients().isEmpty()) {
        Clients().createBatch("defaultClients", listOf(
            Clients().apply {
                firstName = AESEncryption.encrypt("admin")
                lastName = AESEncryption.encrypt("admin")
                login = "admin"
                phone = AESEncryption.encrypt("999")
                salt = generateSalt()
                email = AESEncryption.encrypt("adm@adm.ru")
                clientType = "ADMIN"
                dateWorkIn = LocalDateTime.currectDatetime()
                setNewPassword("Password123.")
            },
            Clients().apply {
                firstName = AESEncryption.encrypt("Dmitriy")
                lastName = AESEncryption.encrypt("MMM")
                login = "tandine"
                phone = AESEncryption.encrypt("+79779999999")
                salt = generateSalt()
                email = AESEncryption.encrypt("mde@mde.ru")
                clientType = "ADMIN"
                dateWorkIn = LocalDateTime.currectDatetime()
                setNewPassword("Ckjy32543254.")
            })
        )
    }
}

private suspend fun defaultCatalogs() {
    if (Catalogs().isEmpty()) {
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
            })
        )
    }
}