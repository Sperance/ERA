package com.example.datamodel.defaults

import com.example.datamodel.catalogs.Catalogs
import com.example.datamodel.clients.Clients
import com.example.helpers.createBatch
import com.example.helpers.isEmpty
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun defaultsConfig() = runBlocking {
    delay(2000)
    defaultCatalogs()
    defaultClients()
}

private suspend fun defaultClients() {
    if (Clients().isEmpty()) {
        Clients().createBatch(listOf(
            Clients().apply {
                firstName = "admin"
                lastName = "admin"
                login = "admin"
                password = "Password123."
                phone = "999"
                email = "adm@adm.ru"
                clientType = "admin"
            },
            Clients().apply {
                firstName = "Dmitriy"
                lastName = "MMM"
                login = "tandine"
                password = "32543254"
                phone = "+79779999999"
                email = "mde@mde.ru"
            })
        )
    }
}

private suspend fun defaultCatalogs() {
    if (Catalogs().isEmpty()) {
        Catalogs().createBatch(listOf(
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