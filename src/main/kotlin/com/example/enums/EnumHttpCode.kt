package com.example.enums

import io.ktor.http.HttpStatusCode

enum class EnumHttpCode(val httpCode: HttpStatusCode) {
    NOT_FOUND(HttpStatusCode.NotFound),
    DUPLICATE(HttpStatusCode.Conflict),
    BAD_REQUEST(HttpStatusCode.BadRequest),
    INCORRECT_PARAMETER(HttpStatusCode.PreconditionFailed),

    COMPLETED(HttpStatusCode.OK),
}