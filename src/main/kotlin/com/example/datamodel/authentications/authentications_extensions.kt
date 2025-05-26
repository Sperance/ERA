@file:Suppress("SENSELESS_COMPARISON")

package com.example.datamodel.authentications

import com.example.setToken
import com.example.basemodel.ResultResponse
import com.example.currectDatetime
import com.example.datamodel.authentications.Authentications.Companion.tbl_authentications
import com.example.enums.EnumBearerRoles
import com.example.enums.EnumHttpCode
import com.example.generateMapError
import com.example.helpers.getDataOne
import com.example.helpers.update
import com.example.logging.DailyLogger.printTextLog
import com.example.plugins.JWT_AUTH_NAME
import com.example.plugins.RoleAwareJWT
import com.example.respond
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.util.date.GMTDate
import kotlinx.datetime.LocalDateTime

fun Route.secureGet(path: String, role: EnumBearerRoles? = null, body: suspend RoutingContext.(userId: Int) -> Unit) {
    authenticate(JWT_AUTH_NAME) {
        get(path) {
            val principal = call.principal<RoleAwareJWT>()
            val check = principal.checkAuthenticate(call, role)
            if (check != null) {
                call.respond(response = check)
                return@get
            }
            try { body.invoke(this, principal!!.userId) } catch (e: Exception) {
                call.respond(response = ResultResponse.Error(EnumHttpCode.BAD_REQUEST, generateMapError(call, 440 to e.localizedMessage)))
            }
        }
    }
}

fun Route.securePost(path: String, role: EnumBearerRoles? = null, body: suspend RoutingContext.(userId: Int) -> Unit) {
    authenticate(JWT_AUTH_NAME) {
        post(path) {
            val principal = call.principal<RoleAwareJWT>()
            val check = principal.checkAuthenticate(call, role)
            if (check != null) {
                call.respond(response = check)
                return@post
            }
            try { body.invoke(this, principal!!.userId) } catch (e: Exception) {
                call.respond(response = ResultResponse.Error(EnumHttpCode.BAD_REQUEST, generateMapError(call, 440 to e.localizedMessage)))
            }
        }
    }
}

fun Route.secureDelete(path: String, role: EnumBearerRoles? = null, body: suspend RoutingContext.(userId: Int) -> Unit) {
    authenticate(JWT_AUTH_NAME) {
        delete(path) {
            val principal = call.principal<RoleAwareJWT>()
            val check = principal.checkAuthenticate(call, role)
            if (check != null) {
                call.respond(response = check)
                return@delete
            }
            try { body.invoke(this, principal!!.userId) } catch (e: Exception) {
                call.respond(response = ResultResponse.Error(EnumHttpCode.BAD_REQUEST, generateMapError(call, 440 to e.localizedMessage)))
            }
        }
    }
}

suspend fun RoleAwareJWT?.checkAuthenticate(call: ApplicationCall, role: EnumBearerRoles?): ResultResponse.Error? {
    if (this == null) {
        return ResultResponse.Error(EnumHttpCode.INCORRECT_PARAMETER, generateMapError(call, 101 to "Principal RoleAwareJWT is null"))
    }

    val findedToken = Authentications().getDataOne({ tbl_authentications.employee eq this@checkAuthenticate.employee ; tbl_authentications.clientId eq this@checkAuthenticate.userId })
    if (findedToken == null) {
        call.response.setToken("", GMTDate())
        return ResultResponse.Error(EnumHttpCode.NOT_FOUND, generateMapError(call, 102 to "The token cannot be found in the database. Please log in again."))
    }

    if (findedToken.dateExpired!! <= LocalDateTime.currectDatetime()) {
        call.response.setToken("", GMTDate())
        return ResultResponse.Error(EnumHttpCode.AUTHORISATION, generateMapError(call, 103 to "Token in database is expired. Please log in again."))
    }

    if (this.employee) {
        if (this.role == null) {
            return ResultResponse.Error(EnumHttpCode.NOT_FOUND, generateMapError(call, 110 to "Dont find Role in token"))
        }
        if (this.role == EnumBearerRoles.DEFAULT) {
            return ResultResponse.Error(EnumHttpCode.BAD_REQUEST, generateMapError(call, 111 to "Current role don`t support system"))
        }
        if (role != null && role.ordinal > this.role.ordinal) {
            return ResultResponse.Error(EnumHttpCode.AUTHORISATION, generateMapError(call, 112 to "This method is blocked for the current role"))
        }
    } else {
        if (this.role == null) {
            return ResultResponse.Error(EnumHttpCode.NOT_FOUND, generateMapError(call, 120 to "Dont find Role in token"))
        }
        if (this.role == EnumBearerRoles.DEFAULT) {
            return ResultResponse.Error(EnumHttpCode.BAD_REQUEST, generateMapError(call, 121 to "Current role don`t support system"))
        }
        if (role != null && role.ordinal > this.role.ordinal) {
            return ResultResponse.Error(EnumHttpCode.AUTHORISATION, generateMapError(call, 122 to "This method is blocked for the current role"))
        }
    }

    findedToken.dateUsed = LocalDateTime.currectDatetime()
    findedToken.update("RoleAwareJWT::checkAuthenticate")
    return null
}