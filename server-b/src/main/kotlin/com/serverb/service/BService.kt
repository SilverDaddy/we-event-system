package com.serverb.service

import com.serverb.protocol.KafkaRequestData
import com.serverb.repository.RedisRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class BService(
    private val redisRepository: RedisRepository
) {
    /**
     * Kafka 메시지 배치 수신 후 Redis 저장 & Redis Stream으로 전달
     */
    @KafkaListener(
        topics = ["request-topic"],
        groupId = "b-server-group",
        concurrency = "16",
        batch = "true"
    )
    fun consumeRequest(kafkaDataBatch: List<KafkaRequestData>) {
        val redisTasks = kafkaDataBatch.flatMap { kafkaData ->
            val redisKey = "user:${kafkaData.userId}:${System.currentTimeMillis()}"
            listOf(
                redisRepository.saveData(redisKey, kafkaData.requestData),
                redisRepository.pushToStream("request-stream", kafkaData.requestData)
            )
        }

        if (redisTasks.isNotEmpty()) {
            Mono.`when`(redisTasks).subscribe()
        }
    }
}
