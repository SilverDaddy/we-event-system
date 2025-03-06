package com.serverb.protocol

data class KafkaRequestData(
    val userId: String, // userId
    val requestData: List<DataDto>
)
