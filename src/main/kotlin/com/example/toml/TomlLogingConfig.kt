package com.example.toml

import kotlinx.serialization.Serializable

@Serializable
data class TomlLogingConfig(
    val ENABLE_LOGS: Boolean = true,
    val DAYS_LOGS: Int = 30
) : IntTomlSettings {
    override fun checkForCorrect(): String? {
        val nodeName = "LOGGING"
        return null
    }
}