package com.vinotheque.nativeapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vinotheque.nativeapp.data.AppDatabase
import com.vinotheque.nativeapp.data.Wine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WineViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).wineDao()

    val wines: StateFlow<List<Wine>> = dao.getAllWines()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addSampleWine() {
        viewModelScope.launch {
            dao.insertWine(
                Wine(
                    reference = "REF${System.currentTimeMillis()}",
                    name = "Château Margaux",
                    region = "Margaux, Bordeaux",
                    vintage = "2015",
                    grape = "Cabernet Sauvignon",
                    type = "Red",
                    price = 650.0,
                    rating = 98,
                    peakMaturity = "2025-2040",
                    binLocation = "Rack A1",
                    body = 4,
                    tannin = 4,
                    acidity = 3,
                    sweetness = 1
                )
            )
        }
    }
}
