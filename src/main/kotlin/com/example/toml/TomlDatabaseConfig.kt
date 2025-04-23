package com.example.toml

import kotlinx.serialization.Serializable

@Serializable
data class TomlDatabaseConfig(
    val HOST: String = "localhost",
    val PORT: Int = 5432,
    val USER: String = "postgres_rpg",
    val PASSWORD: String = "22322137",
    val DATABASE: String = "postgres_rpg"
) : IntTomlSettings {
    override fun checkForCorrect(): String? {
        val nodeName = "DATABASE"
        if (HOST.isEmpty()) return "$nodeName.HOST"
        if (USER.isEmpty()) return "$nodeName.USER"
        if (PASSWORD.isEmpty()) return "$nodeName.PASSWORD"
        if (DATABASE.isEmpty()) return "$nodeName.DATABASE"
        return null
    }
}