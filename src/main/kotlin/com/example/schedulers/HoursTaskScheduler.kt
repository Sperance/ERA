package com.example.schedulers

import com.example.currectDatetime
import com.example.helpers.delete
import com.example.logging.DailyLogger.printTextLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

object HoursTaskScheduler {
    private val scope = CoroutineScope(Dispatchers.Default)

    fun start() {
        val className = this::class.java.simpleName
        scope.launch {
            while (true) {
                val now = LocalDateTime.now()
                val nextRun = now.plusHours(1).truncatedTo(ChronoUnit.HOURS)
                val delay = ChronoUnit.MILLIS.between(now, nextRun)
                printTextLog("[Tick '$className']")

//                execute_clearUnusedTokens()
                delay(delay)
            }
        }
    }

//    suspend fun execute_clearUnusedTokens() {
//        printTextLog("[Scheduler '${this::class.java.simpleName}' checked at ${LocalDateTime.now()}]")
//        val dataRemove = repo_authentications.getRepositoryData().filter { fil -> fil.dateExpired!! <= kotlinx.datetime.LocalDateTime.currectDatetime() }
//        dataRemove.forEach { dat ->
//            repo_authentications.deleteData(dat)
//            dat.delete()
//        }
//    }
}