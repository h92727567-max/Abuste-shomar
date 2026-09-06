package com.abusteh.shomar.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.abusteh.shomar.data.entity.RoundEntity

@Dao
interface RoundDao {

    @Insert
    suspend fun insertRound(round: RoundEntity): Long

    @Delete
    suspend fun deleteRound(round: RoundEntity)

    /** همه دورهای یک بازی، به ترتیب شماره دور (برای صفحه تاریخچه). */
    @Query("SELECT * FROM rounds WHERE gameId = :gameId ORDER BY roundNumber ASC")
    suspend fun getRoundsForGame(gameId: Long): List<RoundEntity>

    @Query("SELECT * FROM rounds WHERE gameId = :gameId ORDER BY roundNumber DESC LIMIT 1")
    suspend fun getLastRoundForGame(gameId: Long): RoundEntity?

    @Query("DELETE FROM rounds WHERE gameId = :gameId")
    suspend fun deleteRoundsForGame(gameId: Long)
}
