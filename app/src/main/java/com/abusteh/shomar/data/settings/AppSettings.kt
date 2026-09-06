package com.abusteh.shomar.data.settings

import android.content.Context

/**
 * تنظیمات ساده برنامه (بخش ۱۱ مشخصات).
 *
 * این تنظیمات (مثل روشن/خاموش بودن تأیید ثبت دور) ماهیتاً با داده بازی
 * فرق دارد، پس عمداً در Room ذخیره نمی‌شود؛ SharedPreferences برای این
 * نوع تنظیمات ساده و سریع، انتخاب مناسب‌تری است.
 */
class AppSettings(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** آیا قبل از ثبت نهایی هر دور، Dialog تأیید نشان داده شود (بخش ۱۱ مشخصات). */
    fun isRoundConfirmationEnabled(): Boolean =
        prefs.getBoolean(KEY_ROUND_CONFIRMATION_ENABLED, true)

    fun setRoundConfirmationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ROUND_CONFIRMATION_ENABLED, enabled).apply()
    }

    companion object {
        private const val PREFS_NAME = "abusteh_shomar_settings"
        private const val KEY_ROUND_CONFIRMATION_ENABLED = "round_confirmation_enabled"
    }
}
