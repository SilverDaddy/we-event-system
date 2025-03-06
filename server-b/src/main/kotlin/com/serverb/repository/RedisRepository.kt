package com.serverb.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.serverb.protocol.DataDto
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

@Repository
class RedisRepository(
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    /**
     * 요청 데이터를 Redis에 저장 (Reactive 방식으로 Batch 처리)
     */
    fun saveData(redisKey: String, requestData: List<DataDto>): Mono<Void> {
        val operations = redisTemplate.opsForValue()

        return Flux.fromIterable(requestData)
            .flatMap { data ->
                val jsonValue = objectMapper.writeValueAsString(data)
                operations.set(redisKey, jsonValue).then()
            }
            .then()
    }

    /**
     * Redis Stream을 통해 C 서버로 전송 (Reactive 방식으로 Batch 처리)
     */
    fun pushToStream(streamKey: String, requestData: List<DataDto>): Mono<Void> {
        val streamOperations = redisTemplate.opsForStream<String, String>()

        return Flux.fromIterable(requestData)
            .flatMap { data ->
                val jsonValue = objectMapper.writeValueAsString(data)
                streamOperations.add(streamKey, mapOf("data" to jsonValue)).then()
            }
            .then()
    }
}
