package com.abusteh.shomar.domain.rules

/**
 * قوانین بازی «ابوسطه».
 *
 * این بخش کاملاً مستقل از UI و Android است تا بتوان آن را جدا تست کرد
 * و در آینده بدون تغییر رابط کاربری، قوانین را اصلاح کرد.
 *
 * قوانین (تأییدشده توسط کاربر):
 * - مقدار خواندن (bid) بین [MIN_BID] تا [MAX_BID] است.
 * - اگر گروه خواننده موفق شود:
 *     - بدون کوت: امتیازش برابر مقدار خوانده‌شده به خودش اضافه می‌شود.
 *     - با کوت: امتیازش برابر دو برابر مقدار خوانده‌شده به خودش اضافه می‌شود.
 * - اگر گروه خواننده ناموفق شود (چه کوت چه عادی؛ کوت روی حالت ناموفق تأثیری ندارد):
 *     - دو برابر مقدار خوانده‌شده به گروه حریف اضافه می‌شود.
 * - رسیدن هر گروه به [WINNING_SCORE] یا بیشتر، پایان بازی و برنده شدن آن گروه است.
 */
object AbusteRules {

    const val MIN_BID = 5
    const val MAX_BID = 9
    const val WINNING_SCORE = 62

    /** بررسی معتبر بودن مقدار خواندن. */
    fun isValidBid(bid: Int): Boolean = bid in MIN_BID..MAX_BID

    /**
     * محاسبه تغییر امتیاز یک دور، مستقل از اینکه خواننده کدام گروه است.
     *
     * @param bid مقدار خوانده‌شده (باید بین [MIN_BID] و [MAX_BID] باشد)
     * @param successful آیا گروه خواننده موفق به گرفتن مقدار خوانده‌شده شد یا نه
     * @param kot آیا این دور کوت بوده
     * @return [RoundOutcome] شامل تغییر امتیاز گروه خواننده و گروه حریف
     * @throws IllegalArgumentException اگر bid خارج از بازه مجاز باشد
     */
    fun calculateRoundOutcome(bid: Int, successful: Boolean, kot: Boolean): RoundOutcome {
        require(isValidBid(bid)) {
            "مقدار خواندن باید بین $MIN_BID تا $MAX_BID باشد، مقدار دریافتی: $bid"
        }

        return if (successful) {
            val multiplier = if (kot) 2 else 1
            RoundOutcome(
                readerScoreDelta = bid * multiplier,
                opponentScoreDelta = 0
            )
        } else {
            // طبق تأیید کاربر: کوت روی حالت ناموفق اثری ندارد (تفسیر ۲).
            // جریمه همیشه دو برابر مقدار خوانده‌شده و به گروه حریف تعلق می‌گیرد.
            RoundOutcome(
                readerScoreDelta = 0,
                opponentScoreDelta = bid * 2
            )
        }
    }

    /**
     * بررسی برنده بازی بر اساس امتیاز فعلی دو گروه.
     * @return گروه برنده، یا null اگر هنوز هیچ‌کدام به [WINNING_SCORE] نرسیده باشند.
     */
    fun checkWinner(teamOneScore: Int, teamTwoScore: Int): Team? {
        val teamOneWins = teamOneScore >= WINNING_SCORE
        val teamTwoWins = teamTwoScore >= WINNING_SCORE

        return when {
            teamOneWins && teamTwoWins ->
                if (teamOneScore >= teamTwoScore) Team.TEAM_ONE else Team.TEAM_TWO
            teamOneWins -> Team.TEAM_ONE
            teamTwoWins -> Team.TEAM_TWO
            else -> null
        }
    }
}

/** یکی از دو گروه بازی. */
enum class Team {
    TEAM_ONE,
    TEAM_TWO
}

/**
 * نتیجه محاسبه‌شده یک دور: چقدر به امتیاز گروه خواننده و چقدر به امتیاز گروه حریف اضافه شود.
 */
data class RoundOutcome(
    val readerScoreDelta: Int,
    val opponentScoreDelta: Int
)
