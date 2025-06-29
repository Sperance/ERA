package com.example.basemodel

import com.example.applicationTomlSettings
import com.example.enums.EnumDataFilter
import com.example.enums.EnumDataTypes
import com.example.enums.EnumSQLTypes
import com.example.generateMapError
import com.example.helpers.SYS_FIELDS_ARRAY
import com.example.getCommentFieldAnnotation
import com.example.helpers.create
import com.example.helpers.delete
import com.example.helpers.deleteSafe
import com.example.helpers.executeAddColumn
import com.example.helpers.executeDelColumn
import com.example.helpers.getData
import com.example.helpers.getDataFromId
import com.example.helpers.getDataOne
import com.example.helpers.getDataPagination
import com.example.helpers.getField
import com.example.helpers.getSize
import com.example.helpers.getWhereDeclarationFilter
import com.example.helpers.haveField
import com.example.helpers.putField
import com.example.helpers.restoreSafe
import com.example.helpers.update
import com.example.interfaces.IntPostgreTableRepository
import com.example.isAllNullOrEmpty
import com.example.logging.DailyLogger.printTextLog
import com.example.plugins.db
import com.example.toIntPossible
import com.example.updateFromNullable
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
    class Success(val data: Any?, val headers: Map<String, Any?>? = null) : ResultResponse()
    class Error(val message: MutableMap<String, String>) : ResultResponse()
}

@Suppress("UNCHECKED_CAST")
abstract class IntBaseDataImpl<T : IntBaseDataImpl<T>> : IntPostgreTableRepository<T> {

    abstract fun isValidLine(): Boolean

    open fun getCommentArray(): String {
        var textFields = ""
        this::class.java.declaredFields.forEach {
            if (SYS_FIELDS_ARRAY.contains(it.name.lowercase())) return@forEach
            textFields += "'${it.name}' ${it.type.simpleName} ${it.getCommentFieldAnnotation()}\n"
        }
        return textFields
    }

    open suspend fun addColumn(call: ApplicationCall): ResultResponse {
        return try {
            val columnName = call.parameters["columnName"]
            if (columnName.isNullOrEmpty()) {
                return ResultResponse.Error(
                    generateMapError(call, 301 to "Dont find parameter 'columnName'. This parameter must be String type")
                )
            }

            val columnType = call.parameters["columnType"]
            if (columnType.isNullOrEmpty()) {
                return ResultResponse.Error(generateMapError(call, 302 to "Dont find parameter 'columnType'. This parameter must be String type"))
            }

            val columnTypeEnum = EnumSQLTypes.entries.find { it.textValue.lowercase() == columnType.lowercase() }
            if (columnTypeEnum == null) {
                return ResultResponse.Error(generateMapError(call, 303 to "Dont find SQL type: $columnType"))
            }

            val defaultValue = call.parameters["defaultValue"] as Any?
            val notNull = call.parameters["notNull"]?.lowercase()?.toBooleanStrictOrNull() ?: false

            val result = executeAddColumn(columnName, columnTypeEnum, defaultValue = defaultValue, notNull = notNull)
            if (result == null) ResultResponse.Success("Column $columnName successfully added")
            else ResultResponse.Error(generateMapError(call, 304 to result))
        } catch (e: Exception) {
            ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage.substringBefore("\n")))
        }
    }

    open suspend fun delColumn(call: ApplicationCall): ResultResponse {
        return try {
            val columnName = call.parameters["columnName"]
            if (columnName.isNullOrEmpty()) {
                return ResultResponse.Error(generateMapError(call, 301 to "Dont find parameter 'columnName'. This parameter must be String type"))
            }

            val result = executeDelColumn(columnName)
            if (result == null) ResultResponse.Success("Column $columnName successfully deleted")
            else ResultResponse.Error(generateMapError(call, 304 to result))
        } catch (e: Exception) {
            ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage.substringBefore("\n")))
        }
    }

    open suspend fun get(call: ApplicationCall): ResultResponse {
        return try {
            val page = call.parameters["page"]?.toIntOrNull()
            if (page == null) {
                ResultResponse.Success(getData())
            } else {
                ResultResponse.Success(getDataPagination(page = page))
            }
        } catch (e: Exception) {
            ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage.substringBefore("\n")))
        }
    }

    open suspend fun getInvalid(call: ApplicationCall): ResultResponse {
        return try {
            ResultResponse.Success(getData().filter { !it.isValidLine() })
        } catch (e: Exception) {
            ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage.substringBefore("\n")))
        }
    }

    open suspend fun getFilter(call: ApplicationCall): ResultResponse {
        try {
            val field = call.parameters["field"]
            if (field.isNullOrEmpty()) {
                return ResultResponse.Error(generateMapError(call, 301 to "Incorrect parameter 'field'. This parameter must be 'String' type"))
            }
            val state = call.parameters["state"]
            if (state.isNullOrEmpty()) {
                return ResultResponse.Error(generateMapError(call, 302 to "Incorrect parameter 'state'. This parameter must be 'String' type"))
            }
            val value = call.parameters["value"]
            if (value.isNullOrEmpty()) {
                return ResultResponse.Error(generateMapError(call, 303 to "Incorrect parameter 'value'. This parameter must be 'String' type"))
            }
            if (!this.haveField(field)) {
                return ResultResponse.Error(generateMapError(call, 304 to "Class ${this::class.simpleName} dont have field '$field'"))
            }
            val stateEnum = EnumDataFilter.entries.find { it.name == state.uppercase() }
            if (stateEnum == null) {
                return ResultResponse.Error(generateMapError(call, 305 to "Incorrect parameter 'state'(${state}). This parameter must be 'String' type. Allowed: eq, ne, lt, gt, le, ge, contains, not_contains"))
            }
            val page = call.parameters["page"]?.toIntOrNull()
            val tableSize = getSize { (getTable()["deleted"] as PropertyMetamodel<T, Any, *>) eq false }
            if (page == null) {
                val resultList = getData(declaration = getWhereDeclarationFilter(field, stateEnum, value))
                return ResultResponse.Success(resultList as Collection<*>, headers = mapOf("Content-count" to tableSize))
            } else {
                val resultList = getDataPagination(declaration = getWhereDeclarationFilter(field, stateEnum, value), page)
                return ResultResponse.Success(resultList as Collection<*>, headers = mapOf("Content-count" to tableSize))
            }
        } catch (e: Exception) {
            return ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage.substringBefore("\n")))
        }
    }

    open suspend fun getFromId(call: ApplicationCall, params: RequestParams<T>): ResultResponse {
        try {
            val id = call.parameters["id"]
            if (id.isNullOrEmpty() || !id.toIntPossible()) {
                return ResultResponse.Error(generateMapError(call, 301 to "Incorrect parameter 'id'. This parameter must be 'Int' type"))
            }
            params.checkings.forEach { check ->
                val res = check.invoke(this as T)
                if (res.result) {
                    return ResultResponse.Error(generateMapError(call, res.errorCode to res.errorText))
                }
            }
            params.defaults.forEach { def ->
                val res = def.invoke(this as T)
                val property = res.first as KMutableProperty0<Any?>
                if (!property.get().isAllNullOrEmpty()) return@forEach
                property.set(res.second)
            }
            val data = getDataFromId(id.toIntOrNull())
            if (data == null) {
                return ResultResponse.Error(generateMapError(call, 302 to "Не найдена запись ${this::class.simpleName} с id $id"))
            }
            return ResultResponse.Success(data)
        } catch (e: Exception) {
            return ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage.substringBefore("\n")))
        }
    }

    open suspend fun delete(call: ApplicationCall, params: RequestParams<T>): ResultResponse {
        return db.withTransaction { tx ->
            try {
                val id = call.parameters["id"]
                if (id == null || !id.toIntPossible()) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(generateMapError(call, 301 to "Incorrect parameter 'id'($id). This parameter must be 'Int' type"))
                }

                params.checkings.forEach { check ->
                    val res = check.invoke(this as T)
                    if (res.result) {
                        tx.setRollbackOnly()
                        return@withTransaction ResultResponse.Error(generateMapError(call, res.errorCode to res.errorText))
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
                val findedObj = getDataOne({ auProp eq id.toInt() })
                if (findedObj == null) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(generateMapError(call, 302 to "Not found $currectObjClassName with id $id"))
                }

                params.onBeforeCompleted?.invoke(findedObj as T)

                getFileImageIcon(findedObj, currectObjClassName.lowercase())?.delete()
                findedObj.delete()

                ResultResponse.Success("$currectObjClassName with id $id successfully deleted")
            } catch (e: Exception) {
                tx.setRollbackOnly()
                return@withTransaction ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage.substringBefore("\n")))
            }
        }
    }

    open suspend fun deleteSafe(call: ApplicationCall, params: RequestParams<T>): ResultResponse {
        return db.withTransaction { tx ->
            try {
                val id = call.parameters["id"]
                if (id == null || !id.toIntPossible()) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(generateMapError(call, 301 to "Incorrect parameter 'id'($id). This parameter must be 'Int' type"))
                }

                params.checkings.forEach { check ->
                    val res = check.invoke(this as T)
                    if (res.result) {
                        tx.setRollbackOnly()
                        return@withTransaction ResultResponse.Error(generateMapError(call, res.errorCode to res.errorText))
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
                val findedObj = getDataOne({ auProp eq id.toInt() })
                if (findedObj == null) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(generateMapError(call, 302 to "Not found $currectObjClassName with id $id"))
                }

                params.onBeforeCompleted?.invoke(findedObj as T)

                findedObj.deleteSafe()

                ResultResponse.Success("$currectObjClassName with id $id successfully safe deleted")
            } catch (e: Exception) {
                tx.setRollbackOnly()
                return@withTransaction ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage.substringBefore("\n")))
            }
        }
    }

    open suspend fun restore(call: ApplicationCall, params: RequestParams<T>): ResultResponse {
        return db.withTransaction { tx ->
            try {
                val id = call.parameters["id"]
                if (id == null || !id.toIntPossible()) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(generateMapError(call, 301 to "Incorrect parameter 'id'($id). This parameter must be 'Int' type"))
                }

                params.checkings.forEach { check ->
                    val res = check.invoke(this as T)
                    if (res.result) {
                        tx.setRollbackOnly()
                        return@withTransaction ResultResponse.Error(generateMapError(call, res.errorCode to res.errorText))
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
                val findedObj = getDataOne({ auProp eq id.toInt() })
                if (findedObj == null) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(generateMapError(call, 302 to "Not found $currectObjClassName with id $id"))
                }

                params.onBeforeCompleted?.invoke(findedObj as T)

                findedObj.restoreSafe()

                ResultResponse.Success("$currectObjClassName with id $id successfully safe restored")
            } catch (e: Exception) {
                tx.setRollbackOnly()
                return@withTransaction ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage.substringBefore("\n")))
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
                var countFiles = 0

                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            newObject = Json.decodeFromString(serializer, part.value)
                        }

                        is PartData.FileItem -> {
                            countFiles++
                            fileName = part.originalFileName
                            fileBytes = part.provider().toByteArray()
                        }

                        is PartData.BinaryChannelItem -> {}
                        is PartData.BinaryItem -> {}
                    }
                }
                if (countFiles > 1) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(generateMapError(call, 301 to "Обнаружено несколько файлов($countFiles). Сервер не поддерживает обработку более 1 файла за раз"))
                }
                if (params.isNeedFile && fileBytes == null) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(generateMapError(call, 302 to "Для объекта $currectObjClassName ожидался файл, который не был получен"))
                }
                if (newObject == null) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(generateMapError(call, 303 to "Не удалось создать объект $currectObjClassName по входящему JSON"))
                }
                params.checkings.forEach { check ->
                    val res = check.invoke(newObject!!)
                    if (res.result) {
                        tx.setRollbackOnly()
                        return@withTransaction ResultResponse.Error(generateMapError(call, res.errorCode to res.errorText))
                    }
                }
                params.defaults.forEach { def ->
                    val res = def.invoke(newObject!!)
                    val property = res.first as KMutableProperty0<Any?>
                    if (!property.get().isAllNullOrEmpty()) return@forEach
                    val value = res.second
                    property.set(value)
                }
                val findedObj = getDataFromId(newObject?.id)
                if (findedObj == null) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(generateMapError(call, 304 to "Not found $currectObjClassName with id ${newObject?.id}"))
                }
                if (fileBytes != null) {
                    if (!newObject!!.isHaveImageFields()) {
                        tx.setRollbackOnly()
                        return@withTransaction ResultResponse.Error(generateMapError(call, 305 to "Для сущности $currectObjClassName не реализованы поля хранения файлов"))
                    }
                    saveImageToFields(newObject, fileBytes, fileName?.substringAfterLast("."))
                }

                params.checkOnUpdate?.invoke(findedObj as T, newObject!!)

                params.onBeforeCompleted?.invoke(newObject!!)
                findedObj.updateFromNullable(newObject!!)

                val updated = findedObj.update("IntBaseData::update")

                return@withTransaction ResultResponse.Success(updated)
            } catch (e: Exception) {
                tx.setRollbackOnly()
                e.printStackTrace()
                return@withTransaction ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage.substringBefore("\n")))
            }
        }
    }

    private fun getFileImageIcon(findedObj: IntBaseDataImpl<T>, pathName: String): File? {
        if (!findedObj.haveField("image_link")) return null
        val currentFile = File(
            Paths.get("").absolutePathString() + "/files/$pathName/icon_${findedObj.id}.${findedObj.getField("imageFormat")}"
        )
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
                        is PartData.FormItem -> {
                            jsonString = part.value
                        }

                        is PartData.FileItem -> {
                            fileName = part.originalFileName
                            fileBytes = part.provider().toByteArray()
                        }

                        else -> {
                            printTextLog("Unknown part type: ${part::class.simpleName}")
                        }
                    }
                }

                val newObject = Json.decodeFromString(serializer, jsonString)

                if (params.isNeedFile && fileBytes == null) {
                    tx.setRollbackOnly()
                    return@withTransaction ResultResponse.Error(generateMapError(call, 301 to "Для объекта $currectObjClassName ожидался файл, который не был получен"))
                }

                params.checkings.forEach { check ->
                    newObject.forEach { item ->
                        val res = check.invoke(item)
                        if (res.result) {
                            tx.setRollbackOnly()
                            return@withTransaction ResultResponse.Error(generateMapError(call, res.errorCode to res.errorText))
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
                    return@withTransaction ResultResponse.Error(generateMapError(call, 302 to "Невозможно сохранить файл изображения к массиву элементов (${newObject.size})"))
                }

                newObject.forEach { item ->
                    params.onBeforeCompleted?.invoke(item)
                    finishObject = item.create("IntBaseDataImpl::post")
                }

                if (fileBytes != null) {
                    if (!finishObject!!.isHaveImageFields()) {
                        tx.setRollbackOnly()
                        return@withTransaction ResultResponse.Error(generateMapError(call, 303 to "Для сущности $currectObjClassName не реализованы поля хранения файлов"))
                    }
                    saveImageToFields(finishObject, fileBytes, fileName?.substringAfterLast("."))
                    params.onBeforeCompleted?.invoke(finishObject!!)

                    finishObject = finishObject!!.update("IntBaseData::post")
                }

                return@withTransaction ResultResponse.Success(finishObject as Any)
            } catch (e: Exception) {
                tx.setRollbackOnly()
                return@withTransaction ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage.substringBefore("\n")))
            }
        }
    }

    /**
     * Проверка полей на наличие картинок
     */
    private fun isHaveImageFields(): Boolean {
        return haveField("image_link") && haveField("image_format")
    }

    private fun saveImageToFields(newObject: T?, fileBytes: ByteArray?, fileExtension: String?) {
        if (newObject == null) {
            printTextLog("[saveImageToFields] newObject is NULL")
            return
        }
        if (fileBytes == null) {
            printTextLog("[saveImageToFields] fileBytes is NULL")
            return
        }
        if (fileExtension == null) {
            printTextLog("[saveImageToFields] fileExtension is NULL")
            return
        }

        val currectObjClassName = this::class.simpleName!!

        val imagePath = File(Paths.get("").toAbsolutePath().toString() + "/files/${currectObjClassName.lowercase()}")
        if (!imagePath.exists()) imagePath.mkdirs()
        val imageFile = File("${imagePath.absolutePath}/icon_${newObject.getField("id")}.$fileExtension")
        if (imageFile.exists()) imageFile.delete()

        imageFile.writeBytes(fileBytes)
        newObject.putField("image_link", "${applicationTomlSettings!!.SETTINGS.ENDPOINT}files/${currectObjClassName.lowercase()}/" + imageFile.name)
        newObject.putField("image_format", imageFile.extension)

        printTextLog("[saveImageToFields] Save file for $currectObjClassName ${imageFile.path}")
    }
}