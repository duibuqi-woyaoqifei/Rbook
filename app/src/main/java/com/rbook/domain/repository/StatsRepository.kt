package com.rbook.domain.repository

import com.rbook.data.local.entity.DailyStatsEntity
import kotlinx.coroutines.flow.Flow

interface StatsRepository {
    fun getAllStats(): Flow<List<DailyStatsEntity>>
    suspend fun incrementReadingTime(minutes: Long)
    suspend fun recordBookOpened()
}
