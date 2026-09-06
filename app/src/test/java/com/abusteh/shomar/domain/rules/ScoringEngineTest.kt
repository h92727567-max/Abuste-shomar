package com.abusteh.shomar.domain.rules

import com.abusteh.shomar.domain.model.Game
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScoringEngineTest {

    private fun freshGame() = Game(
        teamOneName = "گروه اول",
        teamTwoName = "گروه دوم"
    )

    @Test
    fun successfulRoundForTeamOneAddsToTeamOneOnly() {
        val result = ScoringEngine.applyRound(
            game = freshGame(),
            readerTeam = Team.TEAM_ONE,
            bid = 7,
            successful = true,
            kot = false
        )
        assertEquals(7, result.updatedGame.teamOneScore)
        assertEquals(0, result.updatedGame.teamTwoScore)
        assertEquals(7, result.outcome.readerScoreDelta)
        assertEquals(0, result.outcome.opponentScoreDelta)
    }

    @Test
    fun successfulRoundForTeamTwoAddsToTeamTwoOnly() {
        val result = ScoringEngine.applyRound(
            game = freshGame(),
            readerTeam = Team.TEAM_TWO,
            bid = 9,
            successful = true,
            kot = false
        )
        assertEquals(0, result.updatedGame.teamOneScore)
        assertEquals(9, result.updatedGame.teamTwoScore)
    }

    @Test
    fun failedRoundByTeamOneAddsPenaltyToTeamTwo() {
        val result = ScoringEngine.applyRound(
            game = freshGame(),
            readerTeam = Team.TEAM_ONE,
            bid = 5,
            successful = false,
            kot = false
        )
        assertEquals(0, result.updatedGame.teamOneScore)
        assertEquals(10, result.updatedGame.teamTwoScore)
    }

    @Test
    fun failedRoundByTeamTwoAddsPenaltyToTeamOne() {
        val result = ScoringEngine.applyRound(
            game = freshGame(),
            readerTeam = Team.TEAM_TWO,
            bid = 6,
            successful = false,
            kot = false
        )
        assertEquals(12, result.updatedGame.teamOneScore)
        assertEquals(0, result.updatedGame.teamTwoScore)
    }

    @Test
    fun kotSuccessDoublesScoreForReadingTeam() {
        val result = ScoringEngine.applyRound(
            game = freshGame(),
            readerTeam = Team.TEAM_ONE,
            bid = 8,
            successful = true,
            kot = true
        )
        assertEquals(16, result.updatedGame.teamOneScore)
        assertEquals(0, result.updatedGame.teamTwoScore)
    }

    @Test
    fun roundNumberIncreasesAfterEachRound() {
        val afterOneRound = ScoringEngine.applyRound(
            game = freshGame(),
            readerTeam = Team.TEAM_ONE,
            bid = 5,
            successful = true,
            kot = false
        ).updatedGame
        assertEquals(2, afterOneRound.currentRoundNumber)

        val afterTwoRounds = ScoringEngine.applyRound(
            game = afterOneRound,
            readerTeam = Team.TEAM_TWO,
            bid = 6,
            successful = false,
            kot = false
        ).updatedGame
        assertEquals(3, afterTwoRounds.currentRoundNumber)
    }

    @Test
    fun scoresAccumulateAcrossMultipleRounds() {
        var game = freshGame()
        game = ScoringEngine.applyRound(game, Team.TEAM_ONE, bid = 9, successful = true, kot = false).updatedGame // A: 9
        game = ScoringEngine.applyRound(game, Team.TEAM_TWO, bid = 5, successful = false, kot = false).updatedGame // A: +10 -> 19
        assertEquals(19, game.teamOneScore)
        assertEquals(0, game.teamTwoScore)
    }

    @Test
    fun noWinnerBeforeReachingWinningScore() {
        val game = freshGame().copy(teamOneScore = 40, teamTwoScore = 50)
        assertNull(ScoringEngine.winner(game))
    }

    @Test
    fun winnerIsDetectedAt62() {
        val game = freshGame().copy(teamOneScore = 62, teamTwoScore = 41)
        assertEquals(Team.TEAM_ONE, ScoringEngine.winner(game))
    }
}
