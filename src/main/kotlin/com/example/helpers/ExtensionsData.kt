package com.example.helpers

import com.example.basemodel.IntBaseDataImpl
import com.example.enums.EnumSQLTypes
import com.example.interfaces.IntPostgreTable
import com.example.logging.DailyLogger.printTextLog
import com.example.plugins.db
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.metamodel.getAutoIncrementProperty
import org.komapper.core.dsl.operator.count
import org.komapper.core.dsl.query.get
import org.komapper.core.dsl.query.singleOrNull

fun Any.haveField(name: String) = this::class.java.declaredFields.find { it.isAccessible = true ; it.name == name } != null
fun Any.getField(name: String) = this::class.java.declaredFields.find { it.isAccessible = true ; it.name == name }?.get(this)
fun Any.putField(name: String, value: Any?) = this::class.java.declaredFields.find { it.isAccessible = true ; it.name == name }?.set(this, value)

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> IntPostgreTable<TYPE>.update() : TYPE {
    val metaTable = getTable() as META
    val result = db.runQuery { QueryDsl.update(metaTable).single(this@update).returning() } as TYPE
    printTextLog("[Update object '${this::class.java.simpleName}' with id '${result.getField("id")}']")
    return result
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> IntPostgreTable<TYPE>.create(): TYPE {
    val metaTable = getTable() as META
    val result = db.runQuery { QueryDsl.insert(metaTable).single(this@create) }
    printTextLog("[Create object '${this::class.java.simpleName}' with id '${result.getField("id")}']")
    return result as TYPE
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> IntPostgreTable<TYPE>.createBatch(values: List<TYPE>): TYPE {
    val metaTable = getTable() as META
    val result = db.runQuery { QueryDsl.insert(metaTable).multiple(values) }
    return result as TYPE
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> IntPostgreTable<TYPE>.delete() {
    val metaTable = getTable() as META
    printTextLog("[Delete object '${this::class.java.simpleName}' with id '${this@delete.getField("id")}']")
    db.runQuery { QueryDsl.delete(metaTable).where { metaTable.getAutoIncrementProperty() as PropertyMetamodel<Any, Int, Int> eq this@delete.getField("id") as Int } }
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> IntPostgreTable<TYPE>.getData(declaration: WhereDeclaration? = null, sortExpression: SortExpression? = null) : List<TYPE> {
    val metaTable = getTable() as META
    val whereExpr = declaration ?: {metaTable.getAutoIncrementProperty() as PropertyMetamodel<Any, Int, Int> greaterEq 0}
    return if (sortExpression == null) db.runQuery { QueryDsl.from(metaTable).where(whereExpr) } as List<TYPE>
    else db.runQuery { QueryDsl.from(metaTable).where(whereExpr).orderBy(sortExpression) } as List<TYPE>
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> IntPostgreTable<TYPE>.clearTable() {
    val metaTable = getTable() as META
    db.runQuery { QueryDsl.delete(metaTable).all() }
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> IntPostgreTable<TYPE>.getDataOne(declaration: WhereDeclaration? = null, sortExpression: SortExpression? = null) : TYPE? {
    val metaTable = Meta.all().find { it.tableName() == getTable().tableName() } as META
    val whereExpr: WhereDeclaration = declaration ?: {metaTable.getAutoIncrementProperty() as PropertyMetamodel<Any, Int, Int> greaterEq 0}
    return if (sortExpression == null) db.runQuery { QueryDsl.from(metaTable).where(whereExpr).singleOrNull() } as TYPE?
    else db.runQuery { QueryDsl.from(metaTable).where(whereExpr).orderBy(sortExpression).singleOrNull() } as TYPE?
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> IntPostgreTable<TYPE>.getSize(declaration: WhereDeclaration? = null) : Long {
    val metaTable = Meta.all().find { it.tableName() == getTable().tableName() } as META
    val whereExpr: WhereDeclaration = declaration ?: {metaTable.getAutoIncrementProperty() as PropertyMetamodel<Any, Int, Int> greaterEq 0}
    return db.runQuery { QueryDsl.from(metaTable).where(whereExpr).select(count()) }?:0L
}

suspend fun <T: Any> IntPostgreTable<T>.executeDelColumn(columnName: String): String? {
    val columns = this.getColumns()
    if (columns.find { it == columnName } == null) return "В таблице tbl_${this::class.simpleName} нет колонки $columnName"
    return executeScript("""ALTER TABLE ${getTable().tableName()} DROP COLUMN IF EXISTS $columnName RESTRICT""")
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> IntPostgreTable<TYPE>.isEmpty() : Boolean {
    val metaTable = Meta.all().find { it.tableName() == getTable().tableName() } as META
    return db.runQuery { QueryDsl.from(metaTable).select(count()) } == 0L
}

suspend fun <T: Any> IntPostgreTable<T>.getColumns() : Collection<String> {
    try {
        val script = """SELECT column_name FROM information_schema.columns WHERE table_name = 'tbl_${this::class.simpleName?.lowercase()}'"""
        printTextLog("[getColumns] $script")
        val list = mutableListOf<String>()
        db.runQuery {
            QueryDsl.executeTemplate(script.trimIndent())
                .returning()
                .select { row -> list.add(row.get<String>(0)?:"") }
        }
        return list
    } catch (e: Exception) {
        printTextLog("[getColumns] Error: ${e.localizedMessage}")
        return listOf()
    }
}

private suspend fun executeScript(script: String): String? {
    printTextLog("[executeScript] $script")
    try {
        db.runQuery {
            QueryDsl.executeScript(script.trimIndent())
        }
        return null
    } catch (e: Exception) {
        printTextLog("[executeScript] Error: ${e.localizedMessage}")
        return e.localizedMessage
    }
}

suspend fun <T: Any> IntPostgreTable<T>.executeAddColumn(columnName: String, columnType: EnumSQLTypes, defaultValue: Any? = null, notNull: Boolean = false): String? {
    if (defaultValue == null && notNull == true) {
        printTextLog("[executeAddColumn] defaultValue is NULL and notNull is TRUE")
        return "defaultValue is NULL and notNull is TRUE"
    }
    var default = defaultValue
    if (default is String) default = "'$default'"
    return executeScript("""ALTER TABLE ${getTable().tableName()} ADD COLUMN $columnName ${columnType.textValue} ${if (notNull) "NOT NULL" else ""} DEFAULT $default""")
}

