package com.rbook.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rbook.data.local.dao.BookDao
import com.rbook.data.local.dao.StatsDao
import com.rbook.data.local.entity.BookEntity
import com.rbook.data.local.entity.DailyStatsEntity

@Database(entities = [BookEntity::class, DailyStatsEntity::class], version = 1, exportSchema = false)
abstract class RBookDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun statsDao(): StatsDao
}
