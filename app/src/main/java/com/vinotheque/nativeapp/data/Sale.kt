package com.vinotheque.nativeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val wineReference: String,
    val wineName: String,
    val username: String,
    val timestamp: Long,
    val price: Double,
    val quantity: Int = 1
)
