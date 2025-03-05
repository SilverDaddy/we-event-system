package com.serverc.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.serverc.protocol.FinalDataDto
import com.serverc.repository.FinalDataRepository
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import reactor.core.scheduler.Schedulers

@Component
class RedisStreamListener(
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    private val redisConnectionFactory: RedisConnectionFactory,
    private val objectMapper: ObjectMapper,
    private val finalDataRepository: FinalDataRepository,
) {
    private val streamReceiver = StreamReceiver.create(redisTemplate.connectionFactory)
    private val streamKey = "request-stream"
    private val consumerGroup = "request-group"

    init {
        createStreamIfNotExists()
        subscribeToRedisStream()
    }

    private fun createStreamIfNotExists() {
        val connection = redisConnectionFactory.connection
        val streamCommands = connection.streamCommands()

        val opsForStream = redisTemplate.opsForStream<String, String>()

        val streamExists = runCatching { streamCommands.xLen(streamKey.toByteArray()) ?: 0 }.getOrDefault(0) > 0

        if (!streamExists) {
            try {
                opsForStream.add(streamKey, mapOf("init" to "true")).subscribe()

                streamCommands.xGroupCreate(
                    streamKey.toByteArray(),
                    consumerGroup,
                    ReadOffset.from(">"),
                    true
                )
            } catch (ex: Exception) {
                println("Consumer Group already exists or Redis Stream is initialized.")
            }
        }
    }

    private fun subscribeToRedisStream() {
        streamReceiver.receiveAutoAck(
            Consumer.from("request-group", "c-consumer"),
            StreamOffset.create("request-stream", ReadOffset.from(">"))
        )
            .publishOn(Schedulers.boundedElastic())
            .buffer(100_000)
            .flatMap { records ->
                val dataList = records.mapNotNull { record ->
                    val jsonData = record.value["data"]
                    jsonData?.let { objectMapper.readValue(it, FinalDataDto::class.java) }
                }

                finalDataRepository.saveAll(dataList).collectList().then()
            }
            .subscribe()
    }
}
