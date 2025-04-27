package com.example.plugins

import com.example.datamodel.authentications.Authentications
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.bearer

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
    }
}