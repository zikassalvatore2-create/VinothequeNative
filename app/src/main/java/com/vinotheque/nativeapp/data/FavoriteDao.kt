package com.vinotheque.nativeapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites WHERE username = :username")
    fun getFavorites(username: String): Flow<List<Favorite>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE username = :username AND wineReference = :wineRef")
    suspend fun removeFavorite(username: String, wineRef: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE username = :username AND wineReference = :wineRef)")
    fun isFavorite(username: String, wineRef: String): Flow<Boolean>
}
