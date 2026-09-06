package com.abusteh.shomar.domain.model

/**
 * خلاصه یک بازی تمام‌شده — فقط برای نمایش در صفحه «تاریخچه بازی‌ها» (بخش ۱۰ مشخصات).
 *
 * برخلاف [Game] (که وضعیت زنده یک بازی در حال انجام است)، این مدل فقط
 * خواندنی است و مستقیماً از رکورد تمام‌شده در Database ساخته می‌شود.
 */
data class GameSummary(
    val id: Long,
    val teamOneName: String,
    val teamTwoName: String,
    val teamOneScore: Int,
    val teamTwoScore: Int,
    val winnerName: String,
    val startDate: Long,
    val endDate: Long?
)
