package com.example.minigame

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
abstract class Mob {
    val uuid: String = UUID.randomUUID().toString()
    abstract val name: String
    abstract var level: Byte
    var stats: Stats? = null

    abstract fun onAttack(enemy: Mob)
}

@Serializable
class Person(
    override val name: String,
    override var level: Byte = 1,
    var experience: Int = 0
) : Mob() {

    init {
        stats = Stats(this)
    }

    override fun onAttack(enemy: Mob) {
        if (!stats!!.isCanAttack.get()) return

        enemy.stats!!.heatlh.remove(stats!!.attackPhysic.get())
        println("$name attack ${enemy.name} on ${stats!!.attackPhysic.get()}. ${enemy.name} health = ${enemy.stats!!.heatlh.get()}")
    }
}