package com.example.interfaces

import kotlinx.datetime.LocalDateTime
import org.komapper.core.dsl.metamodel.EntityMetamodel

interface IntPostgreTable <T: Any> {
    val id: Int
    val version: Int
    val createdAt: LocalDateTime

    fun getTable(): EntityMetamodel<T, *, *>
}