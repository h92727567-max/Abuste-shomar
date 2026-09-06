package com.abusteh.shomar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abusteh.shomar.data.repository.GameRepository
import com.abusteh.shomar.domain.model.Game
import com.abusteh.shomar.domain.model.Round
import com.abusteh.shomar.domain.rules.ScoringEngine
import com.abusteh.shomar.domain.rules.Team
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel صفحه بازی.
 *
 * منطق امتیازدهی/جریمه هرگز مستقیماً اینجا نوشته نمی‌شود؛ همیشه از طریق
 * ScoringEngine انجام می‌شود (بخش ۳۵ مشخصات). ذخیره‌سازی دائمی هم مستقیماً
 * با Room انجام نمی‌شود؛ همیشه از طریق [GameRepository] (بخش ۲۱ مشخصات) —
 * این ViewModel از جزئیات Entity/Room خبر ندارد.
 *
 * مدیریت خطا (بخش ۲۶ مشخصات): تمام عملیات Database (شروع بازی، ثبت دور،
 * Undo، ادامه بازی) داخل try/catch اجرا می‌شوند. اگر خطایی رخ دهد، برنامه
 * Crash نمی‌کند؛ فقط یک پیام ساده فارسی در [errorMessage] قرار می‌گیرد تا
 * UI آن را (مثلاً با Snackbar) نشان دهد.
 */
class GameViewModel(
    private val repository: GameRepository
) : ViewModel() {

    private val _game = MutableStateFlow<Game?>(null)
    val game: StateFlow<Game?> = _game.asStateFlow()

    private val _rounds = MutableStateFlow<List<Round>>(emptyList())
    val rounds: StateFlow<List<Round>> = _rounds.asStateFlow()

    private val _hasSavedGame = MutableStateFlow(false)
    /** آیا بازی نیمه‌تمامی در Database هست — برای فعال/غیرفعال کردن «ادامه بازی» در صفحه اصلی. */
    val hasSavedGame: StateFlow<Boolean> = _hasSavedGame.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

    fun startNewGame(teamOneName: String, teamTwoName: String) {
        val safeNameOne = teamOneName.ifBlank { DEFAULT_TEAM_ONE_NAME }
        val safeNameTwo = teamTwoName.ifBlank { DEFAULT_TEAM_TWO_NAME }

        viewModelScope.launch {
            try {
                val newGame = repository.createGame(safeNameOne, safeNameTwo)
                _game.value = newGame
                _rounds.value = emptyList()
            } catch (e: Exception) {
                _errorMessage.value = ERROR_START_GAME
            }
        }
    }

    /**
     * ثبت یک دور روی بازی جاری با استفاده از ScoringEngine، ذخیره دائمی آن
     * از طریق Repository، و افزودن رکورد آن دور به تاریخچه در حافظه
     * (بخش ۱۴ مشخصات).
     */
    fun registerRound(
        readerTeam: Team,
        bid: Int,
        successful: Boolean,
        kot: Boolean
    ) {
        val currentGame = _game.value ?: return
        val gameId = currentGame.id ?: return

        // اگر بازی از قبل تمام شده (برنده مشخص شده)، دیگر نباید دوری ثبت شود.
        if (ScoringEngine.winner(currentGame) != null) return

        val result = ScoringEngine.applyRound(
            game = currentGame,
            readerTeam = readerTeam,
            bid = bid,
            successful = successful,
            kot = kot
        )

        val appliedAmount = if (successful) {
            result.outcome.readerScoreDelta
        } else {
            result.outcome.opponentScoreDelta
        }

        val roundRecord = Round(
            roundNumber = currentGame.currentRoundNumber,
            readerTeam = readerTeam,
            bid = bid,
            successful = successful,
            kot = kot,
            teamOneScoreBefore = currentGame.teamOneScore,
            teamOneScoreAfter = result.updatedGame.teamOneScore,
            teamTwoScoreBefore = currentGame.teamTwoScore,
            teamTwoScoreAfter = result.updatedGame.teamTwoScore,
            appliedAmount = appliedAmount
        )

        val winner = ScoringEngine.winner(result.updatedGame)

        viewModelScope.launch {
            try {
                repository.saveRound(gameId, roundRecord)
                repository.updateGameState(result.updatedGame)

                // پایان بازی (بخش ۱۶ مشخصات): اگر امتیاز یکی از گروه‌ها به ۶۲ یا
                // بیشتر رسیده باشد، بازی در Database به‌عنوان تمام‌شده علامت می‌خورد.
                if (winner != null) {
                    val winnerName = if (winner == Team.TEAM_ONE) {
                        result.updatedGame.teamOneName
                    } else {
                        result.updatedGame.teamTwoName
                    }
                    repository.finishGame(gameId, winnerName)
                }

                _rounds.value = _rounds.value + roundRecord
                _game.value = result.updatedGame
            } catch (e: Exception) {
                _errorMessage.value = ERROR_REGISTER_ROUND
            }
        }
    }

    /**
     * لغو آخرین دور ثبت‌شده (بخش ۱۵ مشخصات): هم از Database حذف می‌شود
     * و هم امتیاز بازی به وضعیت «قبل از آن دور» برمی‌گردد.
     */
    fun undoLastRound() {
        val currentGame = _game.value ?: return
        val gameId = currentGame.id ?: return
        val lastRound = _rounds.value.maxByOrNull { it.roundNumber } ?: return

        val revertedGame = currentGame.copy(
            teamOneScore = lastRound.teamOneScoreBefore,
            teamTwoScore = lastRound.teamTwoScoreBefore,
            currentRoundNumber = lastRound.roundNumber
        )

        viewModelScope.launch {
            try {
                repository.deleteRound(gameId, lastRound.roundNumber)
                repository.updateGameState(revertedGame)
                _game.value = revertedGame
                _rounds.value = _rounds.value.filterNot { it.roundNumber == lastRound.roundNumber }
            } catch (e: Exception) {
                _errorMessage.value = ERROR_UNDO
            }
        }
    }

    /**
     * بررسی وجود بازی نیمه‌تمام در Database (بخش ۸ مشخصات).
     * هر بار صفحه اصلی نمایش داده می‌شود صدا زده می‌شود تا وضعیت دکمه
     * «ادامه بازی» درست باشد.
     */
    fun refreshSavedGameStatus() {
        viewModelScope.launch {
            try {
                _hasSavedGame.value = repository.getInProgressGame() != null
            } catch (e: Exception) {
                // یک خطای ساکت: اگر این بررسی شکست بخورد، فقط دکمه «ادامه بازی»
                // غیرفعال می‌ماند؛ نیازی به نمایش پیام خطا به کاربر در صفحه اصلی نیست.
                _hasSavedGame.value = false
            }
        }
    }

    /**
     * بازیابی دقیق آخرین بازی نیمه‌تمام از Database: هم امتیاز نهایی و هم
     * کل تاریخچه دورهای آن، دقیقاً همان‌طور که قبل از بستن برنامه بود.
     * شماره دور فعلی از روی تعداد دورهای ذخیره‌شده بازسازی می‌شود (این عدد
     * هیچ‌وقت مستقل ذخیره نمی‌شود چون همیشه برابر «تعداد دورها + ۱» است).
     */
    fun continueGame() {
        viewModelScope.launch {
            try {
                val savedGame = repository.getInProgressGame() ?: return@launch
                val gameId = savedGame.id ?: return@launch
                val savedRounds = repository.getRoundsForGame(gameId)
                val nextRoundNumber = (savedRounds.maxOfOrNull { it.roundNumber } ?: 0) + 1

                _game.value = savedGame.copy(currentRoundNumber = nextRoundNumber)
                _rounds.value = savedRounds
            } catch (e: Exception) {
                _errorMessage.value = ERROR_CONTINUE_GAME
            }
        }
    }

    /**
     * تغییر نام گروه‌ها برای بازی جاری، از طریق صفحه تنظیمات (بخش ۱۱ مشخصات).
     * نام خالی نادیده گرفته می‌شود و نام قبلی همان گروه حفظ می‌شود.
     */
    fun renameTeams(teamOneName: String, teamTwoName: String) {
        val currentGame = _game.value ?: return
        val updatedGame = currentGame.copy(
            teamOneName = teamOneName.ifBlank { currentGame.teamOneName },
            teamTwoName = teamTwoName.ifBlank { currentGame.teamTwoName }
        )

        viewModelScope.launch {
            try {
                repository.updateGameState(updatedGame)
                _game.value = updatedGame
            } catch (e: Exception) {
                _errorMessage.value = ERROR_RENAME_TEAMS
            }
        }
    }

    companion object {
        const val DEFAULT_TEAM_ONE_NAME = "گروه ۱"
        const val DEFAULT_TEAM_TWO_NAME = "گروه ۲"

        private const val ERROR_START_GAME = "شروع بازی جدید با مشکل مواجه شد. دوباره تلاش کنید."
        private const val ERROR_REGISTER_ROUND = "ثبت دور با مشکل مواجه شد. دوباره تلاش کنید."
        private const val ERROR_UNDO = "لغو دور با مشکل مواجه شد. دوباره تلاش کنید."
        private const val ERROR_CONTINUE_GAME = "بازیابی بازی با مشکل مواجه شد. دوباره تلاش کنید."
        private const val ERROR_RENAME_TEAMS = "ذخیره نام گروه‌ها با مشکل مواجه شد. دوباره تلاش کنید."
    }
}
