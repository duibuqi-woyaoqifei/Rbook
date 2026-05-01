package com.rbook.domain.model

enum class ReaderTheme {
    WHITE, DARK, SEPIA, GREEN, PAPER
}

data class ReaderSettings(
    val fontSize: Int = 18,
    val theme: ReaderTheme = ReaderTheme.WHITE,
    val brightness: Float = 1.0f
)
