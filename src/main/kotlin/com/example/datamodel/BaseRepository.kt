package com.example.datamodel

import com.example.datamodel.serverhistory.ServerHistory
import com.example.printTextLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KMutableProperty1

open class BaseRepository<T: Any>(private val obj: T) {

    // Используем потокобезопасную версию Set
    private val repoData = ConcurrentHashMap.newKeySet<T>()

    // Атомарный флаг для отслеживания изменений
    val onChanged = AtomicBoolean(false)

    private fun isEmpty() = repoData.isEmpty()
    private fun getSize() = repoData.size

    // Асинхронная загрузка данных
    open suspend fun resetData() {
        // Получаем данные асинхронно
        val newData = obj.getData()

        // Атомарная замена всего содержимого
        repoData.clear()
        repoData.addAll(newData)
        printTextLog("[BaseRepository ResetData ${obj::class.simpleName}] size: ${getSize()}")
    }

    // Удаление элемента по объекту
    open suspend fun deleteData(obj: T?) {
        if (obj == null || isEmpty()) return

        // Атомарное удаление
        val removed = repoData.removeIf { it.getField("id") == obj.getField("id") }
        if (removed) {
            printTextLog("[BaseRepository deleteData ${obj::class.simpleName}] obj: $obj")
            onChanged.set(true)
        }
    }

    // Удаление элемента по ID
    open suspend fun deleteData(id: Int?) {
        if (id == null || isEmpty()) return

        // Атомарное удаление
        val removed = repoData.removeIf { it.getField("id").toString() == id.toString() }
        if (removed) {
            printTextLog("[BaseRepository deleteData ${obj::class.simpleName}] id: $id")
            onChanged.set(true)
        }
    }

    // Обновление элемента
    open suspend fun updateData(obj: T?) {
        if (obj == null) return
        if (isEmpty()) resetData() // Если данных нет, загружаем их

        // Атомарное обновление
        repoData.removeIf { it.getField("id") == obj.getField("id") }
        repoData.add(obj)
        printTextLog("[BaseRepository updateData ${obj::class.simpleName}] obj: $obj")
        onChanged.set(true)
    }

    // Добавление данных
    open suspend fun addData(obj: T?) {
        if (obj == null) return
        val id = obj.getField("id").toString().toIntOrNull()

        // Атомарная проверка и добавление
        if (id != null && repoData.none { it.getField("id") == obj.getField("id") }) {
            repoData.add(obj)
            printTextLog("[BaseRepository addData ${obj::class.simpleName}] obj: $obj")
            onChanged.set(true)
        }
    }

    // Очистка таблицы
    open suspend fun clearTable() {
        obj.clearTable()
        repoData.clear()

        // Асинхронная запись в историю
        CoroutineScope(Dispatchers.IO).launch {
            ServerHistory.addRecord(1, "Очистка таблицы ${obj::class.java.simpleName}", "")
        }
    }

    // Проверка наличия данных по ID
    open suspend fun isHaveData(id: Int?): Boolean {
        if (id == null) return false
        return getRepositoryData().any { it.getField("id") == id }
    }

    // Проверка наличия коллекции данных
    open suspend fun isHaveData(id: Collection<Int>?): Boolean {
        if (id == null) return false
        return getRepositoryData().count { id.contains(it.getField("id")) } == id.size
    }

    // Очистка ссылок на значения в коллекции по индексу
    open suspend fun clearLinkEqual(field: KMutableProperty1<T, Int?>, index: Int?) {
        if (index == null) return
        getRepositoryData().filter { field.get(it) == index }.forEach { item ->
            printTextLog("[clearLinkEqual ${obj::class.java.simpleName}] index: $index object: $item")
            field.set(item, null)
            item.update()
            updateData(item) // Используем наш потокобезопасный метод updateData
        }
    }

    // Очистка ссылок в массиве
    open suspend fun clearLinkEqualArray(field: KMutableProperty1<T, Array<Int>?>, index: Int?) {
        if (index == null) return
        getRepositoryData().filter { field.get(it)?.contains(index) == true }.forEach { item ->
            printTextLog("[clearLinkEqualArray ${obj::class.java.simpleName}] index: $index object: $item")
            val newArray = arrayListOf<Int>().apply { addAll(field.get(item)!!) }
            newArray.remove(index)
            field.set(item, newArray.toTypedArray())
            item.update()
            updateData(item) // Используем наш потокобезопасный метод updateData
        }
    }

    // Получение данных с фильтром по полю
    open suspend fun getDataFilter(field: String?, value: Any?): Collection<T> {
        if (field == null) return emptyList()
        return getRepositoryData().filter {
            it.haveField(field) && it.getField(field).toString().lowercase() == value?.toString()?.lowercase()
        }
    }

    // Получение данных, с загрузкой, если коллекция пуста
    open suspend fun getRepositoryData(): Collection<T> {
        if (isEmpty()) resetData() // Если коллекция пуста, загружаем данные
        return repoData.sortedBy { it.getField("id").toString().toInt() }.toList() // Возвращаем копию для безопасности
    }
}