package com.example.toml

import com.example.isNullOrZero
import kotlinx.serialization.Serializable

@Serializable
data class TomlSettingsConfig(
    val ENDPOINT: String = "https://api.salon-era.ru/",
    val CAPTHCA_V3: String = "6Lc4ZFMrAAAAALw8tNGrMkzTpkdGMhnqthuB0dtB",
    val PAGINATION_PAGE_SIZE: Int = 10
) : IntTomlSettings {
    override fun checkForCorrect(): String? {
        val nodeName = "SETTINGS"
        if (ENDPOINT.isEmpty()) return "$nodeName.ENDPOINT"
        if (CAPTHCA_V3.isEmpty()) return "$nodeName.CAPTHCA_V3"
        if (PAGINATION_PAGE_SIZE.isNullOrZero()) return "$nodeName.PAGINATION_PAGE_SIZE"
        return null
    }
}