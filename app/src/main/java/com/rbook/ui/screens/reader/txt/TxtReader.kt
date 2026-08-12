package com.rbook.ui.screens.reader.txt

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rbook.domain.model.ReaderSettings
import com.rbook.domain.model.ReaderTheme
import com.rbook.domain.model.ReadingMode
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TxtReader(
    path: String,
    settings: ReaderSettings,
    initialPage: Int,
    onClickCenter: () -> Unit,
    onUpdateProgress: (Float, Int) -> Unit
) {
    var rawContent by remember(path) { mutableStateOf<String?>(null) }
    val paragraphs = remember(rawContent) {
        rawContent?.split("\n")?.map { it.trim() }?.filter { it.isNotBlank() }.orEmpty()
    }

    val backgroundColor = when (settings.theme) {
        ReaderTheme.WHITE -> Color.White
        ReaderTheme.DARK -> Color(0xFF1A1A1A)
        ReaderTheme.SEPIA -> Color(0xFFE8D1A7)
        ReaderTheme.GREEN -> Color(0xFFC7EDCC)
        ReaderTheme.PAPER -> Color(0xFFF8F1E3)
    }

    val textColor = when (settings.theme) {
        ReaderTheme.DARK -> Color(0xFFE0E0E0)
        ReaderTheme.SEPIA -> Color(0xFF3B2F2F)
        else -> Color(0xFF212121)
    }

    LaunchedEffect(path) {
        val file = File(path)
        if (file.exists()) {
            rawContent = file.readText(Charsets.UTF_8)
        } else {
            rawContent = "找不到文件: $path"
        }
    }

    Surface(
        color = backgroundColor,
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClickCenter
            )
    ) {
        SelectionContainer {
            if (rawContent == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@SelectionContainer
            }

            if (settings.readingMode == ReadingMode.SCROLL) {
                // 1. 垂直滚动阅读模式 (LazyColumn)
                val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialPage.coerceAtLeast(0))

                LaunchedEffect(listState, paragraphs.size) {
                    snapshotFlow { listState.firstVisibleItemIndex }
                        .collect { currentPage ->
                            onUpdateProgress(
                                currentPage.toFloat() / paragraphs.lastIndex.coerceAtLeast(1),
                                currentPage
                            )
                        }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(top = 24.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(paragraphs) { index, originalText ->
                        ParagraphItem(
                            originalText = originalText,
                            fontSize = settings.fontSize,
                            textColor = textColor
                        )
                    }
                }
            } else {
                // 2. 左右翻页阅读模式 (HorizontalPager)
                if (paragraphs.isNotEmpty()) {
                    val pagerState = rememberPagerState(
                        initialPage = initialPage.coerceIn(0, paragraphs.lastIndex),
                        pageCount = { paragraphs.size }
                    )

                    LaunchedEffect(pagerState.currentPage) {
                        val currentPage = pagerState.currentPage
                        if (currentPage in paragraphs.indices) {
                            onUpdateProgress(currentPage.toFloat() / paragraphs.lastIndex.coerceAtLeast(1), currentPage)
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            ParagraphItem(
                                originalText = paragraphs[page],
                                fontSize = settings.fontSize,
                                textColor = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParagraphItem(
    originalText: String,
    fontSize: Int,
    textColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = originalText,
            fontSize = fontSize.sp,
            lineHeight = (fontSize * 1.55).sp,
            color = textColor
        )
    }
}
