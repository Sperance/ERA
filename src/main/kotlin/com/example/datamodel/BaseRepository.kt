package com.example.datamodel

import com.example.datamodel.serverhistory.ServerHistory
import com.example.printTextLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty0

open class BaseRepository<T: Any>(private val obj: T) {

    private val repoData = mutableSetOf<T>()

    private fun isEmpty() = repoData.isEmpty()
    private fun getSize() = repoData.size

    open suspend fun resetData() {
        repoData.clear()
        repoData.addAll(obj.getData())
        printTextLog("[BaseRepository ResetData ${obj::class.simpleName}] size: ${getSize()}")
    }

    open suspend fun deleteData(obj: T?) {
        if (obj == null) return
        if (isEmpty()) resetData()
        repoData.removeIf { it.getField("id") == obj.getField("id") }
        printTextLog("[BaseRepository deleteData ${obj::class.simpleName}] obj: $obj")
    }

    open suspend fun deleteData(id: Int?) {
        if (id == null) return
        if (isEmpty()) resetData()
        repoData.removeIf { it.getField("id") == id }
        printTextLog("[BaseRepository deleteData ${obj::class.simpleName}] id: $id")
    }

    open suspend fun updateData(obj: T?) {
        if (obj == null) return
        if (isEmpty()) resetData()
        if (repoData.removeIf { it.getField("id") == obj.getField("id") }) {
            repoData.add(obj)
            printTextLog("[BaseRepository updateData ${obj::class.simpleName}] obj: $obj")
        }
    }

    open suspend fun addData(obj: T?) {
        if (obj == null) return
        if (!isHaveData(obj.getField("id").toString().toIntOrNull())) {
            repoData.add(obj)
            printTextLog("[BaseRepository addData ${obj::class.simpleName}] obj: $obj")
        }
    }

    open suspend fun clearTable() {
        obj.clearTable()
        repoData.clear()
        CoroutineScope(Dispatchers.IO).launch {
            ServerHistory.addRecord(1, "Очистка таблицы ${obj::class.java.simpleName}", "")
        }
    }

    open suspend fun isHaveData(id: Int?): Boolean {
        if (id == null) return false
        return getRepositoryData().find { it.getField("id") == id } != null
    }

    open suspend fun isHaveData(id: Collection<Int>?): Boolean {
        if (id == null) return false
        val result = getRepositoryData().filter { id.contains(it.getField("id")) }
        return result.size == id.size
    }

    open suspend fun clearLinkEqual(field: KMutableProperty1<T, Int?>, index: Int?) {
        if (index == null) return
        getRepositoryData().filter { field.get(it) == index }.forEach { item ->
            printTextLog("[clearLinkEqual ${obj::class.java.simpleName}] index: $index object: $item")
            field.set(item, null)
            item.update()
            updateData(item)
        }
    }

    open suspend fun clearLinkEqualArray(field: KMutableProperty1<T, Array<Int>?>, index: Int?) {
        if (index == null) return
        getRepositoryData().filter { field.get(it)?.contains(index) == true }.forEach { item ->
            printTextLog("[clearLinkEqualArray ${obj::class.java.simpleName}] index: $index object: $item")
            val newArray = arrayListOf<Int>().apply { addAll(field.get(item)!!) }
            newArray.remove(index)
            field.set(item, newArray.toTypedArray())
            item.update()
            updateData(item)
        }
    }

    open suspend fun getRepositoryData() : Collection<T> {
        if (isEmpty()) resetData()
        return repoData
    }
}