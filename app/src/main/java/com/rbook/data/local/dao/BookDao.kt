package com.rbook.data.local.dao

import androidx.room.*
import com.rbook.data.local.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY lastReadTimestamp DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Long): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity)

    @Query("UPDATE books SET progress = :progress, currentPage = :currentPage, epubLocator = :epubLocator, lastReadTimestamp = :lastReadTimestamp WHERE id = :bookId")
    suspend fun updateReadingProgress(
        bookId: Long,
        progress: Float,
        currentPage: Int,
        epubLocator: String?,
        lastReadTimestamp: Long
    )

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBookById(id: Long)

    @Query("DELETE FROM books WHERE id IN (:ids)")
    suspend fun deleteBooks(ids: List<Long>)
}
