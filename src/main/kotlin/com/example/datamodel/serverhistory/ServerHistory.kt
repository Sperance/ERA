package com.example.datamodel.serverhistory

import com.example.basemodel.BaseRepository
import com.example.basemodel.IntBaseDataImpl
import com.example.currectDatetime
import com.example.helpers.create
import com.example.interfaces.IntPostgreTable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable
import org.komapper.annotation.KomapperVersion
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.metamodel.EntityMetamodel

@Serializable
@KomapperEntity
@KomapperTable("tbl_serverhistory")
data class ServerHistory(
    @KomapperId
    @KomapperAutoIncrement
    @KomapperColumn(name = "serverhistory_id")
    override val id: Int = 0,
    var code: Int = 0,
    var message: String = "",
    var value: String = "",
    @Transient
    @KomapperVersion
    override val version: Int = 0,
    @Transient
    override val createdAt: LocalDateTime = LocalDateTime.currectDatetime(),
) : IntPostgreTable<ServerHistory> {
    companion object {
        val tbl_serverhistory = Meta.serverHistory

        fun addRecord(code: Int, message: String, value: String) {
            CoroutineScope(Dispatchers.IO).launch {
                ServerHistory(code = code, message = message, value = value).create("ServerHistory::addRecord")
            }
        }
    }

    override fun getTable() = tbl_serverhistory
}