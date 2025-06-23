package com.example.redis

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlinx.serialization.json.Json

@OptIn(ExperimentalLettuceCoroutinesApi::class)
class RedisManager(private val redisUri: String) {
    private val client: RedisClient = RedisClient.create(redisUri)
    private val connection: StatefulRedisConnection<String, String> = client.connect()
    val redis: RedisCoroutinesCommands<String, String> = connection.coroutines()
    val json = Json { ignoreUnknownKeys = true }

    suspend inline fun <reified T> cache(key: String, value: T?, ttl: Long? = null, serializer: (T) -> String = { json.encodeToString(it) }) {
        value?.let {
            redis.set(key, serializer(it))
            ttl?.let { expire -> redis.expire(key, expire) }
        } ?: redis.del(key)
    }

    suspend inline fun <reified T> get(key: String, deserializer: (String) -> T = { json.decodeFromString(it) }): T? {
        return redis.get(key)?.let(deserializer)
    }

    fun close() {
        connection.close()
        client.shutdown()
    }
}