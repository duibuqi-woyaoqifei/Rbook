package com.rbook.domain.parser

import android.net.Uri
import com.rbook.domain.model.Book

interface BookParser {
    suspend fun parse(uri: Uri): Book?
}
