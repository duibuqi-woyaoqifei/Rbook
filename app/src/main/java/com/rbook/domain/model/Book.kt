package com.rbook.domain.model

data class Book(
    val id: Long,
    val title: String,
    val author: String,
    val path: String,
    val format: String,
    val coverPath: String?,
    val progress: Float,
    val currentPage: Int? = 0
)
