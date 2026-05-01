package com.rbook.di

import android.content.Context
import androidx.room.Room
import com.rbook.data.local.RBookDatabase
import com.rbook.data.local.dao.BookDao
import com.rbook.data.local.dao.StatsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RBookDatabase {
        return Room.databaseBuilder(
            context,
            RBookDatabase::class.java,
            "rbook_db"
        ).build()
    }

    @Provides
    fun provideBookDao(database: RBookDatabase): BookDao {
        return database.bookDao()
    }

    @Provides
    fun provideStatsDao(database: RBookDatabase): StatsDao {
        return database.statsDao()
    }
}
