package com.example.schedulers

import com.example.printTextLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class DailyTaskScheduler {
    private val scope = CoroutineScope(Dispatchers.Default)

    fun start() {
        printTextLog("[Scheduler '${this::class.java.simpleName}' started]")
        scope.launch {
            while (true) {
                val now = LocalDateTime.now()
                val nextRun = now.plusDays(1).truncatedTo(ChronoUnit.DAYS)
                val delay = ChronoUnit.MILLIS.between(now, nextRun)

                delay(delay)
                executeTask()
            }
        }
    }

    private fun executeTask() {
        printTextLog("[Scheduler '${this::class.java.simpleName}' checked at ${LocalDateTime.now()}]")
    }
}