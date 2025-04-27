package com.example.basemodel

import com.example.datamodel.serverhistory.ServerHistory
import com.example.enums.EnumDataFilter
import com.example.helpers.clearTable
import com.example.helpers.delete
import com.example.helpers.getData
import com.example.helpers.getField
import com.example.helpers.haveField
import com.example.helpers.update
import com.example.logging.DailyLogger.printTextLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.reflect.KMutableProperty1

open class BaseRepository<T : Any>(private val obj: T) {

    private val repoData = mutableSetOf<T>()
    private val mutex = Mutex()
    val onChanged = java.util.concurrent.atomic.AtomicBoolean(false)

    // Безопасная загрузка данных снаружи блока lock
    open suspend fun resetData() {
        val newData = obj.getData()
        withContext(Dispatchers.IO) {
            mutex.withLock {
                repoData.clear()
                repoData.addAll(newData)
                printTextLog("[BaseRepository::${obj::class.simpleName}] resetData size: ${newData.size}")
            }
        }
    }

    open suspend fun deleteData(obj: T?) {
        if (obj == null) return

        withContext(Dispatchers.IO) {
            if (repoData.isEmpty()) resetData()
            mutex.withLock {
                val removed = repoData.removeIf { it.getField("id") == obj.getField("id") }
                if (removed) {
                    onChanged.set(true)
                }
            }
        }
    }

    open suspend fun deleteData(id: Int?) {
        if (id == null) return

        withContext(Dispatchers.IO) {
            if (repoData.isEmpty()) resetData()
            mutex.withLock {
                val removed = repoData.removeIf { it.getField("id").toString() == id.toString() }
                if (removed) {
                    onChanged.set(true)
                }
            }
        }
    }

    open suspend fun updateData(obj: T?) {
        if (obj == null) return

        withContext(Dispatchers.IO) {
            if (repoData.isEmpty()) resetData()
            mutex.withLock {
                val existingObj = repoData.find { it.getField("id") == obj.getField("id") }
                if (existingObj != null) {
                    if (repoData.removeIf{ rem -> rem.getField("id") == existingObj.getField("id") }) {
                        repoData.add(obj)
                        onChanged.set(true)
                    }
                } else {
                    printTextLog("[${obj::class.java.simpleName}] Object not found for update with ID: ${obj.getField("id")}")
                }
            }
        }
    }

    open suspend fun addData(obj: T?) {
        if (obj == null) return

        withContext(Dispatchers.IO) {
            if (repoData.isEmpty()) resetData()
            mutex.withLock {
                if (repoData.none { it.getField("id") == obj.getField("id") }) {
                    printTextLog("[BaseRepository::${obj::class.simpleName}][addData]: $obj")
                    repoData.add(obj)
                    onChanged.set(true)
                } else {
                    printTextLog("[${obj::class.java.simpleName}] Duplicate detected for object with ID: ${obj.getField("id")}")
                }
            }
        }
    }

    open suspend fun clearTable() {
        obj.clearTable()
        withContext(Dispatchers.IO) {
            mutex.withLock {
                repoData.clear()
            }
        }
        ServerHistory.addRecord(1, "Очистка таблицы ${obj::class.java.simpleName}", "")
    }

    open suspend fun isHaveData(id: Int?): Boolean {
        if (id == null) return false
        return withContext(Dispatchers.IO) {
            if (repoData.isEmpty()) resetData()
            mutex.withLock {
                repoData.any { it.getField("id") == id }
            }
        }
    }

    open suspend fun isHaveDataField(field: KMutableProperty1<T, *>, value: Any?): Boolean {
        if (value == null) return false
        if (!obj.haveField(field.name)) {
            printTextLog("[isHaveDataField] object $obj dont have field ${field.name}")
            return false
        }
        return withContext(Dispatchers.IO) {
            if (repoData.isEmpty()) resetData()
            mutex.withLock {
                repoData.firstOrNull { field.get(it) == value } != null
            }
        }
    }

    open suspend fun isHaveData(ids: Collection<Int>?): Boolean {
        if (ids == null) return false
        return withContext(Dispatchers.IO) {
            if (repoData.isEmpty()) resetData()
            mutex.withLock {
                val result = repoData.count { ids.contains(it.getField("id")) }
                printTextLog("COUNT RES: $result")
                result == ids.size
            }
        }
    }

    open suspend fun clearLinkEqual(field: KMutableProperty1<T, Int?>, index: Int?, needDelete: Boolean = false) {
        if (index == null) return
        val toUpdate = mutableListOf<T>()

        withContext(Dispatchers.IO) {
            if (repoData.isEmpty()) resetData()
            mutex.withLock {
                repoData.filter { field.get(it) == index }.forEach { item ->
                    field.set(item, null)
                    toUpdate.add(item)
                }
            }

            toUpdate.forEach { item ->
                printTextLog("[clearLinkEqual ${obj::class.java.simpleName}] index: $index object: $item")
                if (needDelete) {
                    val deleteId = item.getField("id").toString().toIntOrNull()?:0
                    item.delete()
                    deleteData(deleteId)
                } else {
                    item.update()
                    updateData(item)
                }
            }
        }
    }

    open suspend fun clearLinkEqualArray(field: KMutableProperty1<T, Array<Int>?>, index: Int?) {
        if (index == null) return
        val toUpdate = mutableListOf<T>()

        withContext(Dispatchers.IO) {
            if (repoData.isEmpty()) resetData()
            mutex.withLock {
                repoData.filter { field.get(it)?.contains(index) == true }.forEach { item ->
                    val newArray = field.get(item)?.toMutableList() ?: return@forEach
                    newArray.remove(index)
                    field.set(item, newArray.toTypedArray())
                    toUpdate.add(item)
                }
            }

            toUpdate.forEach { item ->
                printTextLog("[clearLinkEqualArray ${obj::class.java.simpleName}] index: $index object: $item")
                item.update()
                updateData(item)
            }
        }
    }

    open suspend fun getDataFilter(field: String?, state: EnumDataFilter, value: Any?): Collection<T> {
        if (field == null) return emptyList()

        return withContext(Dispatchers.IO) {
            if (repoData.isEmpty()) resetData()
            mutex.withLock {
                repoData.filter {
                    val fieldValue = it.getField(field)?.toString()?.lowercase()
                    val filterValue = value?.toString()?.lowercase()

                    when (state) {
                        EnumDataFilter.EQ -> fieldValue == filterValue
                        EnumDataFilter.NE -> fieldValue != filterValue
                        EnumDataFilter.LT -> fieldValue?.toDoubleOrNull()?.let { f -> filterValue?.toDoubleOrNull()?.let { fv -> f < fv } } ?: false
                        EnumDataFilter.GT -> fieldValue?.toDoubleOrNull()?.let { f -> filterValue?.toDoubleOrNull()?.let { fv -> f > fv } } ?: false
                        EnumDataFilter.LE -> fieldValue?.toDoubleOrNull()?.let { f -> filterValue?.toDoubleOrNull()?.let { fv -> f <= fv } } ?: false
                        EnumDataFilter.GE -> fieldValue?.toDoubleOrNull()?.let { f -> filterValue?.toDoubleOrNull()?.let { fv -> f >= fv } } ?: false
                        EnumDataFilter.CONTAINS -> fieldValue?.contains(filterValue ?: "") == true
                        EnumDataFilter.NOT_CONTAINS -> fieldValue?.contains(filterValue ?: "") == false
                    }
                }
            }
        }
    }

    open suspend fun getRepositoryData(): Collection<T> {
        if (repoData.isEmpty()) resetData()

        return withContext(Dispatchers.IO) {
            mutex.withLock {
                repoData
            }
        }
    }
}
