package com.rbook.ui.screens.reader.pdf

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.barteksc.pdfviewer.PDFView
import java.io.File

@Composable
fun PdfReader(
    path: String,
    initialPage: Int,
    seekRequest: Int,
    onClick: () -> Unit,
    onUpdateProgress: (Float, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var pdfView by remember { mutableStateOf<PDFView?>(null) }
    AndroidView(
        factory = { context ->
            PDFView(context, null).apply {
                pdfView = this
                fromFile(File(path))
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .defaultPage(initialPage.coerceAtLeast(0))
                    .onTap {
                        onClick()
                        true
                    }
                    .onPageChange { page, pageCount ->
                        onUpdateProgress(page.toFloat() / (pageCount - 1).coerceAtLeast(1), page.coerceIn(0, (pageCount - 1).coerceAtLeast(0)), pageCount.coerceAtLeast(1))
                    }
                    .load()
            }
        },
        modifier = modifier.fillMaxSize()
    )
    androidx.compose.runtime.LaunchedEffect(seekRequest, pdfView) {
        pdfView?.jumpTo(seekRequest.coerceAtLeast(0), true)
    }
}
