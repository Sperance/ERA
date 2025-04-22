package com.example.toml

import kotlinx.serialization.Serializable

@Serializable
data class TomlSettingsConfig(
    val WEB_SOCKET: Boolean = true
)