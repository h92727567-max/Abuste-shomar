package com.abusteh.shomar.data.repository

import com.abusteh.shomar.data.database.GameDao
import com.abusteh.shomar.data.database.RoundDao
import com.abusteh.shomar.data.entity.GameEntity
import com.abusteh.shomar.data.entity.GameStatus
import com.abusteh.shomar.data.entity.RoundEntity
import com.abusteh.shomar.domain.model.Game
import com.abusteh.shomar.domain.model.GameSummary
import com.abusteh.shomar.domain.model.Round
import com.abusteh.shomar.domain.rules.Team

/**
 * تنها نقطه دسترسی به Database برای بقیه برنامه (بخش ۲۱ مشخصات).
 *
 * ViewModel هرگز مستقیماً با GameDao/RoundDao کار نمی‌کند و از جزئیات
 * Entity/Room خبر ندارد؛ فقط با مدل‌های دامنه (Game، Round) صحبت می‌کند.
 * نگاشت بین این دو دنیا (Entity ↔ Domain) وظیفه همین کلاس است.
 */
class GameRepository(
    private val gameDao: GameDao,
    private val roundDao: RoundDao
) {

    /** ساخت و ذخیره یک بازی جدید در حال انجام، و برگرداندن مدل دامنه به‌همراه id واقعی. */
    suspend fun createGame(teamOneName: String, teamTwoName: String): Game {
        val entity = GameEntity(
            teamOneName = teamOneName,
            teamTwoName = teamTwoName,
            teamOneScore = 0,
            teamTwoScore = 0,
            status = GameStatus.IN_PROGRESS.name,
            startDate = System.currentTimeMillis(),
            endDate = null,
            winner = null
        )
        val newId = gameDao.insertGame(entity)
        return Game(
            id = newId,
            teamOneName = teamOneName,
            teamTwoName = teamTwoName
        )
    }

    /** به‌روزرسانی نام گروه‌ها و امتیاز یک بازی موجود در Database. */
    suspend fun updateGameState(game: Game) {
        val gameId = game.id ?: return
        val existing = gameDao.getGameById(gameId) ?: return
        gameDao.updateGame(
            existing.copy(
                teamOneName = game.teamOneName,
                teamTwoName = game.teamTwoName,
                teamOneScore = game.teamOneScore,
                teamTwoScore = game.teamTwoScore
            )
        )
    }

    /** ذخیره یک دور تازه ثبت‌شده برای یک بازی مشخص. */
    suspend fun saveRound(gameId: Long, round: Round) {
        roundDao.insertRound(round.toEntity(gameId))
    }

    /** لغو (حذف) یک دور مشخص از یک بازی، بر اساس شماره دور. */
    suspend fun deleteRound(gameId: Long, roundNumber: Int) {
        val target = roundDao.getRoundsForGame(gameId).find { it.roundNumber == roundNumber } ?: return
        roundDao.deleteRound(target)
    }

    /** تمام دورهای یک بازی، به ترتیب شماره دور. */
    suspend fun getRoundsForGame(gameId: Long): List<Round> =
        roundDao.getRoundsForGame(gameId).map { it.toDomain() }

    /** بازی نیمه‌تمام (اگر وجود داشته باشد) — برای «ادامه بازی» در Phase 8. */
    suspend fun getInProgressGame(): Game? {
        val entity = gameDao.getLatestGameByStatus(GameStatus.IN_PROGRESS.name) ?: return null
        return entity.toDomain()
    }

    /**
     * پایان بازی (بخش ۱۶ مشخصات): وضعیت بازی را به FINISHED تغییر می‌دهد،
     * نام گروه برنده و زمان پایان را ثبت می‌کند. امتیازهای موجود (که قبل از
     * این فراخوانی با [updateGameState] به‌روزرسانی شده‌اند) دست‌نخورده می‌مانند.
     */
    suspend fun finishGame(gameId: Long, winnerTeamName: String) {
        val existing = gameDao.getGameById(gameId) ?: return
        gameDao.updateGame(
            existing.copy(
                status = GameStatus.FINISHED.name,
                winner = winnerTeamName,
                endDate = System.currentTimeMillis()
            )
        )
    }

    /**
     * لیست بازی‌های تمام‌شده، جدیدترین اول — برای صفحه «تاریخچه بازی‌ها»
     * (بخش ۱۰ مشخصات).
     */
    suspend fun getFinishedGames(): List<GameSummary> =
        gameDao.getGamesByStatus(GameStatus.FINISHED.name).map { it.toSummary() }

    /**
     * حذف تاریخچه (بخش ۱۱ مشخصات): فقط بازی‌های تمام‌شده (و دورهای آن‌ها،
     * به‌خاطر ForeignKey CASCADE) حذف می‌شوند. بازی نیمه‌تمام فعلی (اگر
     * وجود داشته باشد) دست‌نخورده می‌ماند.
     */
    suspend fun deleteFinishedGamesHistory() {
        gameDao.deleteGamesByStatus(GameStatus.FINISHED.name)
    }
}

private fun Round.toEntity(gameId: Long): RoundEntity = RoundEntity(
    gameId = gameId,
    roundNumber = roundNumber,
    readerTeam = readerTeam.name,
    bid = bid,
    successful = successful,
    kot = kot,
    teamOneScoreBefore = teamOneScoreBefore,
    teamOneScoreAfter = teamOneScoreAfter,
    teamTwoScoreBefore = teamTwoScoreBefore,
    teamTwoScoreAfter = teamTwoScoreAfter,
    appliedAmount = appliedAmount,
    recordedAt = System.currentTimeMillis()
)

private fun RoundEntity.toDomain(): Round = Round(
    roundNumber = roundNumber,
    readerTeam = Team.valueOf(readerTeam),
    bid = bid,
    successful = successful,
    kot = kot,
    teamOneScoreBefore = teamOneScoreBefore,
    teamOneScoreAfter = teamOneScoreAfter,
    teamTwoScoreBefore = teamTwoScoreBefore,
    teamTwoScoreAfter = teamTwoScoreAfter,
    appliedAmount = appliedAmount
)

private fun GameEntity.toDomain(): Game = Game(
    id = id,
    teamOneName = teamOneName,
    teamTwoName = teamTwoName,
    teamOneScore = teamOneScore,
    teamTwoScore = teamTwoScore
)

private fun GameEntity.toSummary(): GameSummary = GameSummary(
    id = id,
    teamOneName = teamOneName,
    teamTwoName = teamTwoName,
    teamOneScore = teamOneScore,
    teamTwoScore = teamTwoScore,
    winnerName = winner ?: "",
    startDate = startDate,
    endDate = endDate
)
