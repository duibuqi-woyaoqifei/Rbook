package com.rbook.ui.screens.bookshelf

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rbook.domain.parser.BookParser
import com.rbook.domain.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookshelfViewModel @Inject constructor(
    private val repository: BookRepository,
    private val bookParser: BookParser
) : ViewModel() {
    val allBooks = repository.getAllBooks()

    fun importBook(uri: Uri) {
        viewModelScope.launch {
            val book = bookParser.parse(uri)
            if (book != null) {
                repository.saveBook(book)
            }
        }
    }
}
