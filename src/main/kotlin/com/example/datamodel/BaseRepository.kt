package com.example.datamodel

import com.example.printTextLog

open class BaseRepository<T: IntBaseDataImpl<T>>(private val obj: T) {

    private val repoData = ArrayList<T>()

    fun isEmpty() = repoData.isEmpty()

    open suspend fun resetData() {
        repoData.clear()
        repoData.addAll(obj.getData())
        printTextLog("[resetData] ${obj::class.java.simpleName} size: ${repoData.size}")
    }

    open suspend fun deleteItem(itemId: Int) {
        if (isEmpty()) resetData()
        printTextLog("[deleteItem] ${obj::class.java.simpleName} itemId: $itemId size: ${repoData.size}")
        repoData.removeIf { it.getField("id") == itemId }
    }

    open suspend fun updateItem(item: T) {
        if (isEmpty()) resetData()
        printTextLog("[updateItem] ${obj::class.java.simpleName} item: $item")
        deleteItem(item.getField("id") as Int)
        addItem(item)
    }

    open suspend fun addItem(item: T) {
        if (isEmpty()) resetData()
        printTextLog("[addItem] ${obj::class.java.simpleName} item: $item size: ${repoData.size}")
        repoData.add(item)
    }

    open fun getData() = repoData
}