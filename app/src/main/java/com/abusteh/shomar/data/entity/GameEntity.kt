package com.abusteh.shomar.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** وضعیت بازی در Database. */
enum class GameStatus {
    IN_PROGRESS,
    FINISHED
}

/**
 * ردیف جدول «Game» در Room (بخش ۱۷ مشخصات).
 * فیلدهای فارسیِ خواسته‌شده در مشخصات (نام گروه اول/دوم، امتیاز، وضعیت،
 * تاریخ شروع/پایان، برنده) اینجا با نام‌های انگلیسی معادل پیاده شده‌اند.
 */
@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val teamOneName: String,
    val teamTwoName: String,
    val teamOneScore: Int,
    val teamTwoScore: Int,
    /** یکی از مقادیر [GameStatus] به‌صورت رشته. */
    val status: String,
    val startDate: Long,
    val endDate: Long?,
    /** نام گروه برنده، یا null اگر بازی هنوز تمام نشده. */
    val winner: String?
)
