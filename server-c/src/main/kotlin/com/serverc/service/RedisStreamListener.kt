package com.serverc.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.serverc.protocol.FinalDataDto
import com.serverc.repository.FinalDataRepository
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers

@Component
class RedisStreamListener(
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val redisConnectionFactory: RedisConnectionFactory,
    private val objectMapper: ObjectMapper,
    private val finalDataRepository: FinalDataRepository
) {
    private val logger = LoggerFactory.getLogger(RedisStreamListener::class.java)
    private val streamReceiver = StreamReceiver.create(redisTemplate.connectionFactory)
    private val streamKey = "request-stream"
    private val consumerGroup = "request-group"

    init {
        createStreamIfNotExists()
        subscribeToRedisStream()
    }

    /**
     * Redis Stream 및 Consumer Group을 안전하게 생성
     */
    private fun createStreamIfNotExists() {
        val connection = redisConnectionFactory.connection
        val streamCommands = connection.streamCommands()

        try {
            // Stream 존재 여부 체크
            val streamExists = runCatching { streamCommands.xLen(streamKey.toByteArray()) ?: 0 }
                .getOrDefault(0) > 0

            if (!streamExists) {
                redisTemplate.opsForStream<String, String>().add(streamKey, mapOf("init" to "true")).subscribe(
                    { logger.info("Stream '$streamKey' created with init entry.") },
                    { error -> logger.error("Error creating stream '$streamKey': ${error.message}") }
                )
            }

            // Consumer Group 존재 여부 체크 후 생성
            val groups = streamCommands.xInfoGroups(streamKey.toByteArray())
            if (groups == null || groups.isEmpty) {
                streamCommands.xGroupCreate(
                    streamKey.toByteArray(),
                    consumerGroup,
                    ReadOffset.from("0"),
                    true
                )
            }
        } catch (ex: Exception) {
            logger.error("Error during stream initialization: ${ex.message}")
        }
    }

    /**
     * Redis Stream을 구독하여 데이터 처리
     */
    private fun subscribeToRedisStream() {
        streamReceiver.receiveAutoAck(
            Consumer.from(consumerGroup, "c-consumer"),
            StreamOffset.create(streamKey, ReadOffset.lastConsumed())
        )
            .publishOn(Schedulers.boundedElastic()) // 백그라운드 스레드에서 실행
            .buffer(1000) // 1000개 단위로 처리
            .flatMap { records ->
                val dataList = records.mapNotNull { record ->
                    val jsonData = record.value["data"]
                    jsonData?.let { objectMapper.readValue(it, FinalDataDto::class.java) }
                }

                if (dataList.isNotEmpty()) {
                    batchInsert(dataList)
                } else {
                    Flux.empty()
                }
            }
            .subscribe()
    }

    /**
     * Batch Insert 실행 (1000개 단위)
     */
    private fun batchInsert(dataList: List<FinalDataDto>) =
        Flux.fromIterable(dataList.chunked(1000))
            .flatMap { batch ->
                finalDataRepository.saveAll(batch)
                    .collectList()
                    .retry(3)
                    .doOnError { error -> logger.error("Batch insert error: ${error.message}", error) }
            }
            .then()
}
