package com.rbook.ui.screens.reader

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rbook.domain.model.Book
import com.rbook.domain.model.ReaderSettings
import com.rbook.domain.repository.BookRepository
import com.rbook.domain.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: BookRepository,
    private val statsRepository: StatsRepository
) : ViewModel() {

    private val prefs = context.getSharedPreferences("rbook_prefs", Context.MODE_PRIVATE)

    private val _book = MutableStateFlow<Book?>(null)
    val book = _book.asStateFlow()

    // 读入 SharedPreferences 中的 hasShownGuide 标志，确保仅首次打开时展示引导
    private val _readerSettings = MutableStateFlow(
        ReaderSettings(hasShownGuide = prefs.getBoolean("has_shown_guide", false))
    )
    val readerSettings = _readerSettings.asStateFlow()

    // 控制设置 Overlay 显示/隐藏
    private val _isControlsVisible = MutableStateFlow(false)
    val isControlsVisible = _isControlsVisible.asStateFlow()

    fun loadBook(id: Long) {
        viewModelScope.launch {
            _book.value = repository.getBookById(id)
            statsRepository.recordBookOpened()
        }
    }

    fun updateReadingProgress(progress: Float, currentPage: Int, epubLocator: String? = null) {
        val currentBook = _book.value ?: return
        val normalizedProgress = progress.coerceIn(0f, 1f)
        val normalizedPage = currentPage.coerceAtLeast(0)
        if (currentBook.progress == normalizedProgress && currentBook.currentPage == normalizedPage && currentBook.epubLocator == epubLocator) return

        _book.value = currentBook.copy(progress = normalizedProgress, currentPage = normalizedPage, epubLocator = epubLocator)
        viewModelScope.launch {
            repository.updateReadingProgress(currentBook.id, normalizedProgress, normalizedPage, epubLocator)
        }
    }

    fun toggleControlsVisibility() {
        _isControlsVisible.value = !_isControlsVisible.value
    }

    fun hideControls() {
        _isControlsVisible.value = false
    }

    fun updateSettings(newSettings: ReaderSettings) {
        _readerSettings.value = newSettings
    }

    fun dismissGuide() {
        // 永久保存到本地磁盘，之后不再触发引导
        prefs.edit().putBoolean("has_shown_guide", true).apply()
        _readerSettings.update { it.copy(hasShownGuide = true) }
    }

}
