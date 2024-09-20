package com.example.minigame

import kotlinx.coroutines.delay

class BattleObject(val person1: Person, val person2: Person) {

    private var isBattleActive = false
    private var countSeconds = 0

    suspend fun doBattle() {
        isBattleActive = true
        while (isBattleActive && person1.stats!!.isAlive.get() && person2.stats!!.isAlive.get()) {
            countSeconds += 100
            delay(100)
            if (countSeconds % 1000 == 0) {
                println("Turn second: ${countSeconds / 1000}")
                person1.onAttack(person2)
                person2.onAttack(person1)
            }
        }
    }
}