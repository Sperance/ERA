package com.example.enums

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

enum class EnumBearerRoles(val tokenDuration: Duration) {
    USER((2).minutes),
    MODERATOR((1).days),
    ADMIN((1).days);

    companion object {
        fun getFromName(name: String?): EnumBearerRoles {
            if (name == null) return USER
            entries.forEach {
                if (it.name == name.uppercase()) return it
            }
            return USER
        }
    }
}