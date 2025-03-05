package com.serverb.protocol

data class KafkaRequestData(
    val userId: String, // userId
    val requestCount: Long, //요청 횟수
    val requestData: List<DataDto>
)
