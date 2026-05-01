package com.rbook.domain.repository

import com.rbook.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun getAllBooks(): Flow<List<Book>>
    suspend fun getBookById(id: Long): Book?
    suspend fun saveBook(book: Book): Long
    suspend fun deleteBook(id: Long)
}
