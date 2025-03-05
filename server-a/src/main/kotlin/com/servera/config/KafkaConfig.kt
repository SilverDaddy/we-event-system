package com.servera.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaConfig {

    @Bean
    fun requestTopic(): NewTopic {
        return NewTopic("request-topic", 3, 1) // 파티션 3개, 복제본 1개
    }
}
