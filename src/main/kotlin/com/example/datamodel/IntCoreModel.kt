package com.example.datamodel

import org.komapper.core.dsl.expression.WhereDeclaration

interface IntCoreModel <T>  {
    suspend fun create() : T
    suspend fun update() : T
    suspend fun delete() { println("delete") }

    suspend fun isDuplicate(declaration: WhereDeclaration) : Boolean
}