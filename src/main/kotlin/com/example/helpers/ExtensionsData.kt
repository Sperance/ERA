package com.example.helpers

import com.example.applicationTomlSettings
import com.example.datamodel.records.Records
import com.example.enums.EnumDataFilter
import com.example.enums.EnumDataTypes
import com.example.enums.EnumSQLTypes
import com.example.interfaces.IntPostgreTable
import com.example.logging.DailyLogger.printTextLog
import com.example.plugins.db
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.metamodel.getAutoIncrementProperty
import org.komapper.core.dsl.operator.and
import org.komapper.core.dsl.operator.count
import org.komapper.core.dsl.query.get
import org.komapper.core.dsl.query.singleOrNull
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.javaType
import kotlin.reflect.jvm.javaType
import kotlin.reflect.safeCast

fun Any.haveField(name: String) = this::class.java.declaredFields.find { it.isAccessible = true ; it.name == name } != null
fun Any.getField(name: String) = this::class.java.declaredFields.find { it.isAccessible = true ; it.name == name }?.get(this)
fun Any.putField(name: String, value: Any?) = this::class.java.declaredFields.find { it.isAccessible = true ; it.name == name }?.set(this, value)

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> IntPostgreTable<TYPE>.update(fromClass: String) : TYPE {
    val metaTable = getTable() as META
    val result = db.runQuery { QueryDsl.update(metaTable).single(this@update).returning() } as TYPE
    printTextLog("[UPDATE-$fromClass object '${this::class.java.simpleName}' with id '${result.getField("id")}']")
    return result
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> IntPostgreTable<TYPE>.create(fromClass: String): TYPE {
    val metaTable = getTable() as META
    val result = db.runQuery { QueryDsl.insert(metaTable).single(this@create) }
    printTextLog("[CREATE-$fromClass object '${this::class.java.simpleName}' with id '${result.getField("id")}']")
    return result as TYPE
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> IntPostgreTable<TYPE>.createBatch(fromClass: String, values: List<TYPE>): TYPE {
    val metaTable = getTable() as META
    val result = db.runQuery { QueryDsl.insert(metaTable).multiple(values) }
    printTextLog("[CREATE_BATCH-$fromClass object '${this::class.java.simpleName}' count '${result.size}']")
    return result as TYPE
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> IntPostgreTable<TYPE>.delete() {
    val metaTable = getTable() as META
    printTextLog("[DELETE object '${this::class.java.simpleName}' with id '${this@delete.getField("id")}']")
    db.runQuery { QueryDsl.delete(metaTable).where { metaTable.getAutoIncrementProperty() as PropertyMetamodel<Any, Int, Int> eq this@delete.getField("id") as Int } }
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> IntPostgreTable<TYPE>.deleteSafe() {
    val metaTable = getTable() as META
    printTextLog("[DELETE_SAFE object '${this::class.java.simpleName}' with id '${this@deleteSafe.getField("id")}']")
    this.putField("deleted", true)
    db.runQuery { QueryDsl.update(metaTable).single(this@deleteSafe).returning() } as TYPE
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> IntPostgreTable<TYPE>.restoreSafe() {
    val metaTable = getTable() as META
    printTextLog("[RESTORE_SAFE object '${this::class.simpleName}' with id '${this@restoreSafe.getField("id")}']")
    this.putField("deleted", false)
    db.runQuery { QueryDsl.update(metaTable).single(this@restoreSafe).returning() } as TYPE
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> IntPostgreTable<TYPE>.clearLinks(prop: KMutableProperty1<TYPE, Int?>, id: Int) {
    val metaTable = getTable() as META
    printTextLog("[clearLinks] object '${this::class.simpleName}'. Prop: ${prop.name} id: $id")
    db.runQuery { QueryDsl.update(metaTable)
        .set { (metaTable[prop.name] as PropertyMetamodel<Any, Int, Int>) eq null }
        .where { (metaTable[prop.name] as PropertyMetamodel<Any, Int, Int>) eq id }
    }
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> IntPostgreTable<TYPE>.getData(declaration: WhereDeclaration? = null, sortExpression: SortExpression? = null, withDeleted: Boolean = false) : List<TYPE> {
    val metaTable = getTable() as META
    val whereExpr = declaration ?: {metaTable.getAutoIncrementProperty() as PropertyMetamodel<Any, Int, Int> greaterEq 0}
    val whereDeleteExpr: WhereDeclaration = { (metaTable["deleted"] as PropertyMetamodel<Any, Boolean, Boolean>) eq withDeleted }
    return if (sortExpression == null) db.runQuery { QueryDsl.from(metaTable).where(whereExpr) } as List<TYPE>
    else db.runQuery { QueryDsl.from(metaTable).where(whereExpr.and(whereDeleteExpr)).orderBy(sortExpression) } as List<TYPE>
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> IntPostgreTable<TYPE>.getDataPagination(declaration: WhereDeclaration? = null, page: Int, pageSize: Int? = null) : List<TYPE> {
    val metaTable = getTable() as META
    val whereExpr = declaration ?: {metaTable.getAutoIncrementProperty() as PropertyMetamodel<Any, Int, Int> greaterEq 0}
    val orderKey = metaTable.getAutoIncrementProperty() as PropertyMetamodel<Any, Int, Int>

    if (page == 0) return getData(declaration)

    val _pageSize = pageSize?:applicationTomlSettings!!.SETTINGS.PAGINATION_PAGE_SIZE
    return db.runQuery { QueryDsl.from(metaTable).where(whereExpr).orderBy(orderKey).offset((page - 1) * _pageSize).limit(_pageSize) } as List<TYPE>
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> IntPostgreTable<TYPE>.clearTable() {
    val metaTable = getTable() as META
    db.runQuery { QueryDsl.delete(metaTable).all() }
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> IntPostgreTable<TYPE>.getDataOne(declaration: WhereDeclaration? = null, sortExpression: SortExpression? = null) : TYPE? {
    val metaTable = getTable() as META
    val whereExpr: WhereDeclaration = declaration ?: {metaTable.getAutoIncrementProperty() as PropertyMetamodel<Any, Int, Int> greaterEq 0}
    return if (sortExpression == null) db.runQuery { QueryDsl.from(metaTable).where(whereExpr).singleOrNull() } as TYPE?
    else db.runQuery { QueryDsl.from(metaTable).where(whereExpr).orderBy(sortExpression).singleOrNull() } as TYPE?
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, T: Any, META : EntityMetamodel<Any, Any, META>> IntPostgreTable<TYPE>.isHaveDataField(prop: KMutableProperty1<TYPE, T?>, value: T?) : Boolean {
    if (value == null) return false
    val metaTable = getTable() as META
    val whereExpr: WhereDeclaration = {metaTable[prop.name] as PropertyMetamodel<TYPE, T, T> eq value}
    return db.runQuery { QueryDsl.from(metaTable).where(whereExpr).singleOrNull() } != null
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> IntPostgreTable<TYPE>.getDataFromId(id: Int?) : TYPE? {
    if (id == null) return null
    val metaTable = getTable() as META
    val whereExpr: WhereDeclaration = {metaTable.getAutoIncrementProperty() as PropertyMetamodel<TYPE, Int, Int> eq id}
    return db.runQuery { QueryDsl.from(metaTable).where(whereExpr).singleOrNull() } as TYPE?
}

@Suppress("UNCHECKED_CAST")
fun <T: Any> IntPostgreTable<T>.getWhereDeclarationFilter(field: String, state: EnumDataFilter, value: Any?): WhereDeclaration {

    val property = getTable()[field] as PropertyMetamodel<T, Any, Any>
    val extType = property.exteriorType.javaType

    val innerValue = when (extType) {
        Int::class.java -> value?.toString()?.toIntOrNull()
        Double::class.java -> value?.toString()?.toDoubleOrNull()
        Long::class.java -> value?.toString()?.toLongOrNull()
        String::class.java -> value?.toString()
        LocalDateTime::class.java -> value?.toString()?.toLocalDateTime()
        else -> throw IllegalArgumentException("Value $value cannot be converted to $extType")
    }

    if ((state == EnumDataFilter.CONTAINS || state == EnumDataFilter.NOT_CONTAINS) && extType != String::class.java) {
        throw IllegalArgumentException("State $state only works with STRING type field $field")
    }

    return when (state) {
        EnumDataFilter.EQ -> { { property.eq(innerValue) } }
        EnumDataFilter.NE -> { { property.notEq(innerValue) } }
        EnumDataFilter.LT -> { { property.less(innerValue) } }
        EnumDataFilter.GT -> { { property.greater(innerValue) } }
        EnumDataFilter.LE -> { { property.lessEq(innerValue) } }
        EnumDataFilter.GE -> { { property.greaterEq(innerValue) } }
        EnumDataFilter.CONTAINS -> { { (property as PropertyMetamodel<T, Any, String>).contains(innerValue.toString()) } }
        EnumDataFilter.NOT_CONTAINS -> { { (property as PropertyMetamodel<T, Any, String>).notContains(innerValue.toString()) } }
    }
}

@Suppress("UNCHECKED_CAST")
suspend fun <TYPE: Any, META : EntityMetamodel<Any, Any, META>> IntPostgreTable<TYPE>.getSize(declaration: WhereDeclaration? = null) : Long {
    val metaTable = getTable() as META
    val whereExpr: WhereDeclaration = declaration ?: {metaTable.getAutoIncrementProperty() as PropertyMetamodel<Any, Int, Int> greaterEq 0}
    return db.runQuery { QueryDsl.from(metaTable).where(whereExpr).select(count()) }?:0L
}

suspend fun <T: Any> IntPostgreTable<T>.executeDelColumn(columnName: String): String? {
    val columns = this.getColumns()
    if (columns.find { it == columnName } == null) return "В таблице tbl_${this::class.simpleName} нет колонки $columnName"
    return executeScript("""ALTER TABLE ${getTable().tableName()} DROP COLUMN IF EXISTS $columnName RESTRICT""")
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

