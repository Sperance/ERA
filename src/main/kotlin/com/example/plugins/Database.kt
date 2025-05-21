package com.example.plugins

import com.example.applicationTomlSettings
import com.example.basemodel.ResultResponse
import com.example.datamodel.authentications.Authentications.Companion.tbl_authentications
import com.example.datamodel.catalogs.Catalogs.Companion.tbl_catalogs
import com.example.datamodel.catalogs.configureCatalogs
import com.example.datamodel.clients.Clients.Companion.tbl_clients
import com.example.datamodel.clients.configureClients
import com.example.datamodel.clientsschelude.ClientsSchelude.Companion.tbl_clientsschelude
import com.example.datamodel.feedbacks.FeedBacks.Companion.tbl_feedbacks
import com.example.datamodel.clientsschelude.configureClientsSchelude
import com.example.datamodel.defaults.defaultsConfig
import com.example.datamodel.employees.Employees.Companion.tbl_employees
import com.example.datamodel.employees.configureEmployees
import com.example.datamodel.feedbacks.configureFeedbacks
import com.example.datamodel.news.News.Companion.tbl_news
import com.example.datamodel.news.configureNews
import com.example.datamodel.records.Records.Companion.tbl_records
import com.example.datamodel.records.configureRecords
import com.example.datamodel.serverrequests.ServerRequests
import com.example.datamodel.serverrequests.ServerRequests.Companion.tbl_serverrequests
import com.example.datamodel.services.Services.Companion.tbl_services
import com.example.datamodel.services.configureServices
import com.example.datamodel.stockfiles.Stockfiles.Companion.tbl_stockfiles
import com.example.datamodel.stockfiles.configureStockfiles
import com.example.enums.EnumHttpCode
import com.example.logging.DailyLogger.printTextLog
import com.example.schedulers.configureSchedulers
import io.ktor.server.application.*
import io.ktor.server.http.content.staticFiles
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.r2dbc.spi.ConnectionFactoryOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.komapper.core.ExecutionOptions
import org.komapper.core.dsl.QueryDsl
import org.komapper.r2dbc.R2dbcDatabase
import io.ktor.server.routing.get
import java.io.File
import com.example.respond

private val connectionFactory: ConnectionFactoryOptions = ConnectionFactoryOptions.builder()
    .option(ConnectionFactoryOptions.DRIVER, "postgresql")
    .option(ConnectionFactoryOptions.HOST, applicationTomlSettings?.DATABASE?.HOST?:"localhost")
    .option(ConnectionFactoryOptions.PORT, applicationTomlSettings?.DATABASE?.PORT?:5432)
    .option(ConnectionFactoryOptions.USER, applicationTomlSettings?.DATABASE?.USER?:"postgres_rpg")
    .option(ConnectionFactoryOptions.PASSWORD, applicationTomlSettings?.DATABASE?.PASSWORD?:"22322137")
    .option(ConnectionFactoryOptions.DATABASE, applicationTomlSettings?.DATABASE?.DATABASE?:"postgres_rpg")
    .build()

val db = R2dbcDatabase(connectionFactory, executionOptions = ExecutionOptions(suppressLogging = true))

fun Application.configureDatabases() {
    launch(Dispatchers.IO) {
        db.withTransaction {
            db.runQuery { QueryDsl.create(tbl_clients) }
            db.runQuery { QueryDsl.create(tbl_employees) }
            db.runQuery { QueryDsl.create(tbl_services) }
            db.runQuery { QueryDsl.create(tbl_feedbacks) }
            db.runQuery { QueryDsl.create(tbl_records) }
            db.runQuery { QueryDsl.create(tbl_stockfiles) }
            db.runQuery { QueryDsl.create(tbl_news) }
            db.runQuery { QueryDsl.create(tbl_clientsschelude) }
            db.runQuery { QueryDsl.create(tbl_catalogs) }
            db.runQuery { QueryDsl.create(tbl_authentications) }
            db.runQuery { QueryDsl.create(tbl_serverrequests) }
        }
        routing {
            staticFiles("/files", File("files"))
        }
        configureClients()
        configureEmployees()
        configureServices()
        configureFeedbacks()
        configureRecords()
        configureStockfiles()
        configureNews()
        configureClientsSchelude()
        configureCatalogs()
        configureWorkServer()

        defaultsConfig()
    }.invokeOnCompletion {
        configureSchedulers()

        ServerRequests.lauchBatchedWriteDB()

        printTextLog("[configureDatabases] Server is Started")
    }
}

fun Application.configureWorkServer() {
    routing {
        route("/server") {
            get("/status") {
                call.respond(ResultResponse.Success(EnumHttpCode.COMPLETED, "Server is Work"))
            }
        }
    }
}