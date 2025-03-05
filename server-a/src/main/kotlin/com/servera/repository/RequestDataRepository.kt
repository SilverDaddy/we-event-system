package com.servera.repository

import com.servera.entity.RequestData
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface RequestDataRepository : ReactiveCrudRepository<RequestData, Long>
