package com.example.logging

import com.example.applicationTomlSettings
import com.example.currectDatetime
import com.example.toFormatDateTime
import io.ktor.server.application.Application
import kotlinx.datetime.LocalDateTime
import java.io.File
import java.time.format.DateTimeFormatter

object DailyLogger {
    private const val LOG_DIRECTORY = "logs" // Директория для хранения логов
    private var logRetentionDays: Int = applicationTomlSettings!!.LOGGING.DAYS_LOGS // Количество дней хранения логов
    private val enableLogs: Boolean = applicationTomlSettings!!.LOGGING.ENABLE_LOGS
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private var currentLogFile: File? = null
    private var currentDate: LocalDateTime? = null

    init {
        if (enableLogs) {
            File(LOG_DIRECTORY).mkdirs()
            printTextLog("[applicationTomlSettings] Logs is Active. Logs saved $logRetentionDays days")
        } else {
            printTextLog("[applicationTomlSettings] Logs is Disabled")
        }
        updateLogFile()
        cleanupOldLogs()
    }

    @Synchronized
    fun printTextLog(text: String) {
        val now = LocalDateTime.currectDatetime()
        if (enableLogs && currentDate != now) {
            updateLogFile()
        }

        try {
            val curDTime = System.currentTimeMillis().toFormatDateTime()
            println("$curDTime $text")
            if (enableLogs) currentLogFile?.appendText("$curDTime $text\n")
        } catch (e: Exception) {
            println("[DailyLogger] Error writing to log file: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun updateLogFile() {
        currentDate = LocalDateTime.currectDatetime()
        val logFileName = "log_${java.time.LocalDate.now().format(dateFormatter)}.txt"
        currentLogFile = File(LOG_DIRECTORY, logFileName)
        try {
            if (!currentLogFile?.exists()!!) {
                currentLogFile?.createNewFile()
                println("[DailyLogger] Created new log file: ${currentLogFile?.absolutePath}")
            }
        } catch (e: Exception) {
            println("[DailyLogger] Error creating log file: ${e.message}")
            e.printStackTrace()
        }
        cleanupOldLogs()
    }

    private fun cleanupOldLogs() {
        if (!enableLogs) return
        val logDir = File(LOG_DIRECTORY)
        val files = logDir.listFiles { file -> file.name.startsWith("log_") && file.name.endsWith(".txt") } ?: return

        val cutoffDate = java.time.LocalDate.now().minusDays(logRetentionDays.toLong())

        files.forEach { file ->
            try {
                val logDate = java.time.LocalDate.parse(file.name.substringAfter("log_").substringBefore(".txt"), dateFormatter)
                if (logDate.isBefore(cutoffDate)) {
                    if (file.delete()) {
                        println("[DailyLogger] Deleted old log file: ${file.absolutePath}")
                    } else {
                        println("[DailyLogger] Failed to delete old log file: ${file.absolutePath}")
                    }
                }
            } catch (e: Exception) {
                println("[DailyLogger] Error parsing date from log file name: ${file.name} - ${e.message}")
            }
        }
    }
}