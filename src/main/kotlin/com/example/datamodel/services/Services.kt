package com.example.datamodel.services

import com.example.currectDatetime
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable
import org.komapper.annotation.KomapperVersion
import org.komapper.core.dsl.Meta

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
     * Подкатегория услуги (при наличии)
     */
    var subCategory: String = "",
    /**
     * Стоимость услуги в рублях
     */
    var price: Double = 0.0,
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
) {

    companion object {
        val tbl_services = Meta.services
    }
}