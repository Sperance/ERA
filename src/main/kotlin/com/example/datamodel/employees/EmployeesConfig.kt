package com.example.datamodel.employees

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
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.util.putAll
import kotlinx.serialization.builtins.ListSerializer

fun Application.configureEmployees() {
    routing {
        route("/employees") {
            get("/structure") {
                call.respond(ResultResponse.Success(Employees().getCommentArray()))
            }.apply {
                attributes.putAll(getRouteAttributes(title = "Получение структуры всех полей таблицы"))
            }

            get("/clearTable") {
                Employees().clearTable()
                call.respond(ResultResponse.Success("Таблица успешно очищена"))
            }.apply {
                attributes.putAll(getRouteAttributes(title = "Очистка таблицы Employees в PostgreSQL"))
            }

            get("/routes") {
                call.respond(ResultResponse.Success(getRoutesInfo("/employees")))
            }.apply {
                attributes.putAll(getRouteAttributes(title = "Получение списка всех Маршрутов"))
            }

            secureGet("/count",
                EnumBearerRoles.USER,
                title = "Получение кол-ва объектов таблицы",
                description = "Возвращаются только Не удаленные (поле deleted = false)"
            )  {
                call.respond(ResultResponse.Success(Employees().getSize { Employees.tbl_employees.deleted eq false }))
            }

            secureGet("/timeslot/{clientId}/{servceLength}",
                EnumBearerRoles.USER,
                title = "Получить свободные таймслоты по выбранному Сотруднику и Продолжительности работы",
                params = "'clientId'(Integer) - id клиента для которого нужно получить слоты\n" +
                        "'servceLength'(Integer) - продолжительность услуги"
            ) {
                call.respond(Employees().getTimeSlots(call))
            }

            get("/errors") {
                call.respond(logObjectProperties(EmployeesErrors, Employees()))
            }.apply {
                attributes.putAll(
                    getRouteAttributes(
                        title = "Получить все возможные ошибки Маршрута с их описанием",
                        description = "Временный метод"
                    )
                )
            }

            get("/all") {
                call.respond(Employees().get(call))
            }.apply {
                attributes.putAll(
                    getRouteAttributes(
                        title = "Получить список всех Клиентов",
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
                call.respond(Employees().getInvalid(call))
            }

            get("/all/filter") {
                call.respond(Employees().getFilter(call))
            }.apply {
                attributes.putAll(
                    getRouteAttributes(
                        title = "Получить список всех Сотрудников с Фильтрами",
                        description = "Возвращаются только Не удаленные (поле deleted = false)",
                        params = "'field'(String) - поле, по которому будет работать фильтр\n" +
                                "'state'(String) - команда, по которой будет обрабатываться фильтр (eq, ne, lt, gt, le, ge, contains, not_contains)\n" +
                                "'value'(Any) - значение, которое обрабатывается в поле 'field' по команде 'state'\n" +
                                "'page'(Int) - не обязательный параметр, позволяет получить страницы данных\n"
                    )
                )
            }

            secureGet("/id",
                EnumBearerRoles.MODERATOR,
                title = "Получить одного Сотрудника по его id") {
                call.respond(Employees().getFromId(call, RequestParams()))
            }

            secureGet("/slots/{id}/{data}",
                EnumBearerRoles.USER,
                title = "Получить заказы Сотрудника по его id на указаннаю дату",
                params = "'id'(Integer) - id Сотрудника\n" +
                        "'data'(DateTime) - дата, на которую нужно получить заказы"
            ) {
                call.respond(Employees().getSlots(call))
            }

            post("/auth") {
                call.respond(Employees().auth(call))
            }.apply {
                attributes.putAll(
                    getRouteAttributes(
                        title = "Авторизация Сотрудника",
                        description = "Проверка идёт по полю login и password"
                    )
                )
            }

            securePost("/update",
                EnumBearerRoles.USER,
                title = "Обновление указанного объекта таблицы по его id",
                description = "Удалённые объекты (deleted = true) в методе не учавствуют"
            ) {
                call.respond(Employees().update(call, RequestParams(), Employees.serializer()))
            }

            securePost("",
                EnumBearerRoles.MODERATOR,
                title = "Создание объекта Employees",
                description = "Поддерживается передача массива объектов для их создания"
            ) {
                call.respond(Employees().post(call, RequestParams(), ListSerializer(Employees.serializer())))
            }

            securePost("/onExit",
                EnumBearerRoles.USER,
                title = "Удаление токена авторизации для указанного Сотрудника",
                params = "'id'(Integer) - id Сотрудника, которого нужно деавторизовать"
            ) {
                call.respond(Employees().onExitSite(call))
            }

            securePost("/restore",
                EnumBearerRoles.MODERATOR,
                title = "Метод 'Восстановления' объекта",
                description = "Выставляет признак deleted=false у объекта по передаваемому id"
            ) {
                call.respond(Employees().restore(call, RequestParams()))
            }

            secureDelete("/safe",
                EnumBearerRoles.MODERATOR,
                title = "Удаление объекта, без его удаления из Базы Данных (для возможности его восстановления)",
                description = "Выставляет признак deleted=true у объекта по передаваемому id"
            ) {
                call.respond(Employees().deleteSafe(call, RequestParams()))
            }

            secureDelete("",
                EnumBearerRoles.MODERATOR,
                title = "Удаление объекта из Базы данных по его id"
            ) {
                call.respond(Employees().delete(call, RequestParams()))
            }
        }
    }
}