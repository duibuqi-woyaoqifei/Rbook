package com.rbook.ui.screens.bookshelf

import android.net.Uri
import android.content.Context
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rbook.domain.parser.BookParser
import com.rbook.domain.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookshelfViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: BookRepository,
    private val bookParser: BookParser
) : ViewModel() {
    val allBooks = repository.getAllBooks()
    private val _importMessage = MutableSharedFlow<String>()
    val importMessage = _importMessage.asSharedFlow()

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
