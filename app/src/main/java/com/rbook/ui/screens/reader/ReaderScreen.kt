package com.rbook.ui.screens.reader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.input.pointer.pointerInput
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
    val seekRequest by viewModel.seekRequest.collectAsState()
    val epubSeekProgress by viewModel.epubSeekProgress.collectAsState()
    val totalPages by viewModel.totalPages.collectAsState()
    val readerSettings by viewModel.readerSettings.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val hostView = LocalView.current
    val wakeState = remember { ReaderWakeState() }
    SideEffect { wakeState.configure(hostView, readerSettings.keepScreenAwake, readerSettings.keepScreenAwakeDurationSeconds, lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    val isControlsVisible by viewModel.isControlsVisible.collectAsState()
    val handleBack = {
        if (isControlsVisible) viewModel.hideControls() else onBack()
    }

    BackHandler(onBack = handleBack)

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    DisposableEffect(lifecycleOwner, readerSettings.keepScreenAwake, readerSettings.keepScreenAwakeDurationSeconds) {
        val observer = LifecycleEventObserver { _, event ->
            wakeState.setActive(event == Lifecycle.Event.ON_RESUME)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            wakeState.release()
        }
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
            .pointerInput(wakeState) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.any { it.pressed }) wakeState.onInteraction()
                    }
                }
            }
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
                seekRequest = seekRequest,
                onUpdateProgress = { progress, page ->
                    viewModel.updateReadingProgress(progress, page)
                }
            )
            "PDF" -> PdfReader(
                path = currentBook.path,
                initialPage = currentBook.currentPage ?: 0,
                onClick = { viewModel.toggleControlsVisibility() },
                seekRequest = seekRequest,
                onUpdateProgress = { progress, page, pageCount ->
                    viewModel.updateReadingProgress(progress, page)
                    viewModel.updateTotalPages(pageCount)
                }
            )
            "EPUB" -> EpubReader(
                book = currentBook,
                onBack = handleBack,
                settings = readerSettings,
                onClick = { viewModel.toggleControlsVisibility() },
                targetProgress = epubSeekProgress,
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
            progress = currentBook.progress,
            currentPage = currentBook.currentPage ?: 0,
            totalPages = totalPages,
            onSeekProgress = { viewModel.seekToProgress(it) },
            onSeekPage = viewModel::seekToPage,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // 首次进入阅读器引导（仅一次，通过 SharedPreferences 持久化标记）
        ReaderGuideTooltip(
            visible = !readerSettings.hasShownGuide,
            onDismiss = { viewModel.dismissGuide() }
        )
    }
}

private class ReaderWakeState {
    private var view: android.view.View? = null
    private var enabled = false
    private var durationSeconds = 300
    private var active = false
    private var lastInteraction = 0L

    @Synchronized
    fun configure(view: android.view.View, enabled: Boolean, durationSeconds: Int, active: Boolean) {
        this.view = view
        this.enabled = enabled
        this.durationSeconds = durationSeconds
        this.active = active
        if (!enabled || !active) release() else if (lastInteraction == 0L) onInteraction()
        scheduleExpiry()
    }

    @Synchronized
    fun setActive(active: Boolean) {
        this.active = active
        if (active) onInteraction() else release()
    }

    @Synchronized
    fun onInteraction() {
        if (!enabled || !active) return
        lastInteraction = android.os.SystemClock.elapsedRealtime()
        view?.keepScreenOn = true
        scheduleExpiry()
    }

    @Synchronized
    fun release() {
        lastInteraction = 0L
        view?.keepScreenOn = false
    }

    private fun scheduleExpiry() {
        val timeout = durationSeconds
        if (!enabled || !active || timeout == 0) return
        val deadline = lastInteraction + timeout * 1_000L
        val remaining = (deadline - android.os.SystemClock.elapsedRealtime()).coerceAtLeast(0L)
        view?.postDelayed({
            synchronized(this) {
                if (enabled && active && lastInteraction > 0 && android.os.SystemClock.elapsedRealtime() >= lastInteraction + durationSeconds * 1_000L) {
                    view?.keepScreenOn = false
                    lastInteraction = 0L
                }
            }
        }, remaining)
    }
}
