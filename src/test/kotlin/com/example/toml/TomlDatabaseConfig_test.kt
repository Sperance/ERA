package com.example.toml

import com.akuleshov7.ktoml.Toml
import com.example.datamodel.clients.Clients
import com.example.helpers.TOML_FILE_NAME
import com.example.helpers.executeAddColumn
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.io.File

class TomlDatabaseConfig_test {

    @Test
    fun test_create_file() {

        // 1. Create the TOML configuration file
        val configFile = File(TOML_FILE_NAME)
        if (configFile.exists()) configFile.delete()

        if (!configFile.exists()) {
            val config = TomlConfig() // Use default values
            val tomlString = Toml.encodeToString(TomlConfig.serializer(), config)

            try {
                configFile.writeText(tomlString)
                println("Configuration file '$TOML_FILE_NAME' created successfully. Path: ${configFile.absolutePath}")
            } catch (e: Exception) {
                println("Error creating configuration file: ${e.message}")
                return
            }
        } else {
            println("Configuration file '$TOML_FILE_NAME' already exists.")
        }
    }
}