package com.abusteh.shomar.domain.rules

import com.abusteh.shomar.domain.model.Game

/**
 * لایه واسط بین ViewModel و AbusteRules.
 *
 * طبق معماری مشخص‌شده (بخش ۳۵ مشخصات):
 * ViewModel → ScoringEngine → AbusteRules
 *
 * ViewModel هرگز مستقیماً AbusteRules را صدا نمی‌زند و هرگز قوانین را
 * داخل خودش پیاده‌سازی نمی‌کند؛ این کار وظیفه همین کلاس است.
 */
object ScoringEngine {

    /**
     * یک دور را روی وضعیت فعلی بازی اعمال می‌کند.
     *
     * علاوه بر بازی جدید (immutable)، [RoundOutcome] خام محاسبه‌شده هم برگردانده
     * می‌شود تا لایه‌های بالاتر (مثل ثبت تاریخچه در Phase 5) بدون فراخوانی مجدد
     * AbusteRules، دقیقاً همان مقدار امتیاز/جریمه اعمال‌شده را داشته باشند.
     * شماره دور فعلی بازی هم یک واحد افزایش می‌یابد.
     */
    fun applyRound(
        game: Game,
        readerTeam: Team,
        bid: Int,
        successful: Boolean,
        kot: Boolean
    ): RoundApplicationResult {
        val outcome = AbusteRules.calculateRoundOutcome(bid, successful, kot)

        val (teamOneDelta, teamTwoDelta) = when (readerTeam) {
            Team.TEAM_ONE -> outcome.readerScoreDelta to outcome.opponentScoreDelta
            Team.TEAM_TWO -> outcome.opponentScoreDelta to outcome.readerScoreDelta
        }

        val updatedGame = game.copy(
            teamOneScore = game.teamOneScore + teamOneDelta,
            teamTwoScore = game.teamTwoScore + teamTwoDelta,
            currentRoundNumber = game.currentRoundNumber + 1
        )

        return RoundApplicationResult(updatedGame = updatedGame, outcome = outcome)
    }

    /** بررسی برنده بازی بر اساس وضعیت فعلی. */
    fun winner(game: Game): Team? =
        AbusteRules.checkWinner(game.teamOneScore, game.teamTwoScore)
}

/** نتیجه اعمال یک دور: بازی جدید به‌علاوه جزئیات خام امتیاز/جریمه محاسبه‌شده. */
data class RoundApplicationResult(
    val updatedGame: Game,
    val outcome: RoundOutcome
)
