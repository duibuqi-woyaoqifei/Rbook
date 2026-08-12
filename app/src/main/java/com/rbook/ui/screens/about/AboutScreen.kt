package com.rbook.ui.screens.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rbook.BuildConfig
import com.rbook.data.repository.UpdateInfo
import com.rbook.data.repository.UpdateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val AUTHOR_EMAIL = "827752284@qq.com"
private const val PROJECT_URL = "https://github.com/duibuqi-woyaoqifei/Rbook"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var checking by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<UpdateResult?>(null) }
    var update by remember { mutableStateOf<UpdateInfo?>(null) }
    val snackbar = remember { SnackbarHostState() }

    fun open(url: String) = context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    LaunchedEffect(checking) {
        if (checking) {
            update = withContext(Dispatchers.IO) { runCatching { UpdateRepository.fetch() }.getOrNull() }
            result = if (update == null) UpdateResult.Failed else if (isNewer(update!!.version, BuildConfig.VERSION_NAME)) UpdateResult.Available else UpdateResult.Latest
            checking = false
        }
    }
    LaunchedEffect(result) {
        when (result) {
            UpdateResult.Latest -> snackbar.showSnackbar("已是最新版本")
            UpdateResult.Failed -> snackbar.showSnackbar("暂时无法检查更新，请稍后重试")
            else -> Unit
        }
    }
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, topBar = {
        TopAppBar(title = { Text("关于 RBook") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") }
        })
    }) { padding ->
        Column(Modifier.padding(padding).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("RBook", style = MaterialTheme.typography.headlineMedium)
            Text("版本 ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyLarge)
            Text("一个专注本地阅读体验的开源阅读器。", style = MaterialTheme.typography.bodyMedium)
            HorizontalDivider()
            ListItem(headlineContent = { Text("作者") }, supportingContent = { Text("duibuqi-woyaoqifei") })
            OutlinedButton(onClick = { open("mailto:$AUTHOR_EMAIL") }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Email, null); Spacer(Modifier.width(8.dp)); Text("联系作者（$AUTHOR_EMAIL）")
            }
            OutlinedButton(onClick = { open(PROJECT_URL) }, modifier = Modifier.fillMaxWidth()) { Text("访问项目主页") }
            Button(onClick = {
                checking = true; result = null
            }, enabled = !checking, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.SystemUpdate, null); Spacer(Modifier.width(8.dp)); Text(if (checking) "正在检查更新…" else "检查更新")
            }
        }
    }
    if (result == UpdateResult.Available && update != null) AlertDialog(
        onDismissRequest = { result = null },
        title = { Text("发现新版本 ${update!!.version}") },
        text = { Text(update!!.notes.ifBlank { "有新的版本可供下载。" }) },
        confirmButton = { TextButton(onClick = { open(update!!.downloadUrl); result = null }) { Text("下载更新") } },
        dismissButton = { TextButton(onClick = { result = null }) { Text("稍后") } }
    )
}

private enum class UpdateResult { Latest, Available, Failed }
private fun isNewer(remote: String, local: String): Boolean {
    fun parts(v: String) = v.trimStart('v').split('.').map { it.toIntOrNull() ?: 0 }
    val r = parts(remote); val l = parts(local)
    for (i in 0 until maxOf(r.size, l.size)) if ((r.getOrElse(i) { 0 }) != (l.getOrElse(i) { 0 })) return r.getOrElse(i) { 0 } > l.getOrElse(i) { 0 }
    return false
}
