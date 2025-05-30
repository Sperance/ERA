package com.example.toml

import com.akuleshov7.ktoml.Toml
import com.example.helpers.TOML_FILE_NAME
import com.example.logging.DailyLogger.printTextLog
import kotlinx.serialization.Serializable
import java.io.File

interface IntTomlSettings {
    fun checkForCorrect(): String?
}

@Serializable
data class TomlConfig(
    val DATABASE: TomlDatabaseConfig = TomlDatabaseConfig(),
    val SETTINGS: TomlSettingsConfig = TomlSettingsConfig()
) : IntTomlSettings {
    override fun checkForCorrect(): String? {
        return listOf(DATABASE, SETTINGS).map { it.checkForCorrect() }.firstOrNull { it != null }
    }
}

fun readTomlFile(): TomlConfig? {
    val configFile = File(TOML_FILE_NAME)
    if (!configFile.exists()) createTomlFile(configFile)
    return try {
        Toml.decodeFromString(TomlConfig.serializer(), configFile.readText())
    }catch (e: Exception) {
        printTextLog("[applicationTomlSettings] Parse Error: ${e.localizedMessage}")
        null
    }
}

fun createTomlFile(file: File) {
    val config = TomlConfig()
    val tomlString = Toml.encodeToString(TomlConfig.serializer(), config)
    try {
        file.writeText(tomlString)
        printTextLog("[applicationTomlSettings] Configuration file '$TOML_FILE_NAME' created successfully. Path: ${file.absolutePath}")
    } catch (e: Exception) {
        printTextLog("[applicationTomlSettings] Error creating configuration file: ${e.message}")
    }
}