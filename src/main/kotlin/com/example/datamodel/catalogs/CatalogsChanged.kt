package com.example.datamodel.catalogs

import kotlinx.serialization.Serializable

@Serializable
data class CatalogsChanged(
    val catalog: Catalogs?,
    val type: String
)