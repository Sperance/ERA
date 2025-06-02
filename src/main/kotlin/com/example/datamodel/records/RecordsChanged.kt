package com.example.datamodel.records

import com.example.helpers.Recordsdata
import kotlinx.serialization.Serializable

@Serializable
data class RecordsChanged(
    val record: Recordsdata?,
    val type: String
)