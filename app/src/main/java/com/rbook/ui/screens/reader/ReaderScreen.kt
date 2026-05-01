package com.rbook.ui.screens.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rbook.domain.model.BookFormat

import com.rbook.ui.screens.reader.epub.EpubReader
import com.rbook.ui.screens.reader.pdf.PdfReader
import com.rbook.ui.screens.reader.txt.TxtReader
import com.rbook.ui.screens.reader.cbz.CbzReader
import com.rbook.ui.screens.reader.fb2.Fb2Reader
import com.rbook.ui.screens.reader.settings.SettingsBottomSheet

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@Composable
fun ReaderScreen(
    bookId: Long,
    onBack: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val book by viewModel.book.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    
    var showSettings by remember { mutableStateOf(false) }
    var showBookmarks by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(false) }
    
    var showAddBookmarkDialog by remember { mutableStateOf(false) }
    var newBookmarkName by remember { mutableStateOf("") }

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val currentBook = book
        if (currentBook == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Formatting specific readers
                Box(modifier = Modifier.weight(1f)) {
                    val toggleControls = { showControls = !showControls }
                    when (currentBook.format) {
                        BookFormat.EPUB -> EpubReader(currentBook, onBack, settings, toggleControls) { prog, page -> viewModel.updateProgress(prog, page) }
                        BookFormat.PDF -> PdfReader(currentBook, onBack, viewModel.jumpEvent, toggleControls) { prog, page -> viewModel.updateProgress(prog, page) }
                        BookFormat.TXT -> TxtReader(currentBook, onBack, settings, viewModel.jumpEvent, toggleControls) { prog, page -> viewModel.updateProgress(prog, page) }
                        BookFormat.CBZ, BookFormat.CBR -> CbzReader(currentBook, onBack) { prog, page -> viewModel.updateProgress(prog, page) }
                        BookFormat.FB2 -> Fb2Reader(currentBook, onBack, settings) { prog, page -> viewModel.updateProgress(prog, page) }
                        else -> {
                            Text(
                                text = "不支持的格式",
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        DisposableEffect(bookId) {
            onDispose {
                viewModel.saveStats()
            }
        }

        // Overlay Controls
        androidx.compose.animation.AnimatedVisibility(
            visible = showControls,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            }
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = showControls,
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Add Bookmark
                SmallFloatingActionButton(
                    onClick = { 
                        newBookmarkName = "第 ${book?.currentPage ?: 1} 页的书签"
                        showAddBookmarkDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = "添加书签")
                }

                // View Bookmarks
                SmallFloatingActionButton(
                    onClick = { showBookmarks = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                ) {
                    Icon(Icons.Default.Bookmark, contentDescription = "书签")
                }

                val context = androidx.compose.ui.platform.LocalContext.current
                // Settings
                SmallFloatingActionButton(
                    onClick = { 
                        if (book?.format == BookFormat.PDF) {
                            android.widget.Toast.makeText(context, "PDF文件不支持更改字体大小和背景色彩", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            showSettings = true 
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "设置")
                }
            }
        }

        // Bookmark List Modal
        if (showBookmarks) {
            AlertDialog(
                onDismissRequest = { showBookmarks = false },
                title = { Text("书签") },
                text = {
                    if (bookmarks.isEmpty()) {
                        Text("暂无书签。")
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                            items(bookmarks) { bookmark ->
                                ListItem(
                                    headlineContent = { 
                                        Text(
                                            text = bookmark.title,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        ) 
                                    },
                                    supportingContent = { Text(bookmark.position) },
                                    trailingContent = {
                                        IconButton(onClick = { viewModel.deleteBookmark(bookmark) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "删除")
                                        }
                                    },
                                    modifier = Modifier.clickable { 
                                        viewModel.triggerJump(bookmark)
                                        showBookmarks = false
                                    }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showBookmarks = false }) {
                        Text("关闭")
                    }
                }
            )
        }

        if (showAddBookmarkDialog) {
            AlertDialog(
                onDismissRequest = { showAddBookmarkDialog = false },
                title = { Text("添加书签") },
                text = {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    OutlinedTextField(
                        value = newBookmarkName,
                        onValueChange = { newBookmarkName = it },
                        label = { Text("书签名称") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    TextButton(onClick = { 
                        val isDuplicate = bookmarks.any { it.title == newBookmarkName }
                        if (isDuplicate) {
                            android.widget.Toast.makeText(context, "书签名称重复，请重新命名", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            val pos = "第 ${book?.currentPage ?: 1} 页"
                            viewModel.addBookmark(pos, "当前页面", newBookmarkName)
                            android.widget.Toast.makeText(context, "添加书签成功", android.widget.Toast.LENGTH_SHORT).show()
                            showAddBookmarkDialog = false
                        }
                    }) {
                        Text("保存")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddBookmarkDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }

        if (showSettings) {
            SettingsBottomSheet(
                settings = settings,
                onFontSizeChange = { viewModel.updateFontSize(it) },
                onThemeChange = { viewModel.updateTheme(it) },
                onDismiss = { showSettings = false }
            )
        }
    }
}

