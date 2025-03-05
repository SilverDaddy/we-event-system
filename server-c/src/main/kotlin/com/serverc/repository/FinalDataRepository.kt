package com.serverc.repository

import com.serverc.protocol.FinalDataDto
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository

@Repository
interface FinalDataRepository : R2dbcRepository<FinalDataDto, Long>
