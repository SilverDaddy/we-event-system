package com.serverb.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.serverb.protocol.DataDto
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class RedisRepository(
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    /**
     * 요청 데이터를 Redis에 저장 (Key: userId:requestCount)
     */
    fun saveData(redisKey: String, requestData: List<DataDto>): Mono<Void> {
        val operations = redisTemplate.opsForValue()
        val tasks = requestData.map { data ->
            val jsonValue = objectMapper.writeValueAsString(data)
            operations.set(redisKey, jsonValue).then()
        }

        return if (tasks.isEmpty()) Mono.empty() else Mono.`when`(tasks)
    }

    /**
     * Redis Stream을 통해 C 서버로 전송
     */
    fun pushToStream(streamKey: String, requestData: List<DataDto>): Mono<Void> {
        val streamOperations = redisTemplate.opsForStream<String, String>()
        val tasks = requestData.map { data ->
            val jsonValue = objectMapper.writeValueAsString(data)
            streamOperations.add(streamKey, mapOf("data" to jsonValue)).then()
        }

        return if (tasks.isEmpty()) Mono.empty() else  Mono.`when`(tasks)
    }
}
