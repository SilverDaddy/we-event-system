package com.servera.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("request_data")
data class RequestData(
    val field1: String,
    val field2: String,
    val field3: String,
    val field4: String,
    val field5: String,
    val field6: String,
    val field7: String,
    val field8: String,
    val field9: String,
    val field10: String,
    @Id val id: Long? = null,
)
