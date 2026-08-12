package com.rbook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String,
    val path: String,
    val format: String, // EPUB, PDF, TXT
    val coverPath: String? = null,
    val lastReadTimestamp: Long = System.currentTimeMillis(),
    val progress: Float = 0f,
    val totalSize: Long = 0
)
