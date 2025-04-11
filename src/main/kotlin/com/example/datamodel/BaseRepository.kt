package com.example.datamodel

import com.example.datamodel.serverhistory.ServerHistory
import com.example.enums.EnumDataFilter
import com.example.helpers.clearTable
import com.example.helpers.getData
import com.example.helpers.getField
import com.example.helpers.update
import com.example.printTextLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KMutableProperty1

open class BaseRepository<T : Any>(private val obj: T) {

    private val repoData = mutableSetOf<T>()
    private val mutex = Mutex()
    val onChanged = java.util.concurrent.atomic.AtomicBoolean(false)

    private fun getSize() = repoData.size

    // Безопасная загрузка данных снаружи блока lock
    open suspend fun resetData() {
        val newData = obj.getData()
        mutex.withLock {
            repoData.clear()
            repoData.addAll(newData)
            printTextLog("[BaseRepository ResetData ${obj::class.simpleName}::${this.hashCode()}][${getSize()}]")
        }
    }

    open suspend fun deleteData(obj: T?) {
        if (obj == null) return
        mutex.withLock {
            val removed = repoData.removeIf { it.getField("id") == obj.getField("id") }
            if (removed) {
                printTextLog("[BaseRepository deleteData ${obj::class.simpleName}::${this.hashCode()}][${getSize()}] obj: $obj")
                onChanged.set(true)
            }
        }
    }

    open suspend fun deleteData(id: Int?) {
        if (id == null) return
        mutex.withLock {
            val removed = repoData.removeIf { it.getField("id").toString() == id.toString() }
            if (removed) {
                printTextLog("[BaseRepository deleteData ${obj::class.simpleName}::${this.hashCode()}][${getSize()}] id: $id")
                onChanged.set(true)
            }
        }
    }

    open suspend fun updateData(obj: T?) {
        if (obj == null) return
        mutex.withLock {
            repoData.removeIf { it.getField("id") == obj.getField("id") }
            if (repoData.none { it.getField("id") == obj.getField("id") }) {
                repoData.add(obj)
                printTextLog("[BaseRepository updateData ${obj::class.simpleName}::${this.hashCode()}][${getSize()}] obj: $obj")
                onChanged.set(true)
            }
        }
    }

    open suspend fun addData(obj: T?) {
        if (obj == null) return
        val id = obj.getField("id").toString().toIntOrNull() ?: return
        mutex.withLock {
            if (repoData.none { it.getField("id") == obj.getField("id") }) {
                repoData.add(obj)
                printTextLog("[BaseRepository addData ${obj::class.simpleName}::${this.hashCode()}][${getSize()}] obj: $obj")
                onChanged.set(true)
            }
        }
    }

    open suspend fun clearTable() {
        obj.clearTable()
        mutex.withLock {
            repoData.clear()
        }
        CoroutineScope(Dispatchers.IO).launch {
            ServerHistory.addRecord(1, "Очистка таблицы ${obj::class.java.simpleName}", "")
        }
    }

    open suspend fun isHaveData(id: Int?): Boolean {
        if (id == null) return false
        return mutex.withLock {
            repoData.any { it.getField("id") == id }
        }
    }

    open suspend fun isHaveData(ids: Collection<Int>?): Boolean {
        if (ids == null) return false
        return mutex.withLock {
            repoData.count { ids.contains(it.getField("id")) } == ids.size
        }
    }

    open suspend fun clearLinkEqual(field: KMutableProperty1<T, Int?>, index: Int?) {
        if (index == null) return
        val toUpdate = mutableListOf<T>()
        mutex.withLock {
            repoData.filter { field.get(it) == index }.forEach { item ->
                field.set(item, null)
                toUpdate.add(item)
            }
        }
        toUpdate.forEach { item ->
            printTextLog("[clearLinkEqual ${obj::class.java.simpleName}] index: $index object: $item")
            item.update()
            updateData(item)
        }
    }

    open suspend fun clearLinkEqualArray(field: KMutableProperty1<T, Array<Int>?>, index: Int?) {
        if (index == null) return
        val toUpdate = mutableListOf<T>()
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

    open suspend fun getDataFilter(field: String?, state: EnumDataFilter, value: Any?): Collection<T> {
        if (field == null) return emptyList()
        return mutex.withLock {
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

    open suspend fun getRepositoryData(): Collection<T> {
        if (repoData.isEmpty()) {
            resetData()
        }
        return mutex.withLock {
            repoData.toSet().sortedBy { it.getField("id").toString().toIntOrNull() ?: 0 }
        }
    }
}
