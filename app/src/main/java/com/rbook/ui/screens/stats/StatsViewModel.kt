package com.rbook.ui.screens.stats

import androidx.lifecycle.ViewModel
import com.rbook.domain.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: StatsRepository
) : ViewModel() {
    val allStats = repository.getAllStats()
}
