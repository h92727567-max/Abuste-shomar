package com.abusteh.shomar.domain.model

/**
 * مدل ساده و مستقل از Database برای وضعیت یک بازی.
 *
 * [id] شناسه ردیف متناظر در Room است (Phase 7)؛ تا وقتی بازی هنوز ذخیره
 * نشده (مثلاً در تست‌های Unit خارج از Android) مقدار آن null است.
 */
data class Game(
    val id: Long? = null,
    val teamOneName: String,
    val teamTwoName: String,
    val teamOneScore: Int = 0,
    val teamTwoScore: Int = 0,
    val currentRoundNumber: Int = 1
)
