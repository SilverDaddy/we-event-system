package com.servera.repository

import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class RedisRepository(private val redisTemplate: ReactiveRedisTemplate<String, String>) {

    private val REQUEST_LIMIT = 10

    /**
     *  유저의 요청 횟수를 증가 (100회 초과 시 예외 발생)
     */
    fun incrementRequestCount(userId: String): Mono<Long> {
        val countKey = "user:$userId:requests"
        return redisTemplate.opsForValue().increment(countKey) // 바로 증가
    }

    /**
     *  현재 요청 횟수 조회 (Mono<Long> 반환)
     */
    fun incrementAndCheckLimit(userId: String): Mono<Long> {
        val countKey = "user:$userId:requests"

        return redisTemplate.opsForValue().increment(countKey) // 한 번만 조회 후 증가
            .flatMap { currentCount ->
                if (currentCount > REQUEST_LIMIT) {
                    Mono.error(IllegalStateException("요청 제한 초과: 최대 100회까지만 요청 가능"))
                } else {
                    Mono.just(currentCount)
                }
            }
    }
}
