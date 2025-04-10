package com.example.helpers

val SYS_FIELDS_ARRAY = listOf("companion", "id", "version", "createdat")
val BASE_PATH = "https://api.salon-era.ru/"

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class CommentField(val name: String, val required: Boolean)