package com.rbook.ui.screens.reader.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import kotlin.math.roundToInt
import com.rbook.domain.model.ReaderSettings
import com.rbook.domain.model.ReaderTheme
import com.rbook.domain.model.ReadingMode

// 各格式可用功能矩阵
private data class FormatCapabilities(
    val canChangeFontSize: Boolean,
    val canChangeTheme: Boolean,
    val canChangeReadingMode: Boolean,
    val supported: String,
    val unsupported: String
)

private fun getCapabilities(format: String): FormatCapabilities = when (format.uppercase()) {
    "TXT" -> FormatCapabilities(
        canChangeFontSize = true,
        canChangeTheme = true,
        canChangeReadingMode = true,
        supported = "字体大小、阅读背景、阅读模式",
        unsupported = "无"
    )
    "EPUB" -> FormatCapabilities(
        canChangeFontSize = false,
        canChangeTheme = false,
        canChangeReadingMode = false,
        supported = "阅读",
        unsupported = "字体大小、阅读背景、阅读模式"
    )
    "PDF" -> FormatCapabilities(
        canChangeFontSize = false,
        canChangeTheme = false,
        canChangeReadingMode = false,
        supported = "阅读",
        unsupported = "字体大小、阅读背景、阅读模式"
    )
    else -> FormatCapabilities(
        canChangeFontSize = false,
        canChangeTheme = false,
        canChangeReadingMode = false,
        supported = "无",
        unsupported = "所有阅读功能"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderTopBar(
    visible: Boolean,
    title: String,
    format: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it }),
        modifier = modifier.fillMaxWidth()
    ) {
        TopAppBar(
            title = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp)
                ) {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = format.uppercase(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderBottomPanel(
    visible: Boolean,
    format: String,
    settings: ReaderSettings,
    onUpdateSettings: (ReaderSettings) -> Unit,
    progress: Float,
    currentPage: Int,
    totalPages: Int?,
    onSeekProgress: (Float) -> Unit,
    onSeekPage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val caps = getCapabilities(format)

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            shadowElevation = 16.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // ── 功能可用性概览卡片 ───────────────────────
                FeatureStatusCard(caps = caps, format = format)

                Spacer(modifier = Modifier.height(12.dp))
                var percentText by remember(progress) { mutableStateOf((progress.coerceIn(0f, 1f) * 100).roundToInt().toString()) }
                var pageText by remember(currentPage) { mutableStateOf((currentPage + 1).toString()) }
                Text("阅读进度", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("第 ${currentPage + 1}${totalPages?.let { " / $it 页" } ?: " 页"}", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.weight(1f))
                    androidx.compose.foundation.text.BasicTextField(
                        value = percentText,
                        onValueChange = { percentText = it.filter(Char::isDigit).take(3) },
                        singleLine = true,
                        modifier = Modifier.width(48.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)
                    )
                    Text("%", style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { percentText.toIntOrNull()?.let { onSeekProgress((it.coerceIn(0, 100) / 100f)) } }) { Text("跳转") }
                }
                if (totalPages != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("页码", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.width(8.dp))
                        androidx.compose.foundation.text.BasicTextField(
                            value = pageText,
                            onValueChange = { pageText = it.filter(Char::isDigit).take(totalPages.toString().length) },
                            singleLine = true,
                            modifier = Modifier.width(64.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)
                        )
                        TextButton(onClick = { pageText.toIntOrNull()?.let(onSeekPage) }) { Text("跳转") }
                    }
                }
                Slider(value = progress.coerceIn(0f, 1f), onValueChange = onSeekProgress, valueRange = 0f..1f)

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("屏幕常亮", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    Switch(checked = settings.keepScreenAwake, onCheckedChange = { onUpdateSettings(settings.copy(keepScreenAwake = it)) })
                }
                if (settings.keepScreenAwake) {
                    Text("保持唤醒时长", style = MaterialTheme.typography.labelMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                    ) {
                        listOf(30, 60, 180, 300, 600, 0).forEach { seconds ->
                            FilterChip(
                                selected = settings.keepScreenAwakeDurationSeconds == seconds,
                                onClick = { onUpdateSettings(settings.copy(keepScreenAwakeDurationSeconds = seconds)) },
                                label = { Text(when (seconds) { 0 -> "始终"; 30 -> "30秒"; 60 -> "1分"; else -> "${seconds / 60}分" }) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── 1. 字体大小 ────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "字体大小",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (caps.canChangeFontSize) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = {
                            if (settings.fontSize > 12)
                                onUpdateSettings(settings.copy(fontSize = settings.fontSize - 2))
                        },
                        enabled = caps.canChangeFontSize && settings.fontSize > 12
                    ) {
                        Text("A-", fontWeight = FontWeight.Bold, fontSize = 14.sp,
                            color = if (caps.canChangeFontSize) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.outline)
                    }
                    Slider(
                        value = settings.fontSize.toFloat(),
                        onValueChange = { onUpdateSettings(settings.copy(fontSize = it.toInt())) },
                        valueRange = 12f..32f,
                        enabled = caps.canChangeFontSize,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            if (settings.fontSize < 32)
                                onUpdateSettings(settings.copy(fontSize = settings.fontSize + 2))
                        },
                        enabled = caps.canChangeFontSize && settings.fontSize < 32
                    ) {
                        Text("A+", fontWeight = FontWeight.Bold, fontSize = 18.sp,
                            color = if (caps.canChangeFontSize) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.outline)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ── 2. 阅读背景主题 ─────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "阅读背景",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (caps.canChangeTheme) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.outline
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ThemeColorOption(Color.White, settings.theme == ReaderTheme.WHITE, caps.canChangeTheme) {
                            onUpdateSettings(settings.copy(theme = ReaderTheme.WHITE))
                        }
                        ThemeColorOption(Color(0xFF1A1A1A), settings.theme == ReaderTheme.DARK, caps.canChangeTheme) {
                            onUpdateSettings(settings.copy(theme = ReaderTheme.DARK))
                        }
                        ThemeColorOption(Color(0xFFE8D1A7), settings.theme == ReaderTheme.SEPIA, caps.canChangeTheme) {
                            onUpdateSettings(settings.copy(theme = ReaderTheme.SEPIA))
                        }
                        ThemeColorOption(Color(0xFFC7EDCC), settings.theme == ReaderTheme.GREEN, caps.canChangeTheme) {
                            onUpdateSettings(settings.copy(theme = ReaderTheme.GREEN))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                // ── 3. 阅读模式 ─────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "阅读模式:",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (caps.canChangeReadingMode) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = settings.readingMode == ReadingMode.SCROLL,
                        onClick = { if (caps.canChangeReadingMode) onUpdateSettings(settings.copy(readingMode = ReadingMode.SCROLL)) },
                        enabled = caps.canChangeReadingMode,
                        label = { Text("垂直滚动") }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    FilterChip(
                        selected = settings.readingMode == ReadingMode.PAGED,
                        onClick = { if (caps.canChangeReadingMode) onUpdateSettings(settings.copy(readingMode = ReadingMode.PAGED)) },
                        enabled = caps.canChangeReadingMode,
                        label = { Text("左右翻页") }
                    )
                }

            }
        }
    }
}

// ── 功能状态总览卡片 ─────────────────────────────────
@Composable
private fun FeatureStatusCard(caps: FormatCapabilities, format: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${format.uppercase()} 格式",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text("支持：${caps.supported}", style = MaterialTheme.typography.labelSmall)
            Text("不支持：${caps.unsupported}", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun ThemeColorOption(
    color: Color,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (enabled) color else color.copy(alpha = 0.3f))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f),
                shape = CircleShape
            )
            .clickable(enabled = enabled, onClick = onClick)
    )
}
