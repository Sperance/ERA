package com.example.enums

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

enum class EnumBearerRoles(val tokenDuration: Duration) {
    DEFAULT((1).days),
    USER((60).days),
    MODERATOR((180).days),
    ADMIN((365).days);

    companion object {
        fun getFromName(name: String?): EnumBearerRoles {
            if (name == null) return DEFAULT
            entries.forEach {
                if (it.name == name.uppercase()) return it
            }
            return DEFAULT
        }
        fun getFromNameOrNull(name: String?): EnumBearerRoles? {
            if (name == null) return null
            entries.forEach {
                if (it.name == name.uppercase()) return it
            }
            return null
        }
    }

    override fun toString(): String {
        return "EnumBearerRoles(name=$name tokenDuration=$tokenDuration)"
    }
}