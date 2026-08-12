package com.rbook.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

    private val migration1To2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `translation_cache` (`textHash` TEXT NOT NULL, `originalText` TEXT NOT NULL, `translatedText` TEXT NOT NULL, `targetLang` TEXT NOT NULL, `engine` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`textHash`))"
            )
        }
    }

    private val migration2To3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE books ADD COLUMN currentPage INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val migration3To4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE IF EXISTS translation_cache")
        }
    }

    private val migration4To5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE books ADD COLUMN epubLocator TEXT")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RBookDatabase {
        return Room.databaseBuilder(
            context,
            RBookDatabase::class.java,
            "rbook_db"
        )
            .addMigrations(migration1To2, migration2To3, migration3To4, migration4To5)
            .build()
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
