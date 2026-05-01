package com.rbook.ui.screens.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rbook.data.repository.SettingsRepository
import com.rbook.domain.model.Book
import com.rbook.domain.model.Bookmark
import com.rbook.domain.model.ReaderSettings
import com.rbook.domain.model.ReaderTheme
import com.rbook.domain.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val repository: BookRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book

    private var sessionStartTime: Long = 0

    val bookmarks: StateFlow<List<Bookmark>> = _book.flatMapLatest { book ->
        if (book != null) {
            repository.getBookmarksForBook(book.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val settings: StateFlow<ReaderSettings> = settingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReaderSettings()
        )

    fun loadBook(bookId: Long) {
        viewModelScope.launch {
            _book.value = repository.getBookById(bookId)
            sessionStartTime = System.currentTimeMillis()
            repository.incrementBooksOpened(bookId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        saveStats()
    }

    private val _jumpEvent = MutableSharedFlow<Int>()
    val jumpEvent = _jumpEvent.asSharedFlow()

    fun saveStats() {
        if (sessionStartTime > 0) {
            val duration = System.currentTimeMillis() - sessionStartTime
            viewModelScope.launch(kotlinx.coroutines.NonCancellable) {
                repository.addReadingDuration(duration)
            }
            sessionStartTime = System.currentTimeMillis()
        }
    }

    fun triggerJump(bookmark: Bookmark) {
        val page = Regex("\\d+").find(bookmark.position)?.value?.toIntOrNull() ?: return
        viewModelScope.launch {
            _jumpEvent.emit(page)
        }
    }

    fun updateProgress(progress: Float, currentPage: Int) {
        val currentBook = _book.value ?: return
        
        // Update local state so Bookmarks get the correct page
        _book.value = currentBook.copy(progress = progress, currentPage = currentPage)
        
        viewModelScope.launch {
            repository.updateProgress(currentBook.id, progress, currentPage)
        }
    }

    fun updateFontSize(size: Int) {
        viewModelScope.launch {
            settingsRepository.updateFontSize(size)
        }
    }

    fun updateTheme(theme: ReaderTheme) {
        viewModelScope.launch {
            settingsRepository.updateTheme(theme)
        }
    }

    fun addBookmark(position: String, previewText: String?, customTitle: String) {
        val currentBook = _book.value ?: return
        viewModelScope.launch {
            repository.insertBookmark(
                Bookmark(
                    bookId = currentBook.id,
                    title = customTitle,
                    position = position,
                    previewText = previewText
                )
            )
        }
    }

    fun deleteBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            repository.deleteBookmark(bookmark)
        }
    }
}
