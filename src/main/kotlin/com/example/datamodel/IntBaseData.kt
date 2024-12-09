package com.example.datamodel

import com.example.BASE_PATH
import com.example.SYS_FIELDS_ARRAY
import com.example.datamodel.clients.Clients
import com.example.datamodel.services.Services
import com.example.getCommentFieldAnnotation
import com.example.isAllNullOrEmpty
import com.example.printTextLog
import com.example.toIntPossible
import com.example.updateFromNullable
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.metamodel.getAutoIncrementProperty
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
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
        var isNeedFile = false
        val checkings: ArrayList<suspend (T) -> CheckObj> = ArrayList()
        val defaults: ArrayList<suspend (T) -> Pair<KMutableProperty0<*>, Any?>> = ArrayList()
        var onFinish: (suspend (T) -> Unit)? = null
    }

    data class CheckObj(val result: Boolean, var errorCode: Int, var errorText: String)

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
            if (findedObj == null) return ResultResponse.Error(HttpStatusCode.NotFound, "Not found $currectObjClassName with id $id")

            //Удаляем файл иконки с сервера
            getFileImageIcon(findedObj, currectObjClassName.lowercase())?.delete()

            params.onFinish?.invoke(findedObj as T)

            findedObj.delete()

            return ResultResponse.Success(HttpStatusCode.NoContent, "$currectObjClassName with id $id successfully deleted")
        } catch (e: Exception) {
            return ResultResponse.Error(HttpStatusCode.BadRequest, e.localizedMessage)
        }
    }

    open suspend fun updateFormData(call: ApplicationCall, params: RequestParams<T>, serializer: KSerializer<T>): ResultResponse {
        try {
            val multipartData = call.receiveMultipart()

            var newObject: T? = null
            var partFileData: PartData.FileItem? = null

            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> { newObject = Json.decodeFromString(serializer, part.value) }
                    is PartData.FileItem -> { partFileData = part }
                    is PartData.BinaryChannelItem -> {}
                    is PartData.BinaryItem -> {}
                }
            }

            val currectObjClassName = this::class.simpleName!!

            if (params.isNeedFile && partFileData == null) return ResultResponse.Error(HttpStatusCode.BadRequest, "Для объекта $currectObjClassName ожидался файл, который не был получен")
            if (newObject == null) return ResultResponse.Error(HttpStatusCode.BadRequest, "Не удалось создать объект $currectObjClassName по входящему JSON")

            params.checkings.forEach { check ->
                val res = check.invoke(newObject!!)
                if (res.result) return ResultResponse.Error(HttpStatusCode(res.errorCode, ""), res.errorText)
            }

            params.defaults.forEach { def ->
                val res = def.invoke(newObject!!)
                val property = res.first as KMutableProperty0<Any?>
                if (!property.get().isAllNullOrEmpty()) return@forEach
                val value = res.second
                property.set(value)
            }

            val tblObj = getField("tbl_${currectObjClassName.lowercase()}") as EntityMetamodel<*, *, *>
            val auProp = tblObj.getAutoIncrementProperty() as PropertyMetamodel<Any, Int, Int>
            val findedObj = getDataOne({ auProp eq newObject?.getField("id") as Int?})
            if (findedObj == null) return ResultResponse.Error(HttpStatusCode.NotFound, "Not found $currectObjClassName with id ${newObject?.getField("id")}")

            if (partFileData != null) {

                if (!newObject!!.haveField("imageLink") || !newObject!!.haveField("imageFormat")) {
                    return ResultResponse.Error(HttpStatusCode(400, "Not Access"), "Для сущности $currectObjClassName не реализованы поля хранения файлов")
                }

                getFileImageIcon(findedObj, currectObjClassName.lowercase())?.delete()
                val imageFile = File(Paths.get("").toAbsolutePath().toString() + "/files/${currectObjClassName.lowercase()}/icon_${findedObj.getField("id")}.${partFileData!!.originalFileName?.substringAfterLast(".")}")
                partFileData!!.streamProvider().use { inpStream -> imageFile.outputStream().use { outStream -> inpStream.copyTo(outStream) } }
                newObject!!.putField("imageLink", "${BASE_PATH}files/${currectObjClassName.lowercase()}/" + imageFile.name)
                newObject!!.putField("imageFormat", partFileData!!.originalFileName?.substringAfterLast("."))
            }

            findedObj.updateFromNullable(newObject!!)
            val updated = findedObj.update()

            params.onFinish?.invoke(updated as T)

            return ResultResponse.Success(HttpStatusCode.OK, updated)
        } catch (e: Exception) {
            return ResultResponse.Error(HttpStatusCode.BadRequest, e.localizedMessage)
        }
    }

    private fun getFileImageIcon(findedObj: IntBaseDataImpl<T>, pathName: String): File? {
        if (findedObj.getField("imageLink") == null) return null
        val currentFile = File(Paths.get("").absolutePathString() + "/files/$pathName/icon_${findedObj.getField("id")}.${findedObj.getField("imageFormat")}")
        if (!currentFile.exists()) return null
        return currentFile
    }

    open suspend fun postFormData(call: ApplicationCall, params: RequestParams<T>, serializer: KSerializer<T>): ResultResponse {
        try {
            val multipartData = call.receiveMultipart()

            var newObject: T? = null
            var partFileData: PartData.FileItem? = null

                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> { newObject = Json.decodeFromString(serializer, part.value) }
                        is PartData.FileItem -> { partFileData = part }
                        is PartData.BinaryChannelItem -> {}
                        is PartData.BinaryItem -> {}
                    }
                }

            val currectObjClassName = this::class.simpleName!!

            if (params.isNeedFile && partFileData == null) return ResultResponse.Error(HttpStatusCode.BadRequest, "Для объекта $currectObjClassName ожидался файл, который не был получен")
            if (newObject == null) return ResultResponse.Error(HttpStatusCode.BadRequest, "Не удалось создать объект $currectObjClassName по входящему JSON")

            params.checkings.forEach { check ->
                val res = check.invoke(newObject!!)
                if (res.result) return ResultResponse.Error(HttpStatusCode(res.errorCode, ""), res.errorText)
            }

            params.defaults.forEach { def ->
                val res = def.invoke(newObject!!)
                val property = res.first as KMutableProperty0<Any?>
                if (!property.get().isAllNullOrEmpty()) return@forEach
                val value = res.second
                property.set(value)
            }

            val finish = newObject!!.create(null)
            if (partFileData != null) {
                if (!finish.haveField("imageLink") || !finish.haveField("imageFormat")) {
                    return ResultResponse.Error(HttpStatusCode(400, "Not Access"), "Для сущности $currectObjClassName не реализованы поля хранения файлов")
                }
                val imagePath = File(Paths.get("").toAbsolutePath().toString() + "/files/${currectObjClassName.lowercase()}")
                if (!imagePath.exists()) imagePath.mkdirs()
                val imageFile = File(Paths.get("").toAbsolutePath().toString() + "/files/${currectObjClassName.lowercase()}/icon_${finish.getField("id")}.${partFileData!!.originalFileName?.substringAfterLast(".")}")
                partFileData!!.streamProvider().use { inpStream -> imageFile.outputStream().use { outStream -> inpStream.copyTo(outStream) } }
                finish.putField("imageLink", "${BASE_PATH}files/${currectObjClassName.lowercase()}/" + imageFile.name)
                finish.putField("imageFormat", partFileData!!.originalFileName?.substringAfterLast("."))
                finish.update()
            }

            params.onFinish?.invoke(finish)

            return ResultResponse.Success(HttpStatusCode.Created, finish)
        } catch (e: Exception) {
            return ResultResponse.Error(HttpStatusCode.BadRequest, e.localizedMessage)
        }
    }
}