package com.vinotheque.nativeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wines")
data class Wine(
    @PrimaryKey val reference: String,
    val name: String = "",
    val region: String = "",
    val vintage: String = "",
    val grape: String = "",
    val type: String = "Red",
    val dryness: String = "Dry",
    val price: Double = 0.0,
    val rating: Int = 90,
    val aroma: String = "",
    val foodPairing: String = "",
    val peakMaturity: String = "",
    val binLocation: String = "",
    val body: Int = 3,
    val tannin: Int = 3,
    val acidity: Int = 3,
    val sweetness: Int = 1,
    val sold: Int = 0,
    val glassType: String = "",
    val image: String? = null
)
