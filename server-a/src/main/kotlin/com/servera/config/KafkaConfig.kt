package com.servera.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaConfig {

    @Bean
    fun requestTopic(): NewTopic {
        return NewTopic("request-topic", 20, 1) // topic, partition, replication
    }
}
