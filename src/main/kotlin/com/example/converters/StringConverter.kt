package com.example.converters

import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.serialization.ContentConverter
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.readRemaining

class StringConverter : ContentConverter {
    override suspend fun deserialize(
        charset: Charset,
        typeInfo: TypeInfo,
        content: ByteReadChannel
    ): Any {
        val bytes = content.readRemaining().readBytes()
        return bytes.toString(charset)
    }

    override suspend fun serialize(
        contentType: ContentType,
        charset: Charset,
        typeInfo: TypeInfo,
        value: Any?
    ): OutgoingContent? {
        return when (value) {
            is String -> TextContent(value, contentType)
            else -> null
        }
    }
}