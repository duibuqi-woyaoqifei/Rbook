package com.rbook.ui.screens.reader.txt

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TxtReader(
    path: String,
    title: String,
    onBack: () -> Unit
) {
    var content by remember { mutableStateOf("正在加载...") }
    
    LaunchedEffect(path) {
        val file = File(path)
        if (file.exists()) {
            content = file.readText(Charsets.UTF_8)
        } else {
            content = "找不到文件: $path"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = content,
                fontSize = 18.sp,
                lineHeight = 28.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
