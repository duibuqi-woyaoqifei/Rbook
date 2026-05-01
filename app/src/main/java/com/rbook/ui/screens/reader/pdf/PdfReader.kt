package com.rbook.ui.screens.reader.pdf

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.rbook.domain.model.Book
import com.github.barteksc.pdfviewer.PDFView
import java.io.File

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun PdfReader(
    book: Book,
    onBack: () -> Unit,
    jumpEvent: SharedFlow<Int>,
    onClick: () -> Unit = {},
    onProgressUpdate: (Float, Int) -> Unit
) {
    var pdfViewRef by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<PDFView?>(null) }
    
    LaunchedEffect(Unit) {
        jumpEvent.collect { page ->
            pdfViewRef?.jumpTo(page)
        }
    }

    AndroidView(
        factory = { context ->
            PDFView(context, null).apply {
                pdfViewRef = this
                fromFile(File(book.path))
                    .defaultPage(book.currentPage ?: 0)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .enableAnnotationRendering(true)
                    .onPageChange { page, pageCount ->
                        onProgressUpdate(page.toFloat() / pageCount, page)
                    }
                    .onTap { 
                        onClick()
                        true
                    }
                    .load()
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
