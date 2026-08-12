package com.rbook.ui.screens.reader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.activity.compose.BackHandler
import androidx.hilt.navigation.compose.hiltViewModel
import com.rbook.ui.screens.reader.components.ReaderBottomPanel
import com.rbook.ui.screens.reader.components.ReaderGuideTooltip
import com.rbook.ui.screens.reader.components.ReaderTopBar
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
    val readerSettings by viewModel.readerSettings.collectAsState()
    val isControlsVisible by viewModel.isControlsVisible.collectAsState()
    val handleBack = {
        if (isControlsVisible) viewModel.hideControls() else onBack()
    }

    BackHandler(onBack = handleBack)

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    if (book == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentBook = book!!

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { viewModel.toggleControlsVisibility() }
            )
    ) {
        // 1. 底层阅读内容（不阻挡点击穿透）
        when (currentBook.format.uppercase()) {
            "TXT" -> TxtReader(
                path = currentBook.path,
                settings = readerSettings,
                initialPage = currentBook.currentPage ?: 0,
                onClickCenter = { viewModel.toggleControlsVisibility() },
                onUpdateProgress = { progress, page -> viewModel.updateReadingProgress(progress, page) }
            )
            "PDF" -> PdfReader(
                path = currentBook.path,
                title = currentBook.title,
                initialPage = currentBook.currentPage ?: 0,
                onUpdateProgress = { progress, page -> viewModel.updateReadingProgress(progress, page) },
                onBack = handleBack
            )
            "EPUB" -> EpubReader(
                book = currentBook,
                onBack = handleBack,
                settings = readerSettings,
                onClick = { viewModel.toggleControlsVisibility() },
                onUpdateProgress = { locator, page ->
                    viewModel.updateReadingProgress(
                        progress = (locator.locations.totalProgression ?: 0.0).toFloat(),
                        currentPage = page,
                        epubLocator = locator.toJSON().toString()
                    )
                }
            )
            else -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("不支持的格式: ${currentBook.format}")
            }
        }

        // 2. 顶部 TopBar（从上方滑入）
        ReaderTopBar(
            visible = isControlsVisible,
            title = currentBook.title,
            format = currentBook.format,
            onBack = handleBack,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 3. 底部设置面板（从下方滑入）
        ReaderBottomPanel(
            visible = isControlsVisible,
            format = currentBook.format,
            settings = readerSettings,
            onUpdateSettings = { viewModel.updateSettings(it) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // 首次进入阅读器引导（仅一次，通过 SharedPreferences 持久化标记）
        ReaderGuideTooltip(
            visible = !readerSettings.hasShownGuide,
            onDismiss = { viewModel.dismissGuide() }
        )
    }
}
