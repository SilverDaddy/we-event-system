package com.servera.contoller

import com.servera.protocol.DataDto
import com.servera.service.AService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactor.mono
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class AController(private val aService: AService) {

    @PostMapping("/stream")
    fun handleStreamRequest(
        @RequestHeader("User-Id") userId: String,
        @RequestBody requestDataFlow: Flow<DataDto>
    ) = mono {
        requestDataFlow
            .map { data -> data to aService.isDuplicateRequestAllowed(userId, data) }
            .filter { it.second }
            .map { it.first }
            .buffer(1000)
            .toList(mutableListOf())
            .let { batch ->
                if (batch.isNotEmpty()) {
                    aService.processBatch(userId, batch)
                }
            }
        "A 서버: Streaming 요청 처리 완료"
    }
}
