package com.example.datamodel

import com.example.SYS_FIELDS_ARRAY
import com.example.datamodel.IntBaseDataImpl.RequestParams
import com.example.getCommentFieldAnnotation
import com.example.isAllNullOrEmpty
import com.example.toIntPossible
import com.example.updateFromNullable
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.metamodel.getAutoIncrementProperty
import kotlin.reflect.KMutableProperty0

sealed class ResultResponse {
    abstract val status: HttpStatusCode
    class Success(override val status: HttpStatusCode, val data: Any) : ResultResponse()
    class Error(override val status: HttpStatusCode, val message: String) : ResultResponse()
}

@Suppress("UNCHECKED_CAST")
abstract class IntBaseDataImpl <T> {

    open fun getCommentArray(): String {
        var textFields = ""
        this::class.java.declaredFields.forEach {
            if (SYS_FIELDS_ARRAY.contains(it.name.lowercase())) return@forEach
            textFields += "'${it.name}' ${it.type.simpleName} ${it.getCommentFieldAnnotation()}\n"
        }
        return textFields
    }

    open suspend fun get(call: ApplicationCall, params: RequestParams<T>): ResultResponse {
        try {
            params.checkings.forEach { check ->
                val res = check.invoke(this as T)
                if (res.result) return ResultResponse.Error(HttpStatusCode(res.errorCode, ""), res.errorText)
            }

            params.defaults.forEach { def ->
                val res = def.invoke(this as T)
                val property = res.first as KMutableProperty0<Any?>
                if (!property.get().isAllNullOrEmpty()) return@forEach
                val value = res.second
                property.set(value)
            }
            return ResultResponse.Success(HttpStatusCode.OK, getData())
        } catch (e: Exception) {
            return ResultResponse.Error(HttpStatusCode.BadRequest, e.localizedMessage)
        }
    }

    open suspend fun getId(call: ApplicationCall, params: RequestParams<T>): ResultResponse {
        try {
            val id = call.parameters["id"]

            if (id == null || !id.toIntPossible())
                return ResultResponse.Error(HttpStatusCode.BadRequest, "Incorrect parameter 'id'($id). This parameter must be 'Int' type")

            params.checkings.forEach { check ->
                val res = check.invoke(this as T)
                if (res.result) return ResultResponse.Error(HttpStatusCode(res.errorCode, ""), res.errorText)
            }

            params.defaults.forEach { def ->
                val res = def.invoke(this as T)
                val property = res.first as KMutableProperty0<Any?>
                if (!property.get().isAllNullOrEmpty()) return@forEach
                val value = res.second
                property.set(value)
            }

            val currectObjClassName = this::class.simpleName!!
            val tblObj = getField("tbl_${currectObjClassName.lowercase()}") as EntityMetamodel<*, *, *>
            val auProp = tblObj.getAutoIncrementProperty() as PropertyMetamodel<Any, Int, Int>

            val findedObj = getDataOne({ auProp eq id.toInt()})
            if (findedObj == null)
                return ResultResponse.Error(HttpStatusCode.NotFound, "Not found $currectObjClassName with id $id")

            return ResultResponse.Success(HttpStatusCode.OK, findedObj)
        } catch (e: Exception) {
            return ResultResponse.Error(HttpStatusCode.Conflict, e.localizedMessage)
        }
    }

    class RequestParams<T> {
        val checkings: ArrayList<suspend (T) -> CheckObj> = ArrayList()
        val defaults: ArrayList<suspend (T) -> Pair<KMutableProperty0<*>, Any?>> = ArrayList()
    }

    data class CheckObj(val result: Boolean, var errorCode: Int, var errorText: String)

    open suspend fun postArray(call: ApplicationCall, params: RequestParams<T>): ResultResponse {
        try {
            val newRecord = call.receive<List<T>>()
            var coutner = 0
            newRecord.forEach { record ->
                params.checkings.forEach { check ->
                    val res = check.invoke(record)
                    if (res.result) return ResultResponse.Error(HttpStatusCode(res.errorCode, ""), "Record: $record - ${res.errorText}")
                }

                params.defaults.forEach let@ { def ->
                    val res = def.invoke(record)
                    val property = res.first as KMutableProperty0<Any?>
                    if (!property.get().isAllNullOrEmpty()) return@let
                    val value = res.second
                    property.set(value)
                }

                record?.create(null)
                coutner++
            }

            return ResultResponse.Success(HttpStatusCode.Created, "Successfully created $coutner objects")
        } catch (e: Exception) {
            return ResultResponse.Error(HttpStatusCode.BadRequest, e.localizedMessage?:"Smart error")
        }
    }

    open suspend fun post(call: ApplicationCall, params: RequestParams<T>): ResultResponse {
        try {
            val newRecord = call.receive(this::class)

            params.checkings.forEach { check ->
                val res = check.invoke(newRecord as T)
                if (res.result) return ResultResponse.Error(HttpStatusCode(res.errorCode, ""), res.errorText)
            }

            params.defaults.forEach { def ->
                val res = def.invoke(newRecord as T)
                val property = res.first as KMutableProperty0<Any?>
                if (!property.get().isAllNullOrEmpty()) return@forEach
                val value = res.second
                property.set(value)
            }

            val currectObjClassName = this::class.simpleName!!

            val finish = newRecord.create(null)
            return ResultResponse.Success(HttpStatusCode.Created, "Successfully created $currectObjClassName with id ${finish.getField("id")}")
        } catch (e: Exception) {
            return ResultResponse.Error(HttpStatusCode.BadRequest, e.localizedMessage?:"Smart error")
        }
    }

    open suspend fun delete(call: ApplicationCall, params: RequestParams<T>): ResultResponse {
        try {
            val id = call.parameters["id"]
            if (id == null || !id.toIntPossible()) {
                return ResultResponse.Error(HttpStatusCode.BadRequest, "Incorrect parameter 'id'($id). This parameter must be 'Int' type")
            }

            params.checkings.forEach { check ->
                val res = check.invoke(this as T)
                if (res.result) return ResultResponse.Error(HttpStatusCode(res.errorCode, ""), res.errorText)
            }

            params.defaults.forEach { def ->
                val res = def.invoke(this as T)
                val property = res.first as KMutableProperty0<Any?>
                if (!property.get().isAllNullOrEmpty()) return@forEach
                val value = res.second
                property.set(value)
            }

            val currectObjClassName = this::class.simpleName!!
            val tblObj = getField("tbl_${currectObjClassName.lowercase()}") as EntityMetamodel<*, *, *>
            val auProp = tblObj.getAutoIncrementProperty() as PropertyMetamodel<Any, Int, Int>

            val findedObj = getDataOne({ auProp eq id.toInt()})
            if (findedObj == null) {
                return ResultResponse.Error(HttpStatusCode.NotFound, "Not found $currectObjClassName with id $id")
            }
            findedObj.delete()
            return ResultResponse.Success(HttpStatusCode.NoContent, "$currectObjClassName with id $id successfully deleted")
        } catch (e: Exception) {
            return ResultResponse.Error(HttpStatusCode.BadRequest, e.localizedMessage)
        }
    }

    open suspend fun update(call: ApplicationCall, params: RequestParams<T>): ResultResponse {
        try {
            val newRecord = call.receive(this::class)
            val newRecordId = newRecord.getField("id") as Int?

            if (newRecordId == null || newRecordId == 0)
                return ResultResponse.Error(HttpStatusCode.BadRequest, "Incorrect parameter 'id'($newRecordId)")

            params.checkings.forEach { check ->
                val res = check.invoke(this as T)
                if (res.result) return ResultResponse.Error(HttpStatusCode(res.errorCode, ""), res.errorText)
            }

            params.defaults.forEach { def ->
                val res = def.invoke(this as T)
                val property = res.first as KMutableProperty0<Any?>
                if (!property.get().isAllNullOrEmpty()) return@forEach
                val value = res.second
                property.set(value)
            }

            val currectObjClassName = this::class.simpleName!!
            val tblObj = getField("tbl_${currectObjClassName.lowercase()}") as EntityMetamodel<*, *, *>
            val auProp = tblObj.getAutoIncrementProperty() as PropertyMetamodel<Any, Int, Int>

            val findedObj = getDataOne({ auProp eq newRecordId})
            if (findedObj == null)
                return ResultResponse.Error(HttpStatusCode.NotFound, "Not found $currectObjClassName with id $newRecordId")

            val updatedCount = findedObj.updateFromNullable(newRecord)

            val updated = findedObj.update()
            return ResultResponse.Success(HttpStatusCode.OK, "$currectObjClassName with id ${updated.getField("id")} successfully updated. Updated $updatedCount fields")
        } catch (e: Exception) {
            return ResultResponse.Error(HttpStatusCode.BadRequest, e.localizedMessage)
        }
    }
}