package com.example.minigame

import com.example.to1Digits
import kotlinx.serialization.Serializable

@Serializable
abstract class Prop <T> {
    abstract val name: String
    abstract val description: String
    abstract var stockValue: T
    override fun toString(): String {
        return "Prop(name=$name stockValue=$stockValue)"
    }
}

@Serializable
open class PropertyValue(
    override val name: String,
    override var stockValue: Double = 0.0,
    override val description: String = ""
) : Prop<Double>() {
    fun get() = stockValue.to1Digits()
    fun set(value: Double, needInvoke: Boolean = true) {
        val beforeStock = stockValue
        stockValue = value.to1Digits()
        if (needInvoke && beforeStock != stockValue) arrayListeners.invoke()
    }
    fun remove(value: Double, needInvoke: Boolean = true) {
        val beforeStock = stockValue
        stockValue = (stockValue - value).to1Digits()
        if (needInvoke && beforeStock != stockValue) arrayListeners.invoke()
    }
    fun add(value: Double, needInvoke: Boolean = true) {
        val beforeStock = stockValue
        stockValue = (stockValue + value).to1Digits()
        if (needInvoke && beforeStock != stockValue) arrayListeners.invoke()
    }

    val arrayListeners = ItemListener("Изменение")
}

@Serializable
open class PropertyBlob(
    override val name: String,
    override var stockValue: Boolean,
    override val description: String = ""
) : Prop<Boolean>() {
    fun get() = stockValue
    fun set(value: Boolean) { stockValue = value }
}

object AllStats {
    @Serializable class Health(private var _stockValue: Double) : PropertyValue("Здоровье", _stockValue)
    @Serializable class AttackPhysic(private var _stockValue: Double) : PropertyValue("Физ. атака", _stockValue)
    @Serializable class isCanAttack(private var _stockValue: Boolean) : PropertyBlob("Может атаковать", _stockValue)
    @Serializable class isAlive(private var _stockValue: Boolean) : PropertyBlob("Живой", _stockValue)
}

@Serializable
class Stats(var mob: Mob) {
    val heatlh = AllStats.Health(100.0)
    val attackPhysic = AllStats.AttackPhysic(2.0)

    val isCanAttack = AllStats.isCanAttack(true)
    val isAlive = AllStats.isAlive(true)

    init {
        heatlh.arrayListeners.addListener {
            if (heatlh.get() <= 0.0) {
                isAlive.set(false)
                println("${mob.name} is DEAD")
            }
        }
    }
}