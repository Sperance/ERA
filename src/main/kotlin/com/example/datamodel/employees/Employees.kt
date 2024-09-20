package com.example.datamodel.employees

import com.example.plugins.db
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

@Serializable
@KomapperEntity
@KomapperTable("tbl_employees")
data class Employees(
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "employee_id")
    val id: Int = 0,

    /**
     * Имя сотрудника
     */
    var name: String = "",
    /**
     * Фамилия сотрудника
     */
    var surname: String = "",
    /**
     * Отчество сотрудника (при наличии)
     */
    var patronymic: String = "",
    /**
     * Пароль в личный кабинет сотрудника
     */
    var password: String = "",
    /**
     * Мобильный телефон сотрудника
     */
    var phone: String = "",
    /**
     * Дата рождения сотрудника
     */
    var dateBirthday: Long = 0L,
    /**
     * Дата принятия на работу сотрудника
     */
    var dateWorkIn: Long = 0L,
    /**
     * Дата увольнения сотрудника
     */
    var dateWorkOut: Long = 0L,
    /**
     * Ссылка на картинку работника (аватарка)
     */
    var imageProfileLink: String = "",
    /**
     * Оклад сотрудника
     */
    var salary : Double = 0.0,
    /**
     * Уволен ли сотрудник
     */
    var isFired: Boolean = false,

    @KomapperVersion
    val version: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
) {

    companion object {
        val tbl_employees = Meta.employees
    }

    suspend fun isDuplicate(declaration: WhereDeclaration): Boolean {
        return db.runQuery { QueryDsl.from(tbl_employees).where(declaration).select(count()) } == 0L
    }
}