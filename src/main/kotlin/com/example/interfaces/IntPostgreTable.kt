package com.example.interfaces

import kotlinx.datetime.LocalDateTime
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel

interface IntPostgreTable <T: Any> {
    val id: Int
    val version: Int
    val createdAt: LocalDateTime
    val updatedAt: LocalDateTime
    val deleted: Boolean

    fun getTable(): EntityMetamodel<T, *, *>
}