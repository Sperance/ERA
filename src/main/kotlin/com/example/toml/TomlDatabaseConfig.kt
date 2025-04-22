package com.example.toml

import kotlinx.serialization.Serializable

@Serializable
data class TomlDatabaseConfig(
    val HOST: String = "localhost",
    val PORT: Int = 5432,
    val USER: String = "postgres_rpg",
    val PASSWORD: String = "22322137",
    val DATABASE: String = "postgres_rpg"
)