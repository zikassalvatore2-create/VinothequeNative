package com.vinotheque.nativeapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale)

    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM sales WHERE username = :username AND timestamp >= :startOfDay")
    suspend fun getTodayTotal(username: String, startOfDay: Long): Int

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM sales WHERE username = :username AND timestamp >= :startOfWeek")
    suspend fun getWeekTotal(username: String, startOfWeek: Long): Int

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM sales WHERE username = :username AND timestamp >= :startOfMonth")
    suspend fun getMonthTotal(username: String, startOfMonth: Long): Int

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM sales WHERE username = :username AND timestamp >= :startOfYear")
    suspend fun getYearTotal(username: String, startOfYear: Long): Int

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM sales WHERE username = :username")
    suspend fun getAllTimeTotal(username: String): Int

    @Query("SELECT COALESCE(SUM(price * quantity), 0.0) FROM sales WHERE username = :username")
    suspend fun getAllTimeRevenue(username: String): Double

    @Query("SELECT COALESCE(SUM(price * quantity), 0.0) FROM sales WHERE username = :username AND timestamp >= :since")
    suspend fun getRevenueSince(username: String, since: Long): Double

    @Query("SELECT wineName, SUM(quantity) as totalQty FROM sales WHERE username = :username GROUP BY wineReference ORDER BY totalQty DESC LIMIT :limit")
    suspend fun getTopWines(username: String, limit: Int): List<TopWineResult>

    @Query("SELECT * FROM sales WHERE username = :username ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentSales(username: String, limit: Int): List<Sale>

    @Query("SELECT * FROM sales ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentSalesAll(limit: Int): List<Sale>

    @Query("DELETE FROM sales WHERE id = :saleId")
    suspend fun deleteSale(saleId: Long)

    @Query("DELETE FROM sales")
    suspend fun clearAllSales()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sales: List<Sale>)

    @Query("SELECT * FROM sales WHERE timestamp >= :since AND timestamp < :until ORDER BY timestamp DESC")
    suspend fun getSalesInRange(since: Long, until: Long): List<Sale>
}

data class TopWineResult(val wineName: String, val totalQty: Int)
