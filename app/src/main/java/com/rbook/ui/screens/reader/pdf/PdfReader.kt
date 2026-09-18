package com.rbook.ui.screens.reader.pdf

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.barteksc.pdfviewer.PDFView
import java.io.File

@Composable
fun PdfReader(
    path: String,
    initialPage: Int,
    onClick: () -> Unit,
    onUpdateProgress: (Float, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            PDFView(context, null).apply {
                fromFile(File(path))
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .defaultPage(initialPage)
                    .onTap {
                        onClick()
                        true
                    }
                    .onPageChange { page, pageCount ->
                        onUpdateProgress(page.toFloat() / (pageCount - 1).coerceAtLeast(1), page)
                    }
                    .load()
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
