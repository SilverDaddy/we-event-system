package com.servera.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "request")
class RequestProperties {
    var duplicateLimit: Int = 3 // 기본값 3
    var limitTTL: Long = 5 // 기본값 5분
}
