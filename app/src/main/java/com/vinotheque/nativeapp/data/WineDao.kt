package com.vinotheque.nativeapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WineDao {
    @Query("SELECT * FROM wines ORDER BY name ASC")
    fun getAllWines(): Flow<List<Wine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWine(wine: Wine)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(wines: List<Wine>)

    @Update
    suspend fun updateWine(wine: Wine)

    @Query("DELETE FROM wines WHERE reference = :ref")
    suspend fun deleteWine(ref: String)

    @Query("DELETE FROM wines")
    suspend fun deleteAll()
}
