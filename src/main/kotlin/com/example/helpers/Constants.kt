package com.example.helpers

val SYS_FIELDS_ARRAY = listOf("companion", "id", "version", "createdat")
const val TOML_FILE_NAME = "settings.toml"
const val ENCRYPT_KEY = "ECqDTm9UnVoFn2BD4vM2/Fgzda1470BvZo4t1PWAkuU="

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class CommentField(val name: String)