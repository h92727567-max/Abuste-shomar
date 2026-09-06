package com.abusteh.shomar.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.abusteh.shomar.data.entity.GameEntity

@Dao
interface GameDao {

    @Insert
    suspend fun insertGame(game: GameEntity): Long

    @Update
    suspend fun updateGame(game: GameEntity)

    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun getGameById(gameId: Long): GameEntity?

    /** جدیدترین بازی با وضعیت مشخص (مثلاً برای پیدا کردن بازی نیمه‌تمام در Phase 8). */
    @Query("SELECT * FROM games WHERE status = :status ORDER BY startDate DESC LIMIT 1")
    suspend fun getLatestGameByStatus(status: String): GameEntity?

    /** لیست بازی‌های با وضعیت مشخص، جدیدترین اول (برای تاریخچه بازی‌ها در Phase 10). */
    @Query("SELECT * FROM games WHERE status = :status ORDER BY startDate DESC")
    suspend fun getGamesByStatus(status: String): List<GameEntity>

    /**
     * حذف بازی‌های یک وضعیت مشخص (برای «حذف تاریخچه» در تنظیمات، Phase 11).
     * با حذف یک بازی، دورهای متعلق به آن هم به‌خاطر ForeignKey(CASCADE) خودکار حذف می‌شوند.
     */
    @Query("DELETE FROM games WHERE status = :status")
    suspend fun deleteGamesByStatus(status: String)
}
