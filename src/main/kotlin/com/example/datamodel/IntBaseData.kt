package com.example.datamodel

import com.example.enums.EnumDataFilter
import com.example.helpers.BASE_PATH
import com.example.helpers.SYS_FIELDS_ARRAY
import com.example.getCommentFieldAnnotation
import com.example.getObjectRepository
import com.example.helpers.create
import com.example.helpers.delete
import com.example.helpers.getData
import com.example.helpers.getDataOne
import com.example.helpers.getField
import com.example.helpers.haveField
import com.example.helpers.putField
import com.example.helpers.update
import com.example.isAllNullOrEmpty
import com.example.plugins.db
import com.example.printTextLog
import com.example.toIntPossible
import com.example.updateFromNullable
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveMultipart
import io.ktor.utils.io.toByteArray
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
abstract class IntBaseDataImpl <T: IntBaseDataImpl<T>> {

    abstract fun getBaseId(): Int

    open fun getCommentArray(): String {
        var textFields = ""
        this::class.java.declaredFields.forEach {
            if (SYS_FIELDS_ARRAY.contains(it.name.lowercase())) return@forEach
            textFields += "'${it.name}' ${it.type.simpleName} ${it.getCommentFieldAnnotation()}\n"
        }
        return textFields
    }

    open suspend fun get(call: ApplicationCall, params: RequestParams<T>): ResultResponse {
        return db.withTransaction { tx ->
            try {
                params.checkings.forEach { check ->
                    val res = check.invoke(this as T)
                    if (res.result) {
                        tx.setRollbackOnly()
                        return@withTransaction ResultResponse.Error(HttpStatusCode(res.errorCode, ""), res.errorText)
                    }
                }

                params.defaults.forEach { def ->
                    val res = def.invoke(this as T)
                    val property = res.first as KMutableProperty0<Any?>
                    if (!property.get().isAllNullOrEmpty()) return@forEach
                    val value = res.second
                    property.set(value)
                }

                params.onBeforeCompleted?.invoke(null)
                val data = getObjectRepository(this)?.getRepositoryData()

                return@withTransaction if (data == null) ResultResponse.Success(HttpStatusCode.OK, getData())
                else ResultResponse.Success(HttpStatusCode.OK, data)
            } catch (e: Exception) {
                tx.setRollbackOnly()
                e.printStackTrace()
                return@withTransaction ResultResponse.Error(HttpStatusCode.BadRequest, e.localizedMessage)
            }
        }
    }

    open suspend fun getFilter(call: ApplicationCall, params: RequestParams<T>): ResultResponse {
        return db.withTransaction { tx ->
            try {
                val field = call.parameters["field"]
                if (field.isNullOrEmpty()) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(HttpStatusCode.BadRequest, "Incorrect parameter 'field'. This parameter must be 'String' type")
                }

                val state = call.parameters["state"]
                if (state.isNullOrEmpty()) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(HttpStatusCode.BadRequest, "Incorrect parameter 'state'. This parameter must be 'String' type")
                }

                val value = call.parameters["value"]
                if (value.isNullOrEmpty()) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(HttpStatusCode.BadRequest, "Incorrect parameter 'value'. This parameter must be 'String' type")
                }

                if (!this.haveField(field)) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(HttpStatusCode.BadRequest, "Class ${this::class.simpleName} dont have field '$field'")
                }

                val stateEnum = EnumDataFilter.entries.find { it.name == state.uppercase() }
                if (stateEnum == null) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(HttpStatusCode.BadRequest, "Incorrect parameter 'state'. This parameter must be 'String' type. Allowed: eq, ne, lt, gt, le, ge, contains, not_contains")
                }

                params.checkings.forEach { check ->
                    val res = check.invoke(this as T)
                    if (res.result) {
                        tx.setRollbackOnly()
                        return@withTransaction ResultResponse.Error(HttpStatusCode(res.errorCode, ""), res.errorText)
                    }
                }

                params.defaults.forEach { def ->
                    val res = def.invoke(this as T)
                    val property = res.first as KMutableProperty0<Any?>
                    if (!property.get().isAllNullOrEmpty()) return@forEach
                    property.set(res.second)
                }

                params.onBeforeCompleted?.invoke(null)

                val resultList = getObjectRepository(this)?.getDataFilter(field, stateEnum, value)
                return@withTransaction ResultResponse.Success(HttpStatusCode.OK, resultList as Collection<*>)
            } catch (e: Exception) {
                tx.setRollbackOnly()
                e.printStackTrace()
                return@withTransaction ResultResponse.Error(HttpStatusCode.Conflict, e.localizedMessage)
            }
        }
    }

    class RequestParams<T> {
        var isNeedFile = false
        val checkings: ArrayList<suspend (T) -> CheckObj> = ArrayList()
        val defaults: ArrayList<suspend (T) -> Pair<KMutableProperty0<*>, Any?>> = ArrayList()
        var onBeforeCompleted: (suspend (T?) -> Any)? = null
        var checkOnUpdate: ((T, T) -> Any)? = null
    }

    data class CheckObj(val result: Boolean, var errorCode: Int, var errorText: String)

    open suspend fun delete(call: ApplicationCall, params: RequestParams<T>): ResultResponse {
        return db.withTransaction { tx ->
            try {
                val id = call.parameters["id"]
                if (id == null || !id.toIntPossible()) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(HttpStatusCode.BadRequest, "Incorrect parameter 'id'($id). This parameter must be 'Int' type")
                }

                params.checkings.forEach { check ->
                    val res = check.invoke(this as T)
                    if (res.result) {
                        tx.setRollbackOnly()
                        return@withTransaction ResultResponse.Error(HttpStatusCode(res.errorCode, ""), res.errorText)
                    }
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
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(HttpStatusCode.NotFound, "Not found $currectObjClassName with id $id")
                }

                params.onBeforeCompleted?.invoke(findedObj as T)

                getFileImageIcon(findedObj, currectObjClassName.lowercase())?.delete()
                findedObj.delete()

                getObjectRepository(this)?.deleteData(id.toInt())

                ResultResponse.Success(HttpStatusCode(204, "$currectObjClassName with id $id successfully deleted"), "$currectObjClassName with id $id successfully deleted")
            } catch (e: Exception) {
                tx.setRollbackOnly()
                e.printStackTrace()
                getObjectRepository(this)?.resetData()
                ResultResponse.Error(HttpStatusCode.BadRequest, e.localizedMessage)
            }
        }
    }

    open suspend fun update(call: ApplicationCall, params: RequestParams<T>, serializer: KSerializer<T>): ResultResponse {
        return db.withTransaction { tx ->
            try {
                val multipartData = call.receiveMultipart()

                var newObject: T? = null
                val currectObjClassName = this::class.simpleName!!
                var fileName: String? = null
                var fileBytes: ByteArray? = null

                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> { newObject = Json.decodeFromString(serializer, part.value) }
                        is PartData.FileItem -> {
                            fileName = part.originalFileName
                            fileBytes = part.provider().toByteArray()
                        }
                        is PartData.BinaryChannelItem -> {}
                        is PartData.BinaryItem -> {}
                    }
                }

                if (params.isNeedFile && fileBytes == null) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(HttpStatusCode.BadRequest, "Для объекта $currectObjClassName ожидался файл, который не был получен")
                }

                if (newObject == null) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(HttpStatusCode.BadRequest, "Не удалось создать объект $currectObjClassName по входящему JSON")
                }

                params.checkings.forEach { check ->
                    val res = check.invoke(newObject!!)
                    if (res.result) {
                        tx.setRollbackOnly()
                        return@withTransaction ResultResponse.Error(HttpStatusCode(res.errorCode, ""), res.errorText)
                    }
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
                val findedObj = getDataOne({ auProp eq newObject?.getBaseId() })
                if (findedObj == null) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(HttpStatusCode.NotFound, "Not found $currectObjClassName with id ${newObject?.getBaseId()}")
                }

                if (fileBytes != null) {
                    if (!newObject!!.isHaveImageFields()) {
                        tx.setRollbackOnly()
                        return@withTransaction ResultResponse.Error(HttpStatusCode(400, "Not Access"), "Для сущности $currectObjClassName не реализованы поля хранения файлов")
                    }
                    saveImageToFields(fileBytes, fileName?.substringAfterLast("."))
                }

                params.checkOnUpdate?.invoke(findedObj as T, newObject!!)

                findedObj.updateFromNullable(newObject!!)
                val updated = findedObj.update()
                getObjectRepository(this)?.updateData(updated)

                return@withTransaction ResultResponse.Success(HttpStatusCode.OK, updated)
            } catch (e: Exception) {
                tx.setRollbackOnly()
                e.printStackTrace()
                return@withTransaction ResultResponse.Error(HttpStatusCode.BadRequest, e.localizedMessage)
            }
        }
    }

    open suspend fun updateMany(call: ApplicationCall, params: RequestParams<T>, serializer: KSerializer<List<T>>): ResultResponse {
        return db.withTransaction { tx ->
            try {
                val multipartData = call.receiveMultipart()
                var jsonString = ""
                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> { jsonString = part.value }
                        is PartData.FileItem -> {}
                        is PartData.BinaryChannelItem -> {}
                        is PartData.BinaryItem -> {}
                    }
                }

                val currectObjClassName = this::class.simpleName!!
                val newObject = Json.decodeFromString(serializer, jsonString)

                if (newObject.isEmpty()) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(HttpStatusCode.BadRequest, "Не удалось создать массив объектов $currectObjClassName по входящему JSON")
                }

                params.checkings.forEach { check ->
                    newObject.forEach { item ->
                        val res = check.invoke(item)
                        if (res.result) {
                            tx.setRollbackOnly()
                            return@withTransaction ResultResponse.Error(HttpStatusCode(res.errorCode, ""), res.errorText)
                        }
                    }
                }

                params.defaults.forEach { def ->
                    newObject.forEach { item ->
                        val res = def.invoke(item)
                        val property = res.first as KMutableProperty0<Any?>
                        if (property.get().isAllNullOrEmpty()) {
                            property.set(res.second)
                        }
                    }
                }

                val tblObj = getField("tbl_${currectObjClassName.lowercase()}") as EntityMetamodel<*, *, *>
                val auProp = tblObj.getAutoIncrementProperty() as PropertyMetamodel<Any, Int, Int>

                val resultArray = arrayListOf<IntBaseDataImpl<T>>()
                newObject.forEach { item ->
                    val findedObj = getDataOne({ auProp eq item.getBaseId() as Int?})
                    if (findedObj == null) {
                        tx.setRollbackOnly()
                        return@withTransaction ResultResponse.Error(HttpStatusCode.NotFound, "Not found $currectObjClassName with id ${item.getBaseId()}")
                    }

                    params.checkOnUpdate?.invoke(findedObj as T, item)

                    findedObj.updateFromNullable(item as Any)
                    val updated = findedObj.update()
                    getObjectRepository(this)?.updateData(updated)
                    resultArray.add(updated)
                }

                return@withTransaction ResultResponse.Success(HttpStatusCode.OK, resultArray)
            } catch (e: Exception) {
                tx.setRollbackOnly()
                e.printStackTrace()
                return@withTransaction ResultResponse.Error(HttpStatusCode.BadRequest, e.localizedMessage)
            }
        }
    }

    private fun getFileImageIcon(findedObj: IntBaseDataImpl<T>, pathName: String): File? {
        if (!findedObj.haveField("imageLink")) return null
        val currentFile = File(Paths.get("").absolutePathString() + "/files/$pathName/icon_${findedObj.getBaseId()}.${findedObj.getField("imageFormat")}")
        if (!currentFile.exists()) return null
        return currentFile
    }

    open suspend fun post(call: ApplicationCall, params: RequestParams<T>, serializer: KSerializer<List<T>>): ResultResponse {
        return db.withTransaction { tx ->
            try {
                val multipartData = call.receiveMultipart()

                var finishObject: T? = null
                var jsonString = ""
                val currectObjClassName = this::class.simpleName!!

                var fileName: String? = null
                var fileBytes: ByteArray? = null

                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> { jsonString = part.value }
                        is PartData.FileItem -> {
                            fileName = part.originalFileName
                            fileBytes = part.provider().toByteArray()
                        }
                        else -> { printTextLog("Unknown part type: ${part::class.simpleName}") }
                    }
                }

                val newObject = Json.decodeFromString(serializer, jsonString)

                if (params.isNeedFile && fileBytes == null) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(HttpStatusCode.BadRequest, "Для объекта $currectObjClassName ожидался файл, который не был получен")
                }
                params.checkings.forEach { check ->
                    newObject.forEach { item ->
                        val res = check.invoke(item)
                        if (res.result) {
                            tx.setRollbackOnly()
                            return@withTransaction ResultResponse.Error(HttpStatusCode(res.errorCode, ""), res.errorText)
                        }
                    }
                }

                params.defaults.forEach { def ->
                    newObject.forEach { item ->
                        val res = def.invoke(item)
                        val property = res.first as KMutableProperty0<Any?>
                        if (property.get().isAllNullOrEmpty()) {
                            property.set(res.second)
                        }
                    }
                }

                if (newObject.size > 1 && fileBytes != null) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(HttpStatusCode(420, "Perform Error"), "Невозможно сохранить файл изображения к массиву элементов (${newObject.size})")
                }

                newObject.forEach { item ->
                    finishObject = item.create(null)
                    getObjectRepository(this)?.addData(finishObject)
                }

                if (fileBytes != null) {
                    if (!finishObject!!.isHaveImageFields()) {
                        tx.setRollbackOnly()
                        return@withTransaction ResultResponse.Error(HttpStatusCode(400, "Not Access"), "Для сущности $currectObjClassName не реализованы поля хранения файлов")
                    }
                    saveImageToFields(fileBytes, fileName?.substringAfterLast("."))
                    params.onBeforeCompleted?.invoke(finishObject!!)
                    finishObject = finishObject!!.update()
                } else {
                    params.onBeforeCompleted?.invoke(finishObject!!)
                }

                return@withTransaction ResultResponse.Success(HttpStatusCode.Created, finishObject as Any)
            } catch (e: Exception) {
                tx.setRollbackOnly()
                e.printStackTrace()
                getObjectRepository(this)?.resetData()
                return@withTransaction ResultResponse.Error(HttpStatusCode.BadRequest, e.localizedMessage)
            }
        }
    }

    /**
     * Проверка полей на наличие картинок
     */
    private fun isHaveImageFields(): Boolean {
        return haveField("imageLink") && haveField("imageFormat")
    }

    private fun saveImageToFields(fileBytes: ByteArray?, fileExtension: String?) {
        if (fileBytes == null) {
            printTextLog("[saveImageToFields] fileBytes is NULL")
            return
        }
        if (fileExtension == null) {
            printTextLog("[saveImageToFields] fileExtension is NULL")
            return
        }
        val currectObjClassName = this::class.simpleName!!
        val imageFile = File(Paths.get("").toAbsolutePath().toString() + "/files/${currectObjClassName.lowercase()}/icon_${getBaseId()}.$fileExtension")
        if (imageFile.exists()) imageFile.delete()
        imageFile.writeBytes(fileBytes)
        putField("imageLink", "${BASE_PATH}files/${currectObjClassName.lowercase()}/" + imageFile.name)
        putField("imageFormat", imageFile.extension)

        printTextLog("[saveImageToFields] Save file for $currectObjClassName ${imageFile.path}")
    }
}