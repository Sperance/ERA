package com.example.toml

import kotlinx.serialization.Serializable

@Serializable
data class TomlSettingsConfig(
    val ENDPOINT: String = "https://api.salon-era.ru/",
    val CAPTHCA_V3: String = "6LdB2UkrAAAAAGtd8EAXEWNn7qMgpS92g8nniFgI"
) : IntTomlSettings {
    override fun checkForCorrect(): String? {
        val nodeName = "SETTINGS"
        if (ENDPOINT.isEmpty()) return "$nodeName.ENDPOINT"
        if (CAPTHCA_V3.isEmpty()) return "$nodeName.CAPTHCA_V3"
        return null
    }
}