package com.example.datamodel

import com.example.datamodel.serverhistory.ServerHistory
import com.example.datamodel.services.Services
import com.example.printTextLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class BaseRepository<T: Any>(private val obj: T) {

    private val repoData = ArrayList<T>()

    private fun isEmpty() = repoData.isEmpty()
    private fun getSize() = repoData.size

    open suspend fun resetData() {
        repoData.clear()
        repoData.addAll(obj.getData())
    }

    open suspend fun clearTable() {
        obj.clearTable()
        repoData.clear()
        CoroutineScope(Dispatchers.IO).launch {
            ServerHistory.addRecord(1, "Очистка таблицы ${obj::class.java.simpleName}", "")
        }
    }

    open suspend fun deleteItem(item: T) {
        if (isEmpty()) {
            resetData()
            return
        }
        repoData.removeIf { it.getField("id") == item.getField("id") }
    }

    open suspend fun updateItem(item: T) {
        if (isEmpty()) {
            resetData()
            return
        }
        deleteItem(item)
        addItem(item)
    }

    open suspend fun addItem(item: T) {
        if (isEmpty()) {
            resetData()
            return
        }
        repoData.add(item)
    }

    open suspend fun getData() : ArrayList<T> {
        if (isEmpty()) resetData()
        return repoData
    }
}