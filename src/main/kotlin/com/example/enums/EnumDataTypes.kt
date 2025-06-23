package com.example.enums

enum class EnumDataTypes {
    INT,
    DOUBLE,
    LONG,
    STRING;

    companion object {
        fun getFromName(value: String, defaultValue: EnumDataTypes? = null): EnumDataTypes? {
            return entries.find { it.name.uppercase() == value.trim().uppercase() }?:defaultValue
        }
    }
}