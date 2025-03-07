package com.servera.repository

import com.github.benmanes.caffeine.cache.Caffeine
import com.google.common.hash.Hashing
import com.servera.config.RequestProperties
import com.servera.protocol.DataDto
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.Duration

@Repository
class RedisRepository(
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    private val requestProperties: RequestProperties
) {

    // Caffeine 캐시 설정: 최대 10만 개 항목, TTL 5분
    private val localCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(requestProperties.limitTTL))
        .maximumSize(100_000)
        .build<String, Long>()

    fun isDuplicateRequestAllowed(userId: String, requestData: DataDto): Mono<Boolean> {
        val requestHash = hashRequestData(requestData)
        val redisKey = "request:$userId:$requestHash"

        // 로컬 캐시 먼저 확인
        val cachedCount = localCache.getIfPresent(redisKey)
        if (cachedCount != null) {
            return Mono.just(cachedCount <= requestProperties.duplicateLimit)
        }

        // Redis에서 조회 및 캐시 갱신
        return redisTemplate.opsForValue().setIfAbsent(redisKey, "1", Duration.ofMinutes(requestProperties.limitTTL))
            .flatMap { isFirstRequest ->
                if (isFirstRequest) {
                    localCache.put(redisKey, 1L)
                    Mono.just(true)
                } else {
                    redisTemplate.opsForValue().increment(redisKey, 1)
                        .map { count ->
                            localCache.put(redisKey, count) // 로컬 캐시 동기화
                            count <= requestProperties.duplicateLimit
                        }
                }
            }
    }

    fun hashRequestData(requestData: DataDto): String {
        val jsonData = requestData.toString()
        return Hashing.murmur3_128()
            .hashString(jsonData, Charsets.UTF_8)
            .toString()
    }
}
