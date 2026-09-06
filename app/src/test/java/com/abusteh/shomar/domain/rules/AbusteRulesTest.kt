package com.abusteh.shomar.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class AbusteRulesTest {

    // --- isValidBid ---

    @Test
    fun bidsWithinFiveToNineAreValid() {
        for (bid in 5..9) {
            assertEquals(true, AbusteRules.isValidBid(bid))
        }
    }

    @Test
    fun bidsOutsideFiveToNineAreInvalid() {
        assertEquals(false, AbusteRules.isValidBid(4))
        assertEquals(false, AbusteRules.isValidBid(10))
        assertEquals(false, AbusteRules.isValidBid(0))
        assertEquals(false, AbusteRules.isValidBid(-5))
    }

    // --- calculateRoundOutcome: موفق ---

    @Test
    fun successfulRoundAddsScoreToReader() {
        val outcome = AbusteRules.calculateRoundOutcome(bid = 7, successful = true, kot = false)
        assertEquals(7, outcome.readerScoreDelta)
        assertEquals(0, outcome.opponentScoreDelta)
    }

    @Test
    fun kotSuccessDoublesReaderScore() {
        val outcome = AbusteRules.calculateRoundOutcome(bid = 6, successful = true, kot = true)
        assertEquals(12, outcome.readerScoreDelta)
        assertEquals(0, outcome.opponentScoreDelta)
    }

    // --- calculateRoundOutcome: ناموفق ---

    @Test
    fun failedRoundGivesDoublePenaltyToOpponent() {
        val outcome = AbusteRules.calculateRoundOutcome(bid = 8, successful = false, kot = false)
        assertEquals(0, outcome.readerScoreDelta)
        assertEquals(16, outcome.opponentScoreDelta)
    }

    @Test
    fun kotDoesNotChangeFailedRoundPenalty() {
        // طبق تأیید کاربر: کوت روی حالت ناموفق تأثیری ندارد.
        val withKot = AbusteRules.calculateRoundOutcome(bid = 9, successful = false, kot = true)
        val withoutKot = AbusteRules.calculateRoundOutcome(bid = 9, successful = false, kot = false)
        assertEquals(withoutKot, withKot)
        assertEquals(18, withKot.opponentScoreDelta)
    }

    // --- اعتبارسنجی bid ---

    @Test
    fun invalidBidThrowsException() {
        assertThrows(IllegalArgumentException::class.java) {
            AbusteRules.calculateRoundOutcome(bid = 4, successful = true, kot = false)
        }
        assertThrows(IllegalArgumentException::class.java) {
            AbusteRules.calculateRoundOutcome(bid = 10, successful = false, kot = false)
        }
    }

    // --- checkWinner ---

    @Test
    fun noWinnerBelowWinningScore() {
        assertNull(AbusteRules.checkWinner(teamOneScore = 40, teamTwoScore = 55))
    }

    @Test
    fun winnerIsDetectedAt62() {
        assertEquals(Team.TEAM_ONE, AbusteRules.checkWinner(teamOneScore = 62, teamTwoScore = 48))
        assertEquals(Team.TEAM_TWO, AbusteRules.checkWinner(teamOneScore = 30, teamTwoScore = 62))
    }

    @Test
    fun winnerIsDetectedAboveWinningScore() {
        assertEquals(Team.TEAM_TWO, AbusteRules.checkWinner(teamOneScore = 50, teamTwoScore = 70))
    }

    @Test
    fun higherScoreWinsWhenBothCrossWinningScoreSimultaneously() {
        assertEquals(Team.TEAM_ONE, AbusteRules.checkWinner(teamOneScore = 70, teamTwoScore = 62))
    }
}
