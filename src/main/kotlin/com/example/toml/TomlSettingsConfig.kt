package com.example.toml

import kotlinx.serialization.Serializable

@Serializable
data class TomlSettingsConfig(
    val WEB_SOCKET: Boolean = true,
    val ENDPOINT: String = "https://api.salon-era.ru/"
) : IntTomlSettings {
    override fun checkForCorrect(): String? {
        val nodeName = "SETTINGS"
        if (ENDPOINT.isEmpty()) return "$nodeName.ENDPOINT"
        return null
    }
}