package com.example.basemodel

import com.example.generateMapError
import io.ktor.server.application.ApplicationCall

data class CheckObjCondition<T>(
    val code: Int,
    val message: (T) -> String,
    val condition: suspend (T) -> Boolean
) {
    suspend fun toCheckObj(obj: T): CheckObj {
        return CheckObj(condition.invoke(obj), code, message.invoke(obj))
    }

    fun toResultResponse(call: ApplicationCall, obj: T): ResultResponse {
        return ResultResponse.Error(generateMapError(call, code to message.invoke(obj)))
    }

    override fun toString(): String {
        return "CheckObjCondition(code=$code, message=$message, condition=$condition)"
    }
}