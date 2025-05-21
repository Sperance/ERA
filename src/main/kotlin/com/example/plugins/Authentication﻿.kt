package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.Payload
import com.example.datamodel.authentications.Authentications
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.bearer
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import com.example.basemodel.ResultResponse
import com.example.currectDatetime
import com.example.datamodel.clients.Clients
import com.example.datamodel.employees.Employees
import com.example.enums.EnumBearerRoles
import com.example.enums.EnumHttpCode
import com.example.helpers.delete
import com.example.helpers.update
import com.example.logging.DailyLogger.printTextLog
import com.example.respond
import com.example.schedulers.hoursTaskScheduler
import com.example.security.hashString
import com.example.toIntPossible
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.auth.jwt.JWTPayloadHolder
import io.ktor.server.request.path
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.InternalAPI
import kotlinx.datetime.LocalDateTime
import java.util.Date

const val JWT_HMAC = "secrets_era"
const val JWT_ISSUER = "ktor.era.io"
const val JWT_AUDIENCE = "ktor.audience.era"
const val JWT_AUTH_NAME = "auth-jwt-cookie"

class RoleAwareJWT(
    payload: Payload,
    val userId: Int,
    val employee: Boolean
): JWTPayloadHolder(payload)

@OptIn(InternalAPI::class)
fun Application.configureAuthentication() {
    install(Authentication) {
        bearer("auth-bearer") {
            realm = "Access to the server side API"
            authenticate { tokenCredential ->
                val findedToken = Authentications.repo_authentications.getRepositoryData().find { it.token == tokenCredential.token }
                if (findedToken != null) {
                    UserIdPrincipal(findedToken.token!!)
                } else {
                    null
                }
            }
        }

        jwt(JWT_AUTH_NAME) {
            realm = "ktor.io"
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
                    return@validate null
                }

                val isEmployee = credential.payload.getClaim("employee").toString().lowercase().toBooleanStrictOrNull()
                if (isEmployee == null) {
                    printTextLog("[Authentication::validate] 'employee' is null")
                    return@validate null
                }

                printTextLog("[VALIDATE] userId: $userid employee: $isEmployee")

                val findedId: Int
                if (isEmployee) {
                    val findedEmployee = Employees.repo_employees.getDataFromId(userid.toIntOrNull())
                    if (findedEmployee == null) {
                        printTextLog("[Authentication::validate] dont find Employee with id '$userid'")
                        return@validate null
                    }
                    printTextLog("[VALIDATE] findedEmployee: $findedEmployee role: ${findedEmployee.role} enums: ${EnumBearerRoles.entries.joinToString { enm -> "${enm.name}: " + hashString(enm.name.uppercase(), findedEmployee.salt!!) }}")
                    findedId = findedEmployee.id
                } else {
                    val findedClient = Clients.repo_clients.getDataFromId(userid.toIntOrNull())
                    if (findedClient == null) {
                        printTextLog("[Authentication::validate] dont find Clients with id '$userid'")
                        return@validate null
                    }
                    printTextLog("[VALIDATE] findedClient: $findedClient role: ${findedClient.role} enums: ${EnumBearerRoles.entries.joinToString { enm -> "${enm.name}: " + hashString(enm.name.uppercase(), findedClient.salt!!) }}")
                    findedId = findedClient.id
                }

                if (credential.payload.audience.contains(JWT_AUDIENCE)) {
                    if (credential.payload.expiresAt.before(Date())) {
                        printTextLog("[Authentication::validate] token expired at ${credential.payload.expiresAt}")
                        return@validate null
                    }
                    RoleAwareJWT(credential.payload, findedId, isEmployee)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(ResultResponse.Error(EnumHttpCode.AUTHORISATION, mutableMapOf("error" to "Token is not valid or has expired")))
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
            hoursTaskScheduler.execute_clearUnusedTokens()
            call.respond(HttpStatusCode.Unauthorized)
            return@intercept
        }
        if (cookieToken != null) {
            call.request.setHeader(HttpHeaders.Authorization, listOf("Bearer $cookieToken"))
        }
        proceed()
    }
}

suspend fun verifyJwtToken(token: String?): DecodedJWT? {
    return try {
        if (token == null) return null
        val verifier = JWT.require(Algorithm.HMAC256(JWT_HMAC))
            .withIssuer(JWT_ISSUER)
            .withAudience(JWT_AUDIENCE)
            .build()

        verifier.verify(token).takeIf {
            val result = it.expiresAt.after(Date())
            if (result) {
                var findedTokenInDB = Authentications.repo_authentications.getRepositoryData().find { repoToken -> repoToken.token == token  }
                if (findedTokenInDB != null) {
                    if (findedTokenInDB.dateExpired!! <= LocalDateTime.currectDatetime()) {
                        printTextLog("[Authentication::verifyJwtToken] Token in database is expired $findedTokenInDB")
                        return null
                    }
                    findedTokenInDB.dateUsed = LocalDateTime.currectDatetime()
                    findedTokenInDB = findedTokenInDB.update("Authentication::verifyJwtToken")
                    Authentications.repo_authentications.updateData(findedTokenInDB)
                }
            }
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