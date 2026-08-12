package com.rbook.domain.model

enum class ReaderTheme {
    WHITE, DARK, SEPIA, GREEN, PAPER
}

enum class ReadingMode {
    SCROLL, // 垂直滚动阅读
    PAGED   // 左右翻页阅读
}

data class ReaderSettings(
    val fontSize: Int = 18,
    val theme: ReaderTheme = ReaderTheme.WHITE,
    val readingMode: ReadingMode = ReadingMode.SCROLL,
    val hasShownGuide: Boolean = false
)
