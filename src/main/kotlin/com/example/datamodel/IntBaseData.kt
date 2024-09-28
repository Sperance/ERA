package com.example.datamodel

import com.example.toIntPossible
import com.example.updateFromNullable
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.metamodel.getAutoIncrementProperty
import kotlin.reflect.KClass

sealed class ResultResponse {
    abstract val status: HttpStatusCode
    class Success(override val status: HttpStatusCode, val data: Any) : ResultResponse()
    class Error(override val status: HttpStatusCode, val message: String) : ResultResponse()
}

interface IntBaseData {
    suspend fun get(call: ApplicationCall) : ResultResponse
    suspend fun getId(call: ApplicationCall) : ResultResponse
    suspend fun post(call: ApplicationCall) : ResultResponse
    suspend fun delete(call: ApplicationCall) : ResultResponse
    suspend fun update(call: ApplicationCall, kclass: KClass<*>) : ResultResponse
}

@Suppress("UNCHECKED_CAST")
abstract class IntBaseDataImpl <T> : IntBaseData {

    override suspend fun get(call: ApplicationCall): ResultResponse {
        try {
            return ResultResponse.Success(HttpStatusCode.OK, getData())
        } catch (e: Exception) {
            return ResultResponse.Error(HttpStatusCode.BadRequest, e.localizedMessage)
        }
    }

    override suspend fun getId(call: ApplicationCall): ResultResponse {
        try {
            val id = call.parameters["id"]

            if (id == null || !id.toIntPossible())
                return ResultResponse.Error(HttpStatusCode.BadRequest, "Incorrect parameter 'id'($id). This parameter must be 'Int' type")

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

    override suspend fun post(call: ApplicationCall): ResultResponse {
        TODO("Not yet implemented")
    }

    override suspend fun delete(call: ApplicationCall): ResultResponse {
        try {
            val id = call.parameters["id"]
            if (id == null || !id.toIntPossible()) {
                return ResultResponse.Error(HttpStatusCode.BadRequest, "Incorrect parameter 'id'($id). This parameter must be 'Int' type")
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

    override suspend fun update(call: ApplicationCall, kclass: KClass<*>): ResultResponse {
        try {
            val newRecord = call.receive(kclass)
            val newRecordId = newRecord.getField("id") as Int?

            if (newRecordId == null || newRecordId == 0)
                return ResultResponse.Error(HttpStatusCode.BadRequest, "Incorrect parameter 'id'($newRecordId)")

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