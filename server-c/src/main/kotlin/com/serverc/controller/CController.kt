package com.serverc.controller

import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api")
class CController(
    private val redisTemplate: ReactiveRedisTemplate<String, String>
) {
    @GetMapping("/stream-status")
    fun getStreamStatus(): Mono<String> {
        val streamKey = "request-stream"

        return redisTemplate.hasKey(streamKey)
            .map { exists ->
                if (exists) "Redis Stream '$streamKey' is active and receiving data."
                else "Redis Stream '$streamKey' does not exist."
            }
    }
}
