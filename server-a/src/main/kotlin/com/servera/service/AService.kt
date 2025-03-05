package com.servera.service

import com.servera.entity.RequestData
import com.servera.protocol.DataDto
import com.servera.protocol.KafkaRequestData
import com.servera.repository.RedisRepository
import com.servera.repository.RequestDataRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class AService(
    private val redisRepository: RedisRepository,
    private val requestDataRepository: RequestDataRepository,
    private val kafkaTemplate: KafkaTemplate<String, KafkaRequestData>
) {
    /**
     * 요청 횟수를 증가하고 100회 제한을 체크한 후 반환
     */
    suspend fun incrementAndGetRequestCount(userId: String): Long {
        return redisRepository.incrementAndCheckLimit(userId).awaitSingle()
    }
    /**
     * 요청 데이터 처리 (MySQL 저장 + Kafka 메시지 전송 병렬 실행)
     */
    suspend fun processRequest(userId: String, requestData: List<DataDto>, requestCount: Long) = coroutineScope {
        // MySQL 저장
        launch {
            requestDataRepository.saveAll(
                requestData.map {
                    RequestData(
                        userId = userId,
                        field1 = it.field1,
                        field2 = it.field2,
                        field3 = it.field3,
                        field4 = it.field4,
                        field5 = it.field5,
                        field6 = it.field6,
                        field7 = it.field7,
                        field8 = it.field8,
                        field9 = it.field9,
                        field10 = it.field10
                    )
                }
            ).collectList().awaitSingle()
        }

        // Kafka 메시지 전송 (userId & requestCount 포함)
        launch {
            val kafkaData = KafkaRequestData(userId, requestCount, requestData)
            kafkaTemplate.send("request-topic", kafkaData)
        }
    }
}
