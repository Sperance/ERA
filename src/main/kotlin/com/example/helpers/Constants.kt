package com.example.helpers

import io.ktor.util.AttributeKey

val SYS_FIELDS_ARRAY = listOf("companion", "id", "version", "createdat", "updatedat", "deleted")
const val TOML_FILE_NAME = "settings.toml"
const val ENCRYPT_KEY = "ECqDTm9UnVoFn2BD4vM2/Fgzda1470BvZo4t1PWAkuU="

val AUTH_ERROR_KEY = AttributeKey<String>("AuthError")

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class CommentField(val name: String)