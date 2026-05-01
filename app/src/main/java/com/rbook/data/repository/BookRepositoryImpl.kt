package com.rbook.data.repository

import com.rbook.data.local.dao.BookDao
import com.rbook.data.local.entity.BookEntity
import com.rbook.domain.model.Book
import com.rbook.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao
) : BookRepository {

    override fun getAllBooks(): Flow<List<Book>> {
        return bookDao.getAllBooks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getBookById(id: Long): Book? {
        return bookDao.getBookById(id)?.toDomain()
    }

    override suspend fun saveBook(book: Book): Long {
        return bookDao.insertBook(book.toEntity())
    }

    override suspend fun deleteBook(id: Long) {
        bookDao.deleteBookById(id)
    }

    private fun BookEntity.toDomain() = Book(
        id = id,
        title = title,
        author = author,
        path = path,
        format = format,
        coverPath = coverPath,
        progress = progress
    )

    private fun Book.toEntity() = BookEntity(
        id = id,
        title = title,
        author = author,
        path = path,
        format = format,
        coverPath = coverPath,
        progress = progress
    )
}
