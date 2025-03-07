package com.serverb.controller

import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api")
class BController(
    private val redisTemplate: ReactiveRedisTemplate<String, String>
) {

    /**
     * Redis Stream 상태 확인 API
     */
    @GetMapping("/stream-status")
    fun getStreamStatus(): Mono<ResponseEntity<String>> {
        val streamKey = "request-stream"
        return redisTemplate.hasKey(streamKey)
            .map { exists ->
                if (exists) ResponseEntity.ok("Redis Stream '$streamKey' is active.")
                else ResponseEntity.status(404).body("Redis Stream '$streamKey' does not exist.")
            }
    }

}
