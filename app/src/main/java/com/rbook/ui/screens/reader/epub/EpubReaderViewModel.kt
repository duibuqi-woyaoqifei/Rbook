package com.rbook.ui.screens.reader.epub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rbook.data.readium.ReadiumManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.readium.r2.shared.publication.Publication
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EpubReaderViewModel @Inject constructor(
    private val readiumManager: ReadiumManager
) : ViewModel() {
    private val _publication = MutableStateFlow<Publication?>(null)
    val publication = _publication.asStateFlow()

    fun loadPublication(file: File) {
        viewModelScope.launch {
            _publication.value = readiumManager.openPublication(file)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        _publication.value?.close()
    }
}
