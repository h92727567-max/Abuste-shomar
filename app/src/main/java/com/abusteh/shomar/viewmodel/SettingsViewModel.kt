package com.abusteh.shomar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abusteh.shomar.data.repository.GameRepository
import com.abusteh.shomar.data.settings.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel صفحه تنظیمات (بخش ۱۱ مشخصات).
 */
class SettingsViewModel(
    private val settings: AppSettings,
    private val repository: GameRepository
) : ViewModel() {

    private val _roundConfirmationEnabled = MutableStateFlow(settings.isRoundConfirmationEnabled())
    val roundConfirmationEnabled: StateFlow<Boolean> = _roundConfirmationEnabled.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

    fun setRoundConfirmationEnabled(enabled: Boolean) {
        settings.setRoundConfirmationEnabled(enabled)
        _roundConfirmationEnabled.value = enabled
    }

    /**
     * حذف تاریخچه (بخش ۱۱ مشخصات): فقط بازی‌های *تمام‌شده* (و دورهای آن‌ها)
     * حذف می‌شوند. بازی نیمه‌تمامی که کاربر ممکن است الان وسط آن باشد
     * دست‌نخورده می‌ماند — منطقی‌ترین برداشت از «حذف تاریخچه» بدون از بین
     * بردن بازی جاری کاربر.
     */
    fun deleteHistory(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.deleteFinishedGamesHistory()
                onComplete()
            } catch (e: Exception) {
                _errorMessage.value = "حذف تاریخچه با مشکل مواجه شد. دوباره تلاش کنید."
            }
        }
    }
}
