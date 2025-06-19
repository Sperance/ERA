package com.example.datamodel.catalogs

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
import io.ktor.server.routing.getAllRoutes
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.util.putAll
import kotlinx.serialization.builtins.ListSerializer

fun Application.configureCatalogs() {
    routing {
        route("/catalogs") {

            get("/structure") {
                call.respond(ResultResponse.Success(Catalogs().getCommentArray()))
            }.apply {
                attributes.putAll(getRouteAttributes(title = "Получение структуры всех полей таблицы"))
            }

            get("/clearTable") {
                Catalogs().clearTable()
                call.respond(ResultResponse.Success("Таблица успешно очищена"))
            }.apply {
                attributes.putAll(getRouteAttributes(title = "Очистка таблицы Catalogs в PostgreSQL"))
            }

            get("/routes") {
                call.respond(ResultResponse.Success(getRoutesInfo("/catalogs")))
            }.apply {
                attributes.putAll(getRouteAttributes(title = "Получение списка всех Маршрутов"))
            }

            secureGet("/count",
                EnumBearerRoles.USER,
                title = "Получение кол-ва объектов таблицы",
                description = "Возвращаются только Не удаленные (поле deleted = false)"
            ) {
                call.respond(ResultResponse.Success(Catalogs().getSize { Catalogs.tbl_catalogs.deleted eq false }))
            }

            get("/errors") {
                call.respond(logObjectProperties(CatalogsErrors, Catalogs()))
            }.apply {
                attributes.putAll(
                    getRouteAttributes(
                        title = "Получить все возможные ошибки Маршрута с их описанием",
                        description = "Временный метод"
                    )
                )
            }

            get("/all") {
                call.respond(Catalogs().get(call))
            }.apply {
                attributes.putAll(
                    getRouteAttributes(
                        title = "Получение всех объектов таблицы",
                        description = "Возвращаются только Не удаленные (поле deleted = false). Временный метод",
                        params = "'page'(Int) - не обязательный параметр. Указание страницы для Пагинации"
                    )
                )
            }

            secureGet("/all/invalid",
                EnumBearerRoles.ADMIN,
                title = "Получение списка всех объектов с некорректными ссылками",
                description = "Возвращаются только Не удаленные (поле deleted = false)"
            ) {
                call.respond(Catalogs().getInvalid(call))
            }

            get("/all/filter") {
                call.respond(Catalogs().getFilter(call))
            }.apply {
                attributes.putAll(
                    getRouteAttributes(
                        title = "Получение всех объектов таблицы по указанным фильтрам",
                        description = "Возвращаются только Не удаленные (поле deleted = false)",
                        params = "'field'(String) - поле, по которому будет работать фильтр\n" +
                                "'state'(String) - команда, по которой будет обрабатываться фильтр (eq, ne, lt, gt, le, ge, contains, not_contains)\n" +
                                "'value'(Any) - значение, которое обрабатывается в поле 'field' по команде 'state'\n" +
                                "'page'(Int) - не обязательный параметр, позволяет получить страницы данных\n"
                    )
                )
            }

            securePost("/update",
                EnumBearerRoles.MODERATOR,
                title = "Обновление указанного объекта таблицы по его id",
                description = "Удалённые объекты (deleted = true) в методе не учавствуют"
            ) {
                call.respond(Catalogs().update(call, RequestParams(), Catalogs.serializer()))
            }

            securePost("",
                EnumBearerRoles.MODERATOR,
                title = "Создание объекта Catalogs",
                description = "Поддерживается передача массива объектов для их создания"
            ) {
                call.respond(Catalogs().post(call, RequestParams(), ListSerializer(Catalogs.serializer())))
            }

            securePost("/restore",
                EnumBearerRoles.MODERATOR,
                title = "Метод 'Восстановления' объекта",
                description = "Выставляет признак deleted=false у объекта по передаваемому id"
            ) {
                call.respond(Catalogs().restore(call, RequestParams()))
            }

            secureDelete("/safe",
                EnumBearerRoles.MODERATOR,
                title = "Удаление объекта, без его удаления из Базы Данных (для возможности его восстановления)",
                description = "Выставляет признак deleted=true у объекта по передаваемому id"
            ) {
                call.respond(Catalogs().deleteSafe(call, RequestParams()))
            }

            secureDelete("",
                EnumBearerRoles.MODERATOR,
                title = "Удаление объекта из Базы данных по его id"
            ) {
                call.respond(Catalogs().delete(call, RequestParams()))
            }
        }
    }
}