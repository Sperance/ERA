package com.example.datamodel.services

import com.example.currectDatetime
import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.create
import com.example.datamodel.delete
import com.example.datamodel.getDataOne
import com.example.datamodel.update
import com.example.toIntPossible
import com.example.updateFromNullable
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable
import org.komapper.annotation.KomapperVersion
import org.komapper.core.dsl.Meta
import kotlin.reflect.KClass

@Serializable
@Suppress
data class ServicesNullable(
    val id: Int? = null,
    var name: String? = null,
    var description: String? = null,
    var category: String? = null,
    var priceLow: Double? = null,
    var priceMax: Double? = null,
    var duration: Byte? = null,
    var gender: Byte? = null,
    var imageLink: String? = null
)

/**
 * Список услуг.
 */
@Serializable
@KomapperEntity
@KomapperTable("tbl_services")
data class Services(
    /**
     * Идентификатор услуги в БД (заполняется автоматически)
     */
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "services_id")
    val id: Int = 0,

    /**
     * Наименование услуги (обязательно к заполнению)
     */
    var name: String = "",
    /**
     * Описание услуги
     */
    var description: String = "",
    /**
     * Категория, к которой относится услуга
     */
    var category: String = "",
    /**
     * Минимальная стоимость услуги в рублях
     */
    var priceLow: Double = 0.0,
    /**
     * Максимальная стоимость услуги в рублях
     */
    var priceMax: Double = 0.0,
    /**
     * Продолжительность услуги (1 пункт = 15 мин, например если услуга длится 60 мин - то поле в будет 4)
     */
    var duration: Byte = 0,
    /**
     * К какому полу относится услуга ("-1" - к любому, "0" - к мужскому, "1" - к женскому) [по умолчанию -1]
     */
    var gender: Byte = -1,
    /**
     * Ссылка на изображение услуги
     */
    var imageLink: String = "",
    /**
     * Версия обновлений записи услуги (заполняется автоматически)
     */
    @KomapperVersion
    val version: Int = 0,
    /**
     * Дата создания услуги (заполняется автоматически)
     */
    val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntBaseDataImpl<Services>() {

    companion object {
        val tbl_services = Meta.services
    }

    /**
     * Метод на получение всех записей [Services]
     *
     * <b>Sample usage</b>
     * ```
     * GET : /services/all
     * ```
     *
     * <b>Return codes</b>
     *
     * 400 при возникновении необработанной ошибки
     *
     * @return 200 при успешной отправке ответа
     */
    override suspend fun get(call: ApplicationCall): ResultResponse {
        return super.get(call)
    }

    /**
     * Метод на получение записи [Services] по параметру [id]
     *
     * <b>Sample usage</b>
     * ```
     * GET : /services/1
     * ```
     *
     * <b>Return codes</b>
     *
     * 400 при ошибке входящего параметра [id]
     *
     * 404 если запись [Services] по запрашиваемоу [id] не найдена в БД
     *
     * @return 200 при успешной отправке ответа
     */
    override suspend fun getId(call: ApplicationCall): ResultResponse {
        return super.getId(call)
    }

    /**
     * Метод на создание записи класса [Services]
     *
     * <b>Return codes</b>
     *
     * 430 Необходимо указать Наименование [Services.name]
     *
     * 431 Необходимо указать Стоимость [Services.price] (не должна быть меньше 0.1)
     *
     * 432 Необходимо указать Категорию [Services.category]
     *
     * 433 Необходимо указать Продолжительность [Services.duration] (1пункт = 15минут, 60мин = 4пункта)
     *
     * 400 при возникновении необработанной ошибки
     *
     * @return 201 при успешном создании объекта
     */
    override suspend fun post(call: ApplicationCall): ResultResponse {
        try {
            val newService = call.receive<Services>()
            if (newService.name.isBlank())
                return ResultResponse.Error(HttpStatusCode(430, "name must be initialized"), "Необходимо указать Наименование")

            if (newService.priceLow < 0.1)
                return ResultResponse.Error(HttpStatusCode(431, "priceLow must be initialized"), "Необходимо указать Стоимость мин")

            if (newService.priceMax < 0.1)
                return ResultResponse.Error(HttpStatusCode(431, "priceMax must be initialized"), "Необходимо указать Стоимость макс")

            if (newService.category.isBlank())
                return ResultResponse.Error(HttpStatusCode(432, "category must be initialized"), "Необходимо указать Категорию")

            if (newService.duration == 0.toByte())
                return ResultResponse.Error(HttpStatusCode(433, "duration must be initialized"), "Необходимо указать Продолжительность (1пункт = 15минут, 60мин = 4пункта)")

            val finish = newService.create(null).result
            return ResultResponse.Success(HttpStatusCode.Created, "Successfully created Service with id ${finish.id}")
        } catch (e: Exception) {
            return ResultResponse.Error(HttpStatusCode.BadRequest, e.localizedMessage)
        }
    }

    /**
     * Метод на обновление объекта записи [Services] по параметру [id]
     *
     * Обязательно наличие поля [id] в запросе. Обновление записи происходит по нему.
     * Далее передавайте только те поля "ключ:значение" - которые необходимо обновить.
     * Другие поля которые вы не указывали в запросе - обновлены не будут
     *
     * <b>Return codes</b>
     *
     * 400 при ошибке входящего параметра [id]
     *
     * 404 если запись [Services] по запрашиваемоу [id] не найдена в БД
     *
     * 400 при возникновении необработанной ошибки
     *
     * @return 200 при успешном обновлении объекта
     */
    override suspend fun update(call: ApplicationCall, kclass: KClass<*>): ResultResponse {
        return super.update(call, kclass)
    }

    /**
     * Метод на удаление объекта записи [Services] по параметру [id]
     *
     * <b>Sample usage</b>
     * ```
     * DELETE : /services?id=1
     * ```
     *
     * <b>Return codes</b>
     *
     * 400 при ошибке входящего параметра [id]
     *
     * 404 если запись [Services] по запрашиваемоу [id] не найдена в БД
     *
     * 400 при возникновении необработанной ошибки
     *
     * @return 204 при успешном удалении объекта
     */
    override suspend fun delete(call: ApplicationCall): ResultResponse {
        return super.delete(call)
    }
}