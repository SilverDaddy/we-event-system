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
     * 동일 요청인지 확인 (중복 허용 횟수 초과 여부)
     */
    suspend fun isDuplicateRequestAllowed(userId: String, requestData: DataDto): Boolean {
        return redisRepository.isDuplicateRequestAllowed(userId, requestData).awaitSingle()
    }

    /**
     * Kafka 메시지 전송 + MySQL Batch Insert 동시 수행
     */
    suspend fun processBatch(userId: String, requestDataBatch: List<DataDto>) = coroutineScope {
        // MySQL Batch Insert 실행 (비동기)
        launch {
            requestDataRepository.saveAll(
                requestDataBatch.map {
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

        // Kafka 메시지 전송 실행
        launch {
            kafkaTemplate.executeInTransaction { kafkaOperations ->
                kafkaOperations.send("request-topic", KafkaRequestData(userId, requestDataBatch))
            }
        }
    }
}
