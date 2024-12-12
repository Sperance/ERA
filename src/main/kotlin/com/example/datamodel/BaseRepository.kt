package com.example.datamodel

import com.example.datamodel.serverhistory.ServerHistory
import com.example.datamodel.services.Services
import com.example.printTextLog

open class BaseRepository<T: Any>(private val obj: T) {

    private val repoData = ArrayList<T>()

    private fun isEmpty() = repoData.isEmpty()
    private fun getSize() = repoData.size

    open suspend fun resetData() {
        repoData.clear()
        repoData.addAll(obj.getData())
        printTextLog("[resetData] ${obj::class.java.simpleName} size: ${repoData.size}")
    }

    open suspend fun clearTable() {
        obj.clearTable()
        repoData.clear()
        ServerHistory.addRecord(1, "Очистка таблицы ${obj::class.java.simpleName}", "")
        printTextLog("[clearTable] ${obj::class.java.simpleName}")
    }

    open suspend fun deleteItem(item: T) {
        if (isEmpty()) {
            resetData()
            return
        }
        printTextLog("[deleteItem] ${obj::class.java.simpleName} itemId: ${item.getField("id")}")
        repoData.removeIf { it.getField("id") == item.getField("id") }
    }

    open suspend fun updateItem(item: T) {
        if (isEmpty()) {
            resetData()
            return
        }
        printTextLog("[updateItem] ${obj::class.java.simpleName} item: $item")
        deleteItem(item)
        addItem(item)
    }

    open suspend fun addItem(item: T) {
        if (isEmpty()) {
            resetData()
            return
        }
        val sizeBeforeAdding = getSize()
        repoData.add(item)
        printTextLog("[addItem] ${obj::class.java.simpleName} item: $item sizeBefore: $sizeBeforeAdding sizeAfter: ${getSize()}")
    }

    open suspend fun getData() : ArrayList<T> {
        if (isEmpty()) resetData()
        return repoData
    }
}