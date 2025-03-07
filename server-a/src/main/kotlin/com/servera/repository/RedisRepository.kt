package com.servera.repository

import com.servera.config.RequestProperties
import com.servera.protocol.DataDto
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.security.MessageDigest
import java.time.Duration

@Repository
class RedisRepository(
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    private val requestProperties: RequestProperties
) {

    /**
     * 중복 요청 체크 및 카운트 증가 (최대 N회 허용)
     */
    fun isDuplicateRequestAllowed(userId: String, requestData: DataDto): Mono<Boolean> {
        val requestHash = hashRequestData(requestData)
        val redisKey = "request:$userId:$requestHash"

        return redisTemplate.opsForValue().setIfAbsent(redisKey, "1", Duration.ofMinutes(requestProperties.limitTTL))
            .flatMap { isFirstRequest ->
                if (isFirstRequest) {
                    // 최초 요청: 허용
                    Mono.just(true)
                } else {
                    // 이미 존재하는 경우: 카운트 증가 후 duplicateLimit 이내인지 확인
                    redisTemplate.opsForValue().increment(redisKey, 1)
                        .map { count -> count <= requestProperties.duplicateLimit }
                }
            }
    }

    fun hashRequestData(requestData: DataDto): String {
        val jsonData = requestData.toString()
        val digest = MessageDigest.getInstance("SHA-256").digest(jsonData.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
