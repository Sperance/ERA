package com.example.schedulers

import com.example.printTextLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class HoursTaskScheduler {
    private val scope = CoroutineScope(Dispatchers.Default)

    fun start() {
        val className = this::class.java.simpleName
        scope.launch {
            while (true) {
                val now = LocalDateTime.now()
                val nextRun = now.plusHours(1).truncatedTo(ChronoUnit.HOURS)
                val delay = ChronoUnit.MILLIS.between(now, nextRun)
                printTextLog("[Tick '$className']")

                delay(delay)
            }
        }
    }
}