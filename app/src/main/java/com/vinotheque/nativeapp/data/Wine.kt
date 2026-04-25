package com.vinotheque.nativeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wines")
data class Wine(
    @PrimaryKey val reference: String,
    val name: String,
    val region: String,
    val vintage: String,
    val grape: String,
    val type: String,
    val price: Double,
    val rating: Int,
    val peakMaturity: String,
    val binLocation: String,
    val body: Int,
    val tannin: Int,
    val acidity: Int,
    val sweetness: Int
)
