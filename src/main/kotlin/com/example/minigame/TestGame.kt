package com.example.minigame

import kotlinx.coroutines.runBlocking
import org.junit.Test

class TestGame {

    @Test
    fun test1() {

        val pers1 = Person("Игрок")
        pers1.stats!!.attackPhysic.set(25.0)
        val pers2 = Person("Враг")
        pers2.stats!!.attackPhysic.set(8.0)
        pers2.stats!!.heatlh.set(250.0)

        val battle = BattleObject(pers1, pers2)
        runBlocking {
            battle.doBattle()
        }
    }
}