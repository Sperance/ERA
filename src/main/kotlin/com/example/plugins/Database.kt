package com.example.plugins

import com.example.datamodel.clients.Clients.Companion.tbl_clients
import com.example.datamodel.clients.configureClients
import com.example.datamodel.feedbacks.FeedBacks.Companion.tbl_feedbacks
import com.example.datamodel.feedbacks.configureFeedbacks
import com.example.datamodel.records.Records.Companion.tbl_records
import com.example.datamodel.records.configureRecords
import com.example.datamodel.services.Services.Companion.tbl_services
import com.example.datamodel.services.configureServices
import io.ktor.server.application.*
import io.r2dbc.spi.ConnectionFactoryOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.komapper.core.ExecutionOptions
import org.komapper.core.dsl.QueryDsl
import org.komapper.r2dbc.R2dbcDatabase

private val connectionFactory: ConnectionFactoryOptions = ConnectionFactoryOptions.builder()
    .option(ConnectionFactoryOptions.DRIVER, "postgresql")
    .option(ConnectionFactoryOptions.HOST, "localhost")
    .option(ConnectionFactoryOptions.PORT, 5432)
    .option(ConnectionFactoryOptions.USER, "postgres_rpg")
    .option(ConnectionFactoryOptions.PASSWORD, "22322137")
    .option(ConnectionFactoryOptions.DATABASE, "postgres_rpg")
    .build()

val db = R2dbcDatabase(connectionFactory, executionOptions = ExecutionOptions(suppressLogging = true))

fun Application.configureDatabases() {
    launch(Dispatchers.IO) {
        db.withTransaction {
            db.runQuery { QueryDsl.create(tbl_clients) }
            db.runQuery { QueryDsl.create(tbl_services) }
            db.runQuery { QueryDsl.create(tbl_feedbacks) }
            db.runQuery { QueryDsl.create(tbl_records) }
        }
        configureClients()
        configureServices()
        configureFeedbacks()
        configureRecords()
    }
}
