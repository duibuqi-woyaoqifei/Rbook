package com.rbook.ui.screens.bookshelf

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rbook.domain.model.Book
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookshelfScreen(
    viewModel: BookshelfViewModel = hiltViewModel(),
    onNavigateToReader: (Long) -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val books by viewModel.allBooks.collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()

    var selectionMode by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.importMessage.collect { message -> snackbarHostState.showSnackbar(message) }
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importBook(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (selectionMode) {
                TopAppBar(
                    title = { Text("已选 ${selectedIds.size} 本") },
                    navigationIcon = {
                        IconButton(onClick = {
                            selectionMode = false
                            viewModel.clearSelection()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "取消选择")
                        }
                    },
                    actions = {
                        TextButton(onClick = { viewModel.selectAll(books.map { it.id }) }) {
                            Text("全选")
                        }
                        IconButton(
                            onClick = { if (selectedIds.isNotEmpty()) showDeleteConfirm = true },
                            enabled = selectedIds.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "删除")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("RBook", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = onNavigateToStats) {
                            Icon(Icons.Default.BarChart, contentDescription = "统计")
                        }
                        IconButton(onClick = onNavigateToAbout) {
                            Icon(Icons.Default.Info, contentDescription = "关于")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                launcher.launch(arrayOf("application/epub+zip", "application/pdf", "text/plain"))
            }) {
                Icon(Icons.Default.Add, contentDescription = "导入")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (!selectionMode) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::setSearchQuery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    placeholder = { Text("搜索书名或作者") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "清空")
                            }
                        }
                    },
                    singleLine = true
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = sortOption == SortOption.LAST_READ,
                            onClick = { viewModel.setSortOption(SortOption.LAST_READ) },
                            label = { Text("最近阅读") }
                        )
                        FilterChip(
                            selected = sortOption == SortOption.TITLE,
                            onClick = { viewModel.setSortOption(SortOption.TITLE) },
                            label = { Text("书名") }
                        )
                        FilterChip(
                            selected = sortOption == SortOption.AUTHOR,
                            onClick = { viewModel.setSortOption(SortOption.AUTHOR) },
                            label = { Text("作者") }
                        )
                    }
                    TextButton(onClick = { selectionMode = true }) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("管理")
                    }
                }
            }

            if (books.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "没有找到匹配的书籍" else "书架空空如也，点 + 号开始导入",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(books) { book ->
                        BookCard(
                            book = book,
                            selectionMode = selectionMode,
                            selected = book.id in selectedIds,
                            onClick = {
                                if (selectionMode) viewModel.toggleSelect(book.id)
                                else onNavigateToReader(book.id)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除书籍") },
            text = { Text("确定要删除选中的 ${selectedIds.size} 本书吗？删除后无法恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deleteSelected()
                    selectionMode = false
                }) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookCard(
    book: Book,
    selectionMode: Boolean,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    val hasCover = !book.coverPath.isNullOrEmpty() && File(book.coverPath).exists()

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
    ) {
        Column {
            // 封面渲染逻辑：如果有提取出的封面图片则渲染图片，否则显示格式勋章
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    if (hasCover) {
                        AsyncImage(
                            model = File(book.coverPath!!),
                            contentDescription = book.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = book.format,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                if (selectionMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (selected) Color.Transparent
                                else Color.Black.copy(alpha = 0.45f)
                            ),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Icon(
                            imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = if (selected) "已选择" else "未选择",
                            tint = if (selected) MaterialTheme.colorScheme.primary else Color.White,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
            }
        }
    }
}
