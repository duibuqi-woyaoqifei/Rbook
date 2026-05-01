package com.rbook.ui.screens.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.rbook.domain.model.ReaderSettings
import com.rbook.ui.screens.reader.epub.EpubReader
import com.rbook.ui.screens.reader.pdf.PdfReader
import com.rbook.ui.screens.reader.txt.TxtReader

@Composable
fun ReaderScreen(
    bookId: Long,
    viewModel: ReaderViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val book by viewModel.book.collectAsState()
    val settings = ReaderSettings() // TODO: Load from DataStore

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    if (book == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        when (book?.format?.uppercase()) {
            "TXT" -> TxtReader(path = book!!.path, title = book!!.title, onBack = onBack)
            "PDF" -> PdfReader(path = book!!.path, title = book!!.title, onBack = onBack)
            "EPUB" -> EpubReader(
                book = book!!,
                onBack = onBack,
                settings = settings,
                onClick = { /* Toggle Bars */ },
                onUpdateProgress = { progress, page ->
                    // viewModel.updateProgress(progress, page)
                }
            )
            else -> Text("不支持的格式: ${book?.format}")
        }
    }
}
