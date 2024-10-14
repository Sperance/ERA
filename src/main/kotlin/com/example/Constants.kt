package com.example

val SYS_FIELDS_ARRAY = listOf("companion", "id", "version", "createdat")

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class CommentField(val name: String, val required: Boolean)