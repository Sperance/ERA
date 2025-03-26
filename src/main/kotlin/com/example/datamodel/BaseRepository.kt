package com.example.datamodel

import com.example.datamodel.serverhistory.ServerHistory
import com.example.printTextLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class BaseRepository<T: Any>(private val obj: T) {

    private val repoData = mutableSetOf<T>()

    private fun isEmpty() = repoData.isEmpty()
    private fun getSize() = repoData.size

    open suspend fun resetData() {
        repoData.clear()
        repoData.addAll(obj.getData())
        printTextLog("[BaseRepository ResetData ${obj::class.simpleName}] size: ${getSize()}")
    }

    open suspend fun clearTable() {
        obj.clearTable()
        repoData.clear()
        CoroutineScope(Dispatchers.IO).launch {
            ServerHistory.addRecord(1, "Очистка таблицы ${obj::class.java.simpleName}", "")
        }
    }

    open suspend fun getRepositoryData() : Collection<T> {
        if (isEmpty()) resetData()
        return repoData
    }
}