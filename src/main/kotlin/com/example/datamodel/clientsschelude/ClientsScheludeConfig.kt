package com.example.datamodel.clientsschelude

import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
import com.example.datamodel.authentications.secureDelete
import com.example.datamodel.authentications.secureGet
import com.example.datamodel.authentications.securePost
import com.example.enums.EnumBearerRoles
import com.example.getRouteAttributes
import com.example.getRoutesInfo
import com.example.helpers.clearTable
import com.example.helpers.getSize
import com.example.logObjectProperties
import com.example.respond
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.util.putAll
import kotlinx.serialization.builtins.ListSerializer

fun Application.configureClientsSchelude() {
    routing {
        route("/clientsschelude") {

            get("/structure") {
                call.respond(ResultResponse.Success(ClientsSchelude().getCommentArray()))
            }.apply {
                attributes.putAll(getRouteAttributes(title = "Получение структуры всех полей таблицы"))
            }

            get("/clearTable") {
                ClientsSchelude().clearTable()
                call.respond(ResultResponse.Success("Таблица успешно очищена"))
            }.apply {
                attributes.putAll(getRouteAttributes(title = "Очистка таблицы Clientsschelude в PostgreSQL"))
            }

            get("/routes") {
                call.respond(ResultResponse.Success(getRoutesInfo("/clientsschelude")))
            }.apply {
                attributes.putAll(getRouteAttributes(title = "Получение списка всех Маршрутов"))
            }

            secureGet("/count",
                EnumBearerRoles.USER,
                title = "Получение кол-ва объектов таблицы",
                description = "Возвращаются только Не удаленные (поле deleted = false)"
            ) {
                call.respond(ResultResponse.Success(ClientsSchelude().getSize { ClientsSchelude.tbl_clientsschelude.deleted eq false }))
            }

            get("/errors") {
                call.respond(logObjectProperties(ClientsScheludeErrors, ClientsSchelude()))
            }.apply {
                attributes.putAll(
                    getRouteAttributes(
                        title = "Получить все возможные ошибки Маршрута с их описанием",
                        description = "Временный метод"
                    )
                )
            }

            secureGet("/all",
                EnumBearerRoles.USER,
                title = "Получить список всех Клиентов",
                description = "Возвращаются только Не удаленные (поле deleted = false). Временный метод",
                params = mapOf("'page'(Int)" to "не обязательный параметр. Указание страницы для Пагинации")
            ) {
                call.respond(ClientsSchelude().get(call))
            }

            secureGet("/all/invalid",
                EnumBearerRoles.ADMIN,
                title = "Получение списка всех объектов с некорректными ссылками",
                description = "Возвращаются только Не удаленные (поле deleted = false)"
            ) {
                call.respond(ClientsSchelude().getInvalid(call))
            }

            secureGet("/all/filter",
                EnumBearerRoles.USER,
                title = "Получить список всех Клиентов с Фильтрами",
                description = "Возвращаются только Не удаленные (поле deleted = false)",
                params = mapOf(
                    "'field'(String)" to "поле, по которому будет работать фильтр",
                    "'state'(String)" to "команда, по которой будет обрабатываться фильтр (eq, ne, lt, gt, le, ge, contains, not_contains)",
                    "'value'(Any)" to "значение, которое обрабатывается в поле 'field' по команде 'state'",
                    "'page'(Int)" to "не обязательный параметр, позволяет получить страницы данных")
            ) {
                call.respond(ClientsSchelude().getFilter(call))
            }

            securePost("/update",
                EnumBearerRoles.MODERATOR,
                title = "Обновление указанного объекта таблицы по его id",
                description = "Удалённые объекты (deleted = true) в методе не учавствуют"
            ) {
                call.respond(ClientsSchelude().update(call, RequestParams(), ClientsSchelude.serializer()))
            }

            securePost("",
                EnumBearerRoles.MODERATOR,
                title = "Создание объекта Графика работ",
                description = "Поддерживается передача массива объектов для их создания"
            ) {
                call.respond(ClientsSchelude().post(call, RequestParams(), ListSerializer(ClientsSchelude.serializer())))
            }

            securePost("/restore",
                EnumBearerRoles.MODERATOR,
                title = "Метод 'Восстановления' объекта",
                description = "Выставляет признак deleted=false у объекта по передаваемому id"
            ) {
                call.respond(ClientsSchelude().restore(call, RequestParams()))
            }

            secureDelete("/safe",
                EnumBearerRoles.MODERATOR,
                title = "Удаление объекта, без его удаления из Базы Данных (для возможности его восстановления)",
                description = "Выставляет признак deleted=true у объекта по передаваемому id"
            ) {
                call.respond(ClientsSchelude().deleteSafe(call, RequestParams()))
            }

            secureDelete("",
                EnumBearerRoles.MODERATOR,
                title = "Удаление объекта из Базы данных по его id"
            ) {
                call.respond(ClientsSchelude().delete(call, RequestParams()))
            }
        }
    }
}