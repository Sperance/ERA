package com.example.helpers

val SYS_FIELDS_ARRAY = listOf("companion", "id", "version", "createdat")
val BASE_PATH = "https://api.salon-era.ru/"
val TOML_FILE_NAME = "settings.toml"

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class CommentField(val name: String, val required: Boolean)