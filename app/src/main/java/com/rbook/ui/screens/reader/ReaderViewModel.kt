package com.rbook.ui.screens.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rbook.domain.model.Book
import com.rbook.domain.repository.BookRepository
import com.rbook.domain.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val repository: BookRepository,
    private val statsRepository: StatsRepository
) : ViewModel() {
    private val _book = MutableStateFlow<Book?>(null)
    val book = _book.asStateFlow()

    fun loadBook(id: Long) {
        viewModelScope.launch {
            _book.value = repository.getBookById(id)
            statsRepository.recordBookOpened()
        }
    }
}
