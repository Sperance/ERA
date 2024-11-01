package com.example.datamodel

import com.example.plugins.db
import com.example.printTextLog
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.metamodel.getAutoIncrementProperty
import org.komapper.core.dsl.operator.count
import org.komapper.core.dsl.query.singleOrNull
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.createInstance

fun Any.getField(name: String) = this::class.java.declaredFields.find { it.isAccessible = true ; it.name == name }?.get(this)
fun Any.putField(name: String, value: Any?) = this::class.java.declaredFields.find { it.isAccessible = true ; it.name == name }?.set(this, value)

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> TYPE.update() : TYPE {
    val metaTable = getInstanceClassForTbl(this) as META
    return db.runQuery { QueryDsl.update(metaTable).single(this@update) } as TYPE
}

suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> TYPE.isDuplicate(declaration: WhereDeclaration): Boolean {
    val metaTable = getInstanceClassForTbl(this) as META
    return db.runQuery { QueryDsl.from(metaTable).where(declaration).select(count()) } != 0L
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> TYPE.create(kProperty1: KMutableProperty1<TYPE, *>?): TYPE {
    val metaTable = getInstanceClassForTbl(this) as META
    if (kProperty1 != null) {
        val metaProperty = metaTable.properties().find { it.name == kProperty1.name } as PropertyMetamodel<Any, Any, META>
        val already = db.runQuery { QueryDsl.from(metaTable).where { metaProperty eq kProperty1.get(this@create) }.singleOrNull() }
        if (already != null) return already as TYPE
    }
    val result = db.runQuery { QueryDsl.insert(metaTable).single(this@create) }
    return result as TYPE
}

@Suppress("UNCHECKED_CAST")
suspend fun <META : EntityMetamodel<Any, Any, META>> Any.delete() {
    val metaTable = getInstanceClassForTbl(this) as META
    db.runQuery { QueryDsl.delete(metaTable).where { metaTable.getAutoIncrementProperty() as PropertyMetamodel<Any, Int, Int> eq this@delete.getField("id") as Int } }
}

@Suppress("UNCHECKED_CAST")
private fun <META : EntityMetamodel<Any, Any, META>> getInstanceClassForTbl(obj: Any) : META {
    val nameClass = "com.example.datamodel.${obj::class.java.simpleName.lowercase()}.${obj::class.java.simpleName}"
    val instance = Class.forName(nameClass).kotlin.createInstance()
    val metaTable = instance.getField("tbl_${obj::class.java.simpleName.lowercase()}")
        ?: throw IllegalArgumentException("not finded field with name tbl_${obj::class.java.simpleName.lowercase()}) in class $nameClass")

    return metaTable as META
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> TYPE.getData(declaration: WhereDeclaration? = null, sortExpression: SortExpression? = null) : List<TYPE> {
    val metaTable = getInstanceClassForTbl(this) as META
    val whereExpr = declaration ?: {metaTable.getAutoIncrementProperty() as PropertyMetamodel<Any, Int, Int> greaterEq 0}
    return if (sortExpression == null) db.runQuery { QueryDsl.from(metaTable).where(whereExpr) } as List<TYPE>
    else db.runQuery { QueryDsl.from(metaTable).where(whereExpr).orderBy(sortExpression) } as List<TYPE>
}

suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> TYPE.clearTable() {
    val metaTable = getInstanceClassForTbl(this) as META
    db.runQuery { QueryDsl.delete(metaTable).all() }
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> TYPE.getDataOne(declaration: WhereDeclaration? = null, sortExpression: SortExpression? = null) : TYPE? {
    val metaTable = getInstanceClassForTbl(this) as META
    val whereExpr: WhereDeclaration = declaration ?: {metaTable.getAutoIncrementProperty() as PropertyMetamodel<Any, Int, Int> greaterEq 0}
    return if (sortExpression == null) db.runQuery { QueryDsl.from(metaTable).where(whereExpr).singleOrNull() } as TYPE?
    else db.runQuery { QueryDsl.from(metaTable).where(whereExpr).orderBy(sortExpression).singleOrNull() } as TYPE?
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> TYPE.getSize(declaration: WhereDeclaration? = null) : Long {
    val metaTable = getInstanceClassForTbl(this) as META
    val whereExpr: WhereDeclaration = declaration ?: {metaTable.getAutoIncrementProperty() as PropertyMetamodel<Any, Int, Int> greaterEq 0}
    return db.runQuery { QueryDsl.from(metaTable).where(whereExpr).select(count()) }?:0L
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> TYPE.isHaveData(dataId: Int) : Boolean {
    val metaTable = getInstanceClassForTbl(this) as META
    val whereExpr: WhereDeclaration = {metaTable.getAutoIncrementProperty() as PropertyMetamodel<Any, Int, Int> eq dataId}
    val result = (db.runQuery { QueryDsl.from(metaTable).where(whereExpr).select(count()) } ?: 0L) == 0L
    return result
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> TYPE.getFromId(id: Int?) : TYPE? {
    if (id == null) return null
    val metaTable = getInstanceClassForTbl(this) as META
    val whereExpr: WhereDeclaration = {metaTable.getAutoIncrementProperty() as PropertyMetamodel<Any, Int, Int> eq id}
    val result = db.runQuery { QueryDsl.from(metaTable).where(whereExpr).singleOrNull() }
    return result as TYPE?
}