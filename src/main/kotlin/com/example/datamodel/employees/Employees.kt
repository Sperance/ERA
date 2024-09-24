package com.example.datamodel.employees

import com.example.currectDatetime
import com.example.nullDatetime
import com.example.plugins.db
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable
import org.komapper.annotation.KomapperVersion
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.operator.count

/**
 * Список работников.
 */
@Serializable
@KomapperEntity
@KomapperTable("tbl_employees")
data class Employees(
    /**
     * Идентификатор сотрудника в БД (заполняется автоматически)
     */
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "employee_id")
    val id: Int = 0,
    /**
     * Имя сотрудника (обязательно к заполнению)
     */
    var firstName: String = "",
    /**
     * Фамилия сотрудника (обязательно к заполнению)
     */
    var lastName: String = "",
    /**
     * Отчество сотрудника (при наличии)
     */
    var patronymic: String = "",
    /**
     * Пароль в личный кабинет сотрудника (обязательно к заполнению)
     */
    var password: String = "",
    /**
     * Мобильный телефон сотрудника
     */
    var phone: String = "",
    /**
     * Дата рождения сотрудника (вида "2000-01-01T00:00")
     */
    var dateBirthday: LocalDateTime = LocalDateTime.nullDatetime(),
    /**
     * Дата принятия на работу сотрудника (вида "2000-01-01T00:00")
     */
    var dateWorkIn: LocalDateTime = LocalDateTime.nullDatetime(),
    /**
     * Дата увольнения сотрудника (вида "2000-01-01T00:00")
     */
    var dateWorkOut: LocalDateTime = LocalDateTime.nullDatetime(),
    /**
     * Ссылка на картинку работника (аватарка)
     */
    var imageProfileLink: String = "",
    /**
     * Должность
     */
    var position: String = "",
    /**
     * Оклад сотрудника
     */
    var salary : Double = 0.0,
    /**
     * Уволен ли сотрудник
     */
    var isFired: Boolean = false,
    /**
     * Версия обновлений записи сотрудника (заполняется автоматически)
     */
    @KomapperVersion
    val version: Int = 0,
    /**
     * Дата создания записи (заполняется автоматически)
     */
    val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) {

    companion object {
        val tbl_employees = Meta.employees
    }

    /**
     * Задаётся условие [declaration] для поиска записей по нему в БД.
     * @return нашлась ли хотя бы одна запись с заданным условием.
     */
    suspend fun isDuplicate(declaration: WhereDeclaration): Boolean {
        return db.runQuery { QueryDsl.from(tbl_employees).where(declaration).select(count()) } == 0L
    }
}