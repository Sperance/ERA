@file:Suppress("SENSELESS_COMPARISON")

package com.example.datamodel.authentications

import com.example.setToken
import com.example.basemodel.ResultResponse
import com.example.currectDatetime
import com.example.datamodel.authentications.Authentications.Companion.tbl_authentications
import com.example.enums.EnumBearerRoles
import com.example.generateMapError
import com.example.getRouteAttributes
import com.example.helpers.getDataOne
import com.example.helpers.update
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
import io.ktor.util.putAll
import kotlinx.datetime.LocalDateTime

fun Route.secureGet(path: String, role: EnumBearerRoles? = null, title: String, description: String = "", params: Map<String, String> = mapOf(), body: suspend RoutingContext.(roleAwareJWT: RoleAwareJWT?) -> Unit) {
    authenticate(JWT_AUTH_NAME) {
        get(path) {
            val principal = call.principal<RoleAwareJWT>()
            val check = principal.checkAuthenticate(call, role)
            if (check != null) {
                call.respond(response = check)
                return@get
            }
            try { body.invoke(this, principal) } catch (e: Exception) {
                call.respond(response = ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage)))
            }
        }.apply {
            attributes.putAll(getRouteAttributes(title, description, role, params))
        }
    }
}

fun Route.securePost(path: String, role: EnumBearerRoles? = null, title: String, description: String = "", params: Map<String, String> = mapOf(), body: suspend RoutingContext.(roleAwareJWT: RoleAwareJWT?) -> Unit) {
    authenticate(JWT_AUTH_NAME) {
        post(path) {
            val principal = call.principal<RoleAwareJWT>()
            val check = principal.checkAuthenticate(call, role)
            if (check != null) {
                call.respond(response = check)
                return@post
            }
            try { body.invoke(this, principal) } catch (e: Exception) {
                call.respond(response = ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage)))
            }
        }.apply {
            attributes.putAll(getRouteAttributes(title, description, role, params))
        }
    }
}

fun Route.secureDelete(path: String, role: EnumBearerRoles? = null, title: String, description: String = "", params: Map<String, String> = mapOf(), body: suspend RoutingContext.(roleAwareJWT: RoleAwareJWT?) -> Unit) {
    authenticate(JWT_AUTH_NAME) {
        delete(path) {
            val principal = call.principal<RoleAwareJWT>()
            val check = principal.checkAuthenticate(call, role)
            if (check != null) {
                call.respond(response = check)
                return@delete
            }
            try { body.invoke(this, principal) } catch (e: Exception) {
                call.respond(response = ResultResponse.Error(generateMapError(call, 440 to e.localizedMessage)))
            }
        }.apply {
            attributes.putAll(getRouteAttributes(title, description, role, params))
        }
    }
}

suspend fun RoleAwareJWT?.checkAuthenticate(call: ApplicationCall, role: EnumBearerRoles?): ResultResponse.Error? {
    if (this == null) {
        return ResultResponse.Error(generateMapError(call, 101 to "Principal RoleAwareJWT is null"))
    }

    if (this.role == null) {
        return ResultResponse.Error(generateMapError(call, 102 to "Dont find Role in token"))
    }

    if (this.role == EnumBearerRoles.DEFAULT) {
        return ResultResponse.Error(generateMapError(call, 103 to "Current role don`t support system"))
    }

    if (role != null && role.ordinal > this.role.ordinal) {
        return ResultResponse.Error(generateMapError(call, 104 to "This method is blocked for the current role"))
    }

    val findedToken = Authentications().getDataOne({ tbl_authentications.employee eq this@checkAuthenticate.employee ; tbl_authentications.client_id eq this@checkAuthenticate.userId })
    if (findedToken == null) {
        call.response.setToken("", GMTDate())
        return ResultResponse.Error(generateMapError(call, 105 to "The token cannot be found in the database. Please log in again."))
    }

    if (findedToken.isExpires()) {
        call.response.setToken("", GMTDate())
        return ResultResponse.Error(generateMapError(call, 106 to "Token in database is expired. Please log in again."))
    }

    findedToken.date_used = LocalDateTime.currectDatetime()
    findedToken.update("RoleAwareJWT::checkAuthenticate")
    return null
}