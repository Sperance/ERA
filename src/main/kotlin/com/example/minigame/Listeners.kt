package com.example.minigame

import kotlinx.serialization.Serializable

@Serializable
class ItemListener(val name: String) {
    private val listeresrArray: ArrayList<() -> Unit> = ArrayList()
    fun addListener(body: () -> Unit) {
        listeresrArray.add(body)
    }
    fun invoke() {
        listeresrArray.forEach {
            it.invoke()
        }
    }
}