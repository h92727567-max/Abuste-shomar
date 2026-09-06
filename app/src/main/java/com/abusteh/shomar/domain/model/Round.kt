package com.abusteh.shomar.domain.model

import com.abusteh.shomar.domain.rules.Team

/**
 * رکورد یک دور ثبت‌شده از بازی جاری.
 *
 * این مدل مستقل از Database است؛ نگاشت آن به `RoundEntity` (جدول Room)
 * توسط `GameRepository` انجام می‌شود (Phase 7) — این دو مدل عمداً با هم
 * مخلوط نشده‌اند تا لایه دامنه از جزئیات ذخیره‌سازی مستقل بماند.
 */
data class Round(
    val roundNumber: Int,
    val readerTeam: Team,
    val bid: Int,
    val successful: Boolean,
    val kot: Boolean,
    val teamOneScoreBefore: Int,
    val teamOneScoreAfter: Int,
    val teamTwoScoreBefore: Int,
    val teamTwoScoreAfter: Int,
    /** مقدار امتیاز (در صورت موفقیت) یا جریمه (در صورت ناموفقی) که در این دور اعمال شد. */
    val appliedAmount: Int
)
