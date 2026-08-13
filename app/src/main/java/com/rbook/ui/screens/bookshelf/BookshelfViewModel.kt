package com.rbook.ui.screens.bookshelf

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rbook.domain.parser.BookParser
import com.rbook.domain.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class SortOption { LAST_READ, TITLE, AUTHOR }

@HiltViewModel
class BookshelfViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: BookRepository,
    private val bookParser: BookParser
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.LAST_READ)
    val sortOption = _sortOption.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds = _selectedIds.asStateFlow()

    val allBooks = combine(
        repository.getAllBooks(),
        _searchQuery,
        _sortOption
    ) { books, query, sort ->
        val filtered = if (query.isBlank()) books else books.filter {
            it.title.contains(query, ignoreCase = true) || it.author.contains(query, ignoreCase = true)
        }
        when (sort) {
            SortOption.TITLE -> filtered.sortedBy { it.title.lowercase() }
            SortOption.AUTHOR -> filtered.sortedBy { it.author.lowercase() }
            SortOption.LAST_READ -> filtered
        }
    }

    private val _importMessage = MutableSharedFlow<String>()
    val importMessage = _importMessage.asSharedFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun toggleSelect(id: Long) {
        _selectedIds.value = _selectedIds.value.let { if (id in it) it - id else it + id }
    }

    fun selectAll(visibleIds: List<Long>) {
        _selectedIds.value = visibleIds.toSet()
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun deleteSelected() {
        val ids = _selectedIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            ids.forEach { id ->
                val book = repository.getBookById(id)
                if (book != null) {
                    runCatching { File(book.path).delete() }
                    book.coverPath?.let { runCatching { File(it).delete() } }
                }
            }
            repository.deleteBooks(ids)
            _selectedIds.value = emptySet()
            _importMessage.emit("已删除 ${ids.size} 本书")
        }
    }

    fun importBook(uri: Uri) {
        viewModelScope.launch {
            val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && index >= 0) cursor.getString(index) else null
            } ?: uri.lastPathSegment
            val extension = fileName?.substringAfterLast('.', missingDelimiterValue = "")?.lowercase()
            if (extension !in SUPPORTED_EXTENSIONS) {
                _importMessage.emit("格式不支持，仅支持 EPUB、PDF、TXT")
                return@launch
            }

            val book = bookParser.parse(uri)
            if (book != null) {
                repository.saveBook(book)
            } else {
                _importMessage.emit("导入失败，请检查文件是否有效")
            }
        }
    }

    private companion object {
        val SUPPORTED_EXTENSIONS = setOf("epub", "pdf", "txt")
    }
}
