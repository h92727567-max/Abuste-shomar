package com.abusteh.shomar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abusteh.shomar.data.repository.GameRepository
import com.abusteh.shomar.domain.model.GameSummary
import com.abusteh.shomar.domain.model.Round
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel صفحه «تاریخچه بازی‌ها» (بخش ۱۰ مشخصات).
 *
 * جدا از [GameViewModel] است چون به وضعیت بازی جاری کاری ندارد؛ فقط
 * بازی‌های تمام‌شده را از طریق [GameRepository] می‌خواند.
 *
 * مدیریت خطا (بخش ۲۶ مشخصات): خواندن از Database داخل try/catch است.
 */
class HistoryViewModel(
    private val repository: GameRepository
) : ViewModel() {

    private val _finishedGames = MutableStateFlow<List<GameSummary>>(emptyList())
    val finishedGames: StateFlow<List<GameSummary>> = _finishedGames.asStateFlow()

    private val _selectedGame = MutableStateFlow<GameSummary?>(null)
    val selectedGame: StateFlow<GameSummary?> = _selectedGame.asStateFlow()

    private val _selectedGameRounds = MutableStateFlow<List<Round>>(emptyList())
    val selectedGameRounds: StateFlow<List<Round>> = _selectedGameRounds.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

    fun loadFinishedGames() {
        viewModelScope.launch {
            try {
                _finishedGames.value = repository.getFinishedGames()
            } catch (e: Exception) {
                _errorMessage.value = "خواندن تاریخچه بازی‌ها با مشکل مواجه شد. دوباره تلاش کنید."
            }
        }
    }

    /** انتخاب یک بازی از لیست برای دیدن جزئیات دورهای آن (بخش ۱۰ مشخصات). */
    fun selectGame(gameSummary: GameSummary) {
        _selectedGame.value = gameSummary
        viewModelScope.launch {
            try {
                _selectedGameRounds.value = repository.getRoundsForGame(gameSummary.id)
            } catch (e: Exception) {
                _errorMessage.value = "خواندن جزئیات این بازی با مشکل مواجه شد. دوباره تلاش کنید."
            }
        }
    }

    fun clearSelection() {
        _selectedGame.value = null
        _selectedGameRounds.value = emptyList()
    }
}
