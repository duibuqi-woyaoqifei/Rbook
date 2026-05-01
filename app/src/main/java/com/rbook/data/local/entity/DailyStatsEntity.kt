package com.rbook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey val date: String, // yyyy-MM-dd
    val readingDurationMinutes: Long = 0,
    val booksOpenedCount: Int = 0
)
