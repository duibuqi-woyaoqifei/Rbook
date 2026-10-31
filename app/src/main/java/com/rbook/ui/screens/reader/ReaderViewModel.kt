package com.rbook.ui.screens.reader

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rbook.domain.model.Book
import com.rbook.domain.model.ReaderSettings
import com.rbook.domain.model.ReaderTheme
import com.rbook.domain.model.ReadingMode
import com.rbook.domain.repository.BookRepository
import com.rbook.domain.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
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
    private val _totalPages = MutableStateFlow<Int?>(null)
    val totalPages = _totalPages.asStateFlow()
    private val _seekRequest = MutableStateFlow(0)
    val seekRequest = _seekRequest.asStateFlow()
    private val _epubSeekProgress = MutableStateFlow<Float?>(null)
    val epubSeekProgress = _epubSeekProgress.asStateFlow()

    // 读入 SharedPreferences 中的 hasShownGuide 标志，确保仅首次打开时展示引导
    private val _readerSettings = MutableStateFlow(
        ReaderSettings(
            fontSize = prefs.getInt("reader_font_size", 18).coerceIn(12, 32),
            theme = ReaderTheme.values().getOrElse(prefs.getInt("reader_theme", 0)) { ReaderTheme.WHITE },
            readingMode = ReadingMode.values().getOrElse(prefs.getInt("reader_mode", 0)) { ReadingMode.SCROLL },
            hasShownGuide = prefs.getBoolean("has_shown_guide", false),
            keepScreenAwake = prefs.getBoolean("keep_screen_awake", false),
            keepScreenAwakeDurationSeconds = prefs.getInt("keep_screen_awake_seconds", 300).let { if (it in listOf(0, 30, 60, 180, 300, 600)) it else 300 }
        )
    )
    val readerSettings = _readerSettings.asStateFlow()

    // 控制设置 Overlay 显示/隐藏
    private val _isControlsVisible = MutableStateFlow(false)
    val isControlsVisible = _isControlsVisible.asStateFlow()

    fun loadBook(id: Long) {
        viewModelScope.launch {
            _book.value = repository.getBookById(id)
            val loaded = _book.value
            _totalPages.value = if (loaded?.format.equals("TXT", true)) loaded?.path?.let { path ->
                runCatching { java.io.File(path).readText(Charsets.UTF_8).split("\n").count { it.isNotBlank() }.coerceAtLeast(1) }.getOrNull()
            } else null
            _seekRequest.value = loaded?.currentPage ?: 0
            statsRepository.recordBookOpened()
        }
    }

    fun updateReadingProgress(progress: Float, currentPage: Int, epubLocator: String? = null) {
        val currentBook = _book.value ?: return
        val normalizedProgress = progress.coerceIn(0f, 1f)
        val normalizedPage = currentPage.coerceAtLeast(0)
        val resolvedLocator = epubLocator ?: currentBook.epubLocator
        if (currentBook.progress == normalizedProgress && currentBook.currentPage == normalizedPage && currentBook.epubLocator == resolvedLocator) return

        _book.value = currentBook.copy(progress = normalizedProgress, currentPage = normalizedPage, epubLocator = resolvedLocator)
        viewModelScope.launch {
            runCatching { repository.updateReadingProgress(currentBook.id, normalizedProgress, normalizedPage, resolvedLocator) }
        }
    }

    fun updateTotalPages(count: Int) {
        _totalPages.value = count.coerceAtLeast(1)
        val currentBook = _book.value ?: return
        if (currentBook.format.equals("PDF", true) && count > 0) {
            val page = (currentBook.currentPage ?: 0).coerceIn(0, count - 1)
            val progress = page.toFloat() / (count - 1).coerceAtLeast(1)
            if (currentBook.progress != progress || currentBook.currentPage != page) updateReadingProgress(progress, page)
            if (_seekRequest.value != page) _seekRequest.value = page
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
        prefs.edit()
            .putInt("reader_font_size", newSettings.fontSize.coerceIn(12, 32))
            .putInt("reader_theme", newSettings.theme.ordinal)
            .putInt("reader_mode", newSettings.readingMode.ordinal)
            .putBoolean("keep_screen_awake", newSettings.keepScreenAwake)
            .putInt("keep_screen_awake_seconds", newSettings.keepScreenAwakeDurationSeconds)
            .apply()
    }

    fun seekToProgress(progress: Float) {
        val currentBook = _book.value ?: return
        val normalized = progress.coerceIn(0f, 1f)
        if (currentBook.format.equals("EPUB", true)) {
            _epubSeekProgress.value = normalized
            return
        }
        val page = _totalPages.value?.let { ((normalized * (it - 1)).roundToInt()).coerceIn(0, it - 1) } ?: currentBook.currentPage ?: 0
        val resolvedProgress = _totalPages.value?.let { page.toFloat() / (it - 1).coerceAtLeast(1) } ?: normalized
        _seekRequest.value = page
        updateReadingProgress(resolvedProgress, page, currentBook.epubLocator)
    }

    fun seekToPage(pageNumber: Int) {
        val count = _totalPages.value ?: return
        val page = (pageNumber - 1).coerceIn(0, count - 1)
        seekToProgress(page.toFloat() / (count - 1).coerceAtLeast(1))
    }

    fun dismissGuide() {
        // 永久保存到本地磁盘，之后不再触发引导
        prefs.edit().putBoolean("has_shown_guide", true).apply()
        _readerSettings.update { it.copy(hasShownGuide = true) }
    }

}
