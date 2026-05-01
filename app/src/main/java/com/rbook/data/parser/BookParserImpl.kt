package com.rbook.data.parser

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.rbook.domain.model.Book
import com.rbook.domain.parser.BookParser
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class BookParserImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BookParser {

    override suspend fun parse(uri: Uri): Book? {
        val fileName = getFileName(uri) ?: return null
        val format = fileName.substringAfterLast(".").uppercase()
        
        val destinationFile = File(context.filesDir, "books/${System.currentTimeMillis()}_$fileName")
        destinationFile.parentFile?.mkdirs()
        
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destinationFile).use { output ->
                input.copyTo(output)
            }
        }

        return Book(
            id = 0,
            title = fileName.substringBeforeLast("."),
            author = "未知作者",
            path = destinationFile.absolutePath,
            format = format,
            coverPath = null,
            progress = 0f
        )
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name
    }
}
