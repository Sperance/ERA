package com.example.toml

import kotlinx.serialization.Serializable

@Serializable
data class TomlLogingConfig(
    val ENABLE_LOGS: Boolean = true,
    val DAYS_LOGS: Int = 30
)