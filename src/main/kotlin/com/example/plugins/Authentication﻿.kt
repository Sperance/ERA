package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.Payload
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import com.example.basemodel.ResultResponse
import com.example.datamodel.clients.Clients
import com.example.datamodel.employees.Employees
import com.example.enums.EnumBearerRoles
import com.example.generateMapError
import com.example.helpers.AUTH_ERROR_KEY
import com.example.helpers.getDataFromId
import com.example.logging.DailyLogger.printTextLog
import com.example.respond
import com.example.toIntPossible
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.auth.jwt.JWTPayloadHolder
import io.ktor.server.response.respond
import io.ktor.utils.io.InternalAPI
import java.util.Date

const val JWT_HMAC = "secrets_era"
const val JWT_ISSUER = "ktor.era.io"
const val JWT_AUDIENCE = "ktor.audience.era"
const val JWT_AUTH_NAME = "auth-jwt-cookie"
const val JWT_REALM_NAME = "ERA_KT_SRV"

class RoleAwareJWT(
    payload: Payload,
    val userId: Int,
    val role: EnumBearerRoles,
    val employee: Boolean
): JWTPayloadHolder(payload)

@OptIn(InternalAPI::class)
fun Application.configureAuthentication() {
    install(Authentication) {
        jwt(JWT_AUTH_NAME) {
            realm = JWT_REALM_NAME
            verifier(
                JWT.require(Algorithm.HMAC256(JWT_HMAC))
                    .withAudience(JWT_AUDIENCE)
                    .withIssuer(JWT_ISSUER)
                    .build()
            )
            validate { credential ->
                val userid = credential.payload.getClaim("userId").toString()
                if (userid.isBlank() || !userid.toIntPossible()) {
                    printTextLog("[Authentication::validate] 'userid' is null")
                    attributes.put(AUTH_ERROR_KEY, "Token field 'userid' is null")
                    return@validate null
                }

                val isEmployee = credential.payload.getClaim("employee").toString().lowercase().toBooleanStrictOrNull()
                if (isEmployee == null) {
                    printTextLog("[Authentication::validate] 'employee' is null")
                    attributes.put(AUTH_ERROR_KEY, "Token field 'employee' is null")
                    return@validate null
                }

                val findedId: Int
                val role: EnumBearerRoles
                if (isEmployee) {
                    val findedEmployee = Employees().getDataFromId(userid.toIntOrNull())
                    if (findedEmployee == null) {
                        printTextLog("[Authentication::validate] dont find Employee with id '$userid'")
                        attributes.put(AUTH_ERROR_KEY, "Dont find Employee with id '$userid'")
                        return@validate null
                    }
                    findedId = findedEmployee.id
                    role = findedEmployee.getRoleAsEnum()
                } else {
                    val findedClient = Clients().getDataFromId(userid.toIntOrNull())
                    if (findedClient == null) {
                        printTextLog("[Authentication::validate] dont find Clients with id '$userid'")
                        attributes.put(AUTH_ERROR_KEY, "Dont find Clients with id '$userid'")
                        return@validate null
                    }
                    findedId = findedClient.id
                    role = findedClient.getRoleAsEnum()
                }

                if (credential.payload.audience.contains(JWT_AUDIENCE)) {
                    if (credential.payload.expiresAt.before(Date())) {
                        printTextLog("[Authentication::validate] token is expired at ${credential.payload.expiresAt}")
                        attributes.put(AUTH_ERROR_KEY, "Token is expired at ${credential.payload.expiresAt}")
                        return@validate null
                    }
                    RoleAwareJWT(credential.payload, findedId, role, isEmployee)
                } else {
                    null
                }
            }
            authHeader { call ->
                val token = call.request.cookies["era_auth_token"]
                token?.let { HttpAuthHeader.Single("Bearer", it) }
            }
        }
    }

    intercept(ApplicationCallPipeline.Plugins) {
        val cookieToken = call.request.cookies["era_auth_token"]
        val verify = verifyJwtToken(cookieToken)
        if (cookieToken != null && verify == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return@intercept
        }
        if (cookieToken != null) {
            call.request.setHeader(HttpHeaders.Authorization, listOf("Bearer $cookieToken"))
        }
        proceed()
    }
}

fun verifyJwtToken(token: String?): DecodedJWT? {
    return try {
        if (token == null) return null
        val verifier = JWT.require(Algorithm.HMAC256(JWT_HMAC))
            .withIssuer(JWT_ISSUER)
            .withAudience(JWT_AUDIENCE)
            .build()

        verifier.verify(token).takeIf {
            val result = it.expiresAt.after(Date())
            result
        }
    } catch (e: TokenExpiredException) {
        printTextLog("[Authentication::verifyJwtToken] TokenExpiredException: ${e.message}")
        null
    } catch (e: JWTVerificationException) {
        printTextLog("[Authentication::verifyJwtToken] JWTVerificationException: ${e.message}")
        null
    } catch (e: Exception) {
        printTextLog("[Authentication::verifyJwtToken] Unknown exception while verifying token: ${e.message}")
        null
    }
}