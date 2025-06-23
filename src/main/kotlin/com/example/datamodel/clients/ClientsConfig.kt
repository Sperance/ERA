package com.example.datamodel.clients

import com.example.basemodel.RequestParams
import com.example.basemodel.ResultResponse
import com.example.datamodel.authentications.secureDelete
import com.example.datamodel.authentications.secureGet
import com.example.datamodel.authentications.securePost
import com.example.enums.EnumBearerRoles
import com.example.getRouteAttributes
import com.example.getRoutesInfo
import com.example.helpers.clearTable
import com.example.logObjectProperties
import com.example.respond
import com.example.helpers.getSize
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.util.putAll
import kotlinx.serialization.builtins.ListSerializer

fun Application.configureClients() {
    routing {
        route("/clients") {

            get("/structure") {
                call.respond(ResultResponse.Success(Clients().getCommentArray()))
            }.apply {
                attributes.putAll(getRouteAttributes(title = "Получение структуры всех полей таблицы"))
            }

            get("/clearTable") {
                Clients().clearTable()
                call.respond(ResultResponse.Success("Таблица успешно очищена"))
            }.apply {
                attributes.putAll(getRouteAttributes(title = "Очистка таблицы Clients в PostgreSQL"))
            }

            get("/routes") {
                call.respond(ResultResponse.Success(getRoutesInfo("/clients")))
            }.apply {
                attributes.putAll(getRouteAttributes(title = "Получение списка всех Маршрутов"))
            }

            secureGet("/count",
                EnumBearerRoles.USER,
                title = "Получение кол-ва объектов таблицы",
                description = "Возвращаются только Не удаленные (поле deleted = false)"
            ) {
                call.respond(ResultResponse.Success(Clients().getSize { Clients.tbl_clients.deleted eq false }))
            }

            get("/errors") {
                call.respond(logObjectProperties(ClientsErrors, Clients()))
            }.apply {
                attributes.putAll(
                    getRouteAttributes(
                        title = "Получить все возможные ошибки Маршрута с их описанием",
                        description = "Временный метод"
                    )
                )
            }

            secureGet("/all",
                EnumBearerRoles.MODERATOR,
                title = "Получить список всех Клиентов",
                description = "Возвращаются только Не удаленные (поле deleted = false). Временный метод",
                params = mapOf("'page'(Int)" to "не обязательный параметр. Указание страницы для Пагинации")
            ) {
                call.respond(Clients().get(call))
            }

            secureGet("/all/invalid",
                EnumBearerRoles.ADMIN,
                title = "Получение списка всех объектов с некорректными ссылками",
                description = "Возвращаются только Не удаленные (поле deleted = false)"
            ) {
                call.respond(Clients().getInvalid(call))
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
                call.respond(Clients().getFilter(call))
            }

            secureGet("/id",
                EnumBearerRoles.USER,
                title = "Получить одного Клиента по его id"
            ) {
                call.respond(Clients().getFromId(call, RequestParams()))
            }

            post("/auth") {
                call.respond(Clients().auth(call))
            }.apply {
                attributes.putAll(
                    getRouteAttributes(
                        title = "Авторизация Клиента",
                        description = "Проверка идёт по полю login и password"
                    )
                )
            }

            securePost("/update",
                EnumBearerRoles.USER,
                title = "Обновление указанного объекта таблицы по его id",
                description = "Удалённые объекты (deleted = true) в методе не учавствуют"
            ) {
                call.respond(Clients().update(call, RequestParams(), Clients.serializer()))
            }

            post {
                call.respond(Clients().post(call, RequestParams(), ListSerializer(Clients.serializer())))
            }.apply {
                attributes.putAll(
                    getRouteAttributes(
                        title = "Создание объекта Clients",
                        description = "Поддерживается передача массива объектов для их создания"
                    )
                )
            }

            securePost("/recoveryPassword",
                EnumBearerRoles.USER,
                title = "Отправка письма клиенту на его Почтовый адрес",
                description = "Возвращает сгенерированный код для восстановления пароля",
                params = mapOf("'email'(String)" to "email, на который будет отправлено письмо",
                        "'send'(Boolean)" to "отправлять ли письмо на почту")
            ) {
                call.respond(Clients().postRecoveryPassword(call))
            }

            securePost("/changePasswordFromEmail",
                EnumBearerRoles.USER,
                title = "Установка нового пароля для Клиента",
                params = mapOf("'email'(String)" to "email Клиента, для которого будем указывать новый пароль",
                        "'password'(String)" to "новый пароль для Клиента")
            ) {
                call.respond(Clients().changePasswordFromEmail(call))
            }

            securePost("/onExit",
                EnumBearerRoles.USER,
                title = "Удаление токена авторизации для указанного Клиента",
                params = mapOf("'id'(Integer)" to "id Клиента, которого нужно деавторизовать")
            ) {
                call.respond(Clients().onExitSite(call))
            }

            securePost("/restore",
                EnumBearerRoles.MODERATOR,
                title = "Метод 'Восстановления' объекта",
                description = "Выставляет признак deleted=false у объекта по передаваемому id"
            ) {
                call.respond(Clients().restore(call, RequestParams()))
            }

            secureDelete("/safe",
                EnumBearerRoles.USER,
                title = "Удаление объекта, без его удаления из Базы Данных (для возможности его восстановления)",
                description = "Выставляет признак deleted=true у объекта по передаваемому id"
            ) {
                call.respond(Clients().deleteSafe(call, RequestParams()))
            }

            secureDelete("",
                EnumBearerRoles.MODERATOR,
                title = "Удаление объекта из Базы данных по его id"
            ) {
                call.respond(Clients().delete(call, RequestParams()))
            }
        }
    }
}
