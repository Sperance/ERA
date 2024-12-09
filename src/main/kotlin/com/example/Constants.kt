package com.example

val SYS_FIELDS_ARRAY = listOf("companion", "id", "version", "createdat")
val BASE_PATH = "http://95.163.84.228:6533/"

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class CommentField(val name: String, val required: Boolean)