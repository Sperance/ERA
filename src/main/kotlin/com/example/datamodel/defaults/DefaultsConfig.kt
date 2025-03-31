package com.example.datamodel.defaults

import com.example.datamodel.catalogs.Catalogs
import com.example.datamodel.clients.Clients
import com.example.datamodel.createBatch
import com.example.datamodel.isEmpty
import kotlinx.coroutines.delay

suspend fun defaultsConfig() {
    delay(3000)
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
                type = "Виды работы"
                category = "Женские стрижки"
                value = "Женские стрижки"
            },
            Catalogs().apply {
                type = "Виды работы"
                category = "Женские стрижки"
                value = "Укладка"
            },
            Catalogs().apply {
                type = "Виды работы"
                category = "Женские стрижки"
                value = "Краска волос 1 тон"
            },
            Catalogs().apply {
                type = "Виды работы"
                category = "Женские стрижки"
                value = "Мелирование"
            },
            Catalogs().apply {
                type = "Виды работы"
                category = "Женские стрижки"
                value = "Осветление"
            },
            Catalogs().apply {
                type = "Виды работы"
                category = "Женские стрижки"
                value = "Мелирование + тонирование"
            },
            Catalogs().apply {
                type = "Виды работы"
                category = "Женские стрижки"
                value = "Осветление + тонирование"
            },
            Catalogs().apply {
                type = "Виды работы"
                category = "Женские стрижки"
                value = "Шлифовка волос"
            },
            Catalogs().apply {
                type = "Виды работы"
                category = "Мужские стрижки"
                value = "Мужские стрижки"
            },
            Catalogs().apply {
                type = "Виды работы"
                category = "Маникюр"
                value = "Маникюр"
            },
            Catalogs().apply {
                type = "Виды работы"
                category = "Маникюр"
                value = "Педикюр"
            },
            Catalogs().apply {
                type = "Виды работы"
                category = "Маникюр"
                value = "Укрепление ногтей"
            },
            Catalogs().apply {
                type = "Виды работы"
                category = "Брови"
                value = "Оформление бровей"
            },
            Catalogs().apply {
                type = "Виды работы"
                category = "Ресницы"
                value = "Наращивание ресниц"
            })
        )
    }
}