package com.example.datamodel

import com.example.datamodel.serverhistory.ServerHistory
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

    open suspend fun getRepositoryData() : ArrayList<T> {
        if (isEmpty()) resetData()
        return repoData
    }
}