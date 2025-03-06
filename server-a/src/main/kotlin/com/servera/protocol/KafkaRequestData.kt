package com.servera.protocol

data class KafkaRequestData(
    val userId: String,
    val requestData: List<DataDto>
)
