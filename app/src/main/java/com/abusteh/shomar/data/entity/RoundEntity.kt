package com.abusteh.shomar.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * ردیف جدول «Round» در Room (بخش ۱۷ مشخصات).
 * با حذف بازی مادر (GameEntity)، دورهای متعلق به آن هم به‌صورت خودکار حذف می‌شوند
 * (CASCADE) — لازم برای «حذف تاریخچه» در Phase 11.
 */
@Entity(
    tableName = "rounds",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameId")]
)
data class RoundEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameId: Long,
    val roundNumber: Int,
    /** "TEAM_ONE" یا "TEAM_TWO" (نام enum [com.abusteh.shomar.domain.rules.Team]). */
    val readerTeam: String,
    val bid: Int,
    val successful: Boolean,
    val kot: Boolean,
    val teamOneScoreBefore: Int,
    val teamOneScoreAfter: Int,
    val teamTwoScoreBefore: Int,
    val teamTwoScoreAfter: Int,
    val appliedAmount: Int,
    val recordedAt: Long
)
