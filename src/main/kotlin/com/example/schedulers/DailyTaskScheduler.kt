package com.example.schedulers

import com.example.currentZeroDate
import com.example.datamodel.clientsschelude.ClientsSchelude
import com.example.datamodel.clientsschelude.ClientsSchelude.Companion.tbl_clientsschelude
import com.example.helpers.delete
import com.example.helpers.getData
import com.example.logging.DailyLogger.printTextLog
import com.example.minus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.days

object DailyTaskScheduler {
    private val scope = CoroutineScope(Dispatchers.Default)

    fun start() {
        val className = this::class.java.simpleName
        scope.launch {
            while (true) {
                val now = LocalDateTime.now()
                val nextRun = now.plusDays(1).truncatedTo(ChronoUnit.DAYS)
                val delay = ChronoUnit.MILLIS.between(now, nextRun)
                printTextLog("[Tick '$className']")

                execute_clearClientsSheluders()
                delay(delay)
            }
        }
    }

    /**
     * Очистка старых графиков работ (60+ дней)
     */
    private suspend fun execute_clearClientsSheluders() {
        printTextLog("[Scheduler '${this::class.java.simpleName}' 'execute_clearClientsSheluders' checked at ${LocalDateTime.now()}]")
        val currentDate = kotlinx.datetime.LocalDateTime.currentZeroDate().minus((60).days)
        val dataRemove = ClientsSchelude().getData({ tbl_clientsschelude.schelude_date_end less currentDate })
        dataRemove.forEach { dat ->
            printTextLog("[DELETE] $dat - on over older by date (${dat.schelude_date_end}) < $currentDate")
            dat.delete()
        }
    }
}