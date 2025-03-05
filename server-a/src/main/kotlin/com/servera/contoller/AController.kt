package com.servera.contoller

import com.servera.protocol.DataDto
import com.servera.service.AService
import kotlinx.coroutines.reactor.mono
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class AController(private val aService: AService) {

    @PostMapping("/request")
    fun handleRequest(
        @RequestHeader("User-Id") userId: String,
        @RequestBody requestData: List<DataDto>
    ) = mono {
        val requestCount = aService.incrementAndGetRequestCount(userId)

        if (requestCount > 10) {
            "A 서버: 요청 제한 초과 (429 Too Many Requests)"
        } else {
            aService.processRequest(userId, requestData, requestCount)
            "A 서버: 요청 처리 완료"
        }
    }
}
