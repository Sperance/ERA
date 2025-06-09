package com.example.datamodel.records

import com.example.applicationTomlSettings
import com.example.helpers.Recordsdata
import com.example.logging.DailyLogger.printTextLog
import com.example.plugins.db
import com.example.plugins.jsonParser
import com.example.plugins.sockets.socketsRecords
import io.r2dbc.postgresql.api.PostgresqlConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.serialization.Serializable
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.QueryDsl.Companion.executeScript
import org.komapper.core.dsl.QueryDsl.Companion.executeTemplate
import org.komapper.core.dsl.query.get
import java.sql.DriverManager
import java.util.Properties

@Serializable
data class RecordChangeEvent(
    val operation: String,
    val data: Records?
)

@Serializable
data class RecordsDataEvent(
    val operation: String,
    val data: Recordsdata?
)

object RecordsChanged {

    private fun createTrigger() {
        val jdbcUrl = "jdbc:postgresql://localhost:${applicationTomlSettings!!.DATABASE.PORT}/${applicationTomlSettings!!.DATABASE.DATABASE}"
        val props = Properties().apply {
            setProperty("user", applicationTomlSettings!!.DATABASE.USER)
            setProperty("password", applicationTomlSettings!!.DATABASE.PASSWORD)
        }

        DriverManager.getConnection(jdbcUrl, props).use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute("""
                CREATE OR REPLACE FUNCTION notify_record_change()
                RETURNS TRIGGER AS $$
                DECLARE
                    notification_json jsonb;
                BEGIN
                    notification_json := jsonb_build_object(
                        'operation', TG_OP,
                        'data', CASE TG_OP
                            WHEN 'DELETE' THEN to_jsonb(OLD)
                            ELSE to_jsonb(NEW)
                        END
                    );
                    PERFORM pg_notify('record_changes', notification_json::text);
                    RETURN NEW;
                END;
                $$ LANGUAGE plpgsql;
            """)

                stmt.execute("""
                CREATE OR REPLACE TRIGGER record_change_trigger
                AFTER INSERT OR UPDATE OR DELETE ON tbl_records
                FOR EACH ROW EXECUTE FUNCTION notify_record_change();
            """)
            }
        }
    }

    fun watchOrders() = CoroutineScope(Dispatchers.IO).launch {
        createTrigger()
        val connection = db.config.connectionFactory.create().awaitFirstOrNull() as? PostgresqlConnection ?: error("Не удалось подключиться к PostgreSQL")
        connection.createStatement("LISTEN record_changes").execute().awaitFirstOrNull()
        connection.notifications.doOnNext { notification ->
            val params = notification.parameter?.replace("record_id", "id")!!
            val res = jsonParser.decodeFromString<RecordChangeEvent>(params)
            printTextLog("[RecordsChanged] TYPE: ${res.operation} VALUE: ${res.data}")
            CoroutineScope(Dispatchers.IO).launch {
                val data = RecordsDataEvent(operation = res.operation, data = res.data?.toRecordsData())
                socketsRecords.broadcast(jsonParser.encodeToString(data))
            }
        }.subscribe()
    }
}