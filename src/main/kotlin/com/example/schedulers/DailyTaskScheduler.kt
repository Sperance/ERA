package com.example.schedulers

import com.example.currentZeroDate
import com.example.datamodel.clientsschelude.ClientsSchelude.Companion.repo_clientsschelude
import com.example.datamodel.delete
import com.example.minus
import com.example.printTextLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.days

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
                execute_clearClientsSheluders()
            }
        }
    }

    /**
     * Очистка старых графиков работ (60+ дней)
     */
    private suspend fun execute_clearClientsSheluders() {
        printTextLog("[Scheduler '${this::class.java.simpleName}' checked at ${LocalDateTime.now()}]")
        val currentDate = kotlinx.datetime.LocalDateTime.currentZeroDate().minus((60).days)
        val dataRemove = repo_clientsschelude.getRepositoryData().filter { fil -> fil.scheludeDateEnd!! < currentDate }
        dataRemove.forEach { dat ->
            printTextLog("[DELETE] $dat - on over older by date (${dat.scheludeDateEnd}) < $currentDate")
            dat.delete()
        }
        repo_clientsschelude.resetData()
    }
}