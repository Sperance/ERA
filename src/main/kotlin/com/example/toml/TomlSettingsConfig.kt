package com.example.toml

import kotlinx.serialization.Serializable

@Serializable
data class TomlSettingsConfig(
    val ENDPOINT: String = "https://api.salon-era.ru/",
    val CAPTHCA_V3: String = "6Lc4ZFMrAAAAALw8tNGrMkzTpkdGMhnqthuB0dtB"
) : IntTomlSettings {
    override fun checkForCorrect(): String? {
        val nodeName = "SETTINGS"
        if (ENDPOINT.isEmpty()) return "$nodeName.ENDPOINT"
        if (CAPTHCA_V3.isEmpty()) return "$nodeName.CAPTHCA_V3"
        return null
    }
}