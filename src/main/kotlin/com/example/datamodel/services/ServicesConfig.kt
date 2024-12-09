package com.example.datamodel.services

import com.example.datamodel.IntBaseDataImpl
import com.example.datamodel.ResultResponse
import com.example.datamodel.clearTable
import com.example.datamodel.records.configureRecords
import com.example.datamodel.serverhistory.ServerHistory
import com.example.printCallLog
import com.example.respond
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureServices() {
    routing {
        route("/services") {

            get("/structure") {
                this@configureServices.printCallLog(call)
                call.respond(ResultResponse.Success(HttpStatusCode.OK, Services().getCommentArray()))
            }

            get("/clearTable") {
                this@configureServices.printCallLog(call)
                Services().clearTable()
                ServerHistory.addRecord(1, "Очистка таблицы Services", "")
                call.respond(ResultResponse.Success(HttpStatusCode.OK, "Таблица успешно очищена"))
            }

            get("/all") {
                this@configureServices.printCallLog(call)
                call.respond(Services().get(call, IntBaseDataImpl.RequestParams()))
            }

            get("/{id}") {
                this@configureServices.printCallLog(call)
                call.respond(Services().getId(call, IntBaseDataImpl.RequestParams()))
            }

            post("/update") {
                this@configureServices.printCallLog(call)
                call.respond(Services().updateFormData(call, IntBaseDataImpl.RequestParams(), Services.serializer()))
            }

            post {
                this@configureServices.printCallLog(call)
                call.respond(Services().postFormData(call, IntBaseDataImpl.RequestParams(), Services.serializer()))
            }

            delete {
                this@configureServices.printCallLog(call)
                call.respond(Services().delete(call, IntBaseDataImpl.RequestParams()))
            }
        }
    }
}
