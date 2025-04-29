package com.example.datamodel.records

import kotlinx.serialization.Serializable

@Serializable
data class RecordsChanged(
    val record: Records?,
    val type: String
)