package com.rbook.data.repository

import com.rbook.data.local.dao.StatsDao
import com.rbook.data.local.entity.DailyStatsEntity
import com.rbook.domain.repository.StatsRepository
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class StatsRepositoryImpl @Inject constructor(
    private val statsDao: StatsDao
) : StatsRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private fun getCurrentDate() = dateFormat.format(Date())

    override fun getAllStats(): Flow<List<DailyStatsEntity>> {
        return statsDao.getAllStats()
    }

    override suspend fun incrementReadingTime(minutes: Long) {
        val date = getCurrentDate()
        val current = statsDao.getStatsByDate(date) ?: DailyStatsEntity(date)
        statsDao.insertOrUpdate(current.copy(readingDurationMinutes = current.readingDurationMinutes + minutes))
    }

    override suspend fun recordBookOpened() {
        val date = getCurrentDate()
        val current = statsDao.getStatsByDate(date) ?: DailyStatsEntity(date)
        statsDao.insertOrUpdate(current.copy(booksOpenedCount = current.booksOpenedCount + 1))
    }
}
