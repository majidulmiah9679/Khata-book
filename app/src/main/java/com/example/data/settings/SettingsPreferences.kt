package com.example.data.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("labor_khata_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        val appLanguageName = prefs.getString("app_language", AppLanguage.BENGALI_IN.name) ?: AppLanguage.BENGALI_IN.name
        val themeModeName = prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val colorPaletteName = prefs.getString("color_palette", ColorPalette.TRADITIONAL_KHATA.name) ?: ColorPalette.TRADITIONAL_KHATA.name
        val ledgerLayoutName = prefs.getString("ledger_layout", LedgerLayoutStyle.CLASSIC_GRID.name) ?: LedgerLayoutStyle.CLASSIC_GRID.name
        val rawCurrency = prefs.getString("currency_symbol", "Rs") ?: "Rs"
        val currencySymbol = if (rawCurrency == "৳") "Rs" else rawCurrency
        val standardWorkHours = prefs.getInt("standard_work_hours", 8)
        val cardCornerName = prefs.getString("card_corner", CardCornerStyle.ROUNDED.name) ?: CardCornerStyle.ROUNDED.name
        val showPhoneNumbers = prefs.getBoolean("show_phone_numbers", true)
        val showOtColumn = prefs.getBoolean("show_ot_column", true)
        val highlightDueBalance = prefs.getBoolean("highlight_due_balance", true)
        val soundVibration = prefs.getBoolean("sound_vibration", true)
        val compactDensity = prefs.getBoolean("compact_density", false)

        val appLanguage = runCatching { AppLanguage.valueOf(appLanguageName) }.getOrDefault(AppLanguage.BENGALI_IN)
        val themeMode = runCatching { ThemeMode.valueOf(themeModeName) }.getOrDefault(ThemeMode.SYSTEM)
        val colorPalette = runCatching { ColorPalette.valueOf(colorPaletteName) }.getOrDefault(ColorPalette.TRADITIONAL_KHATA)
        val ledgerLayout = runCatching { LedgerLayoutStyle.valueOf(ledgerLayoutName) }.getOrDefault(LedgerLayoutStyle.CLASSIC_GRID)
        val cardCorner = runCatching { CardCornerStyle.valueOf(cardCornerName) }.getOrDefault(CardCornerStyle.ROUNDED)

        return AppSettings(
            appLanguage = appLanguage,
            themeMode = themeMode,
            colorPalette = colorPalette,
            ledgerLayoutStyle = ledgerLayout,
            currencySymbol = currencySymbol,
            standardWorkHours = standardWorkHours,
            cardCornerStyle = cardCorner,
            showPhoneNumbers = showPhoneNumbers,
            showOtColumn = showOtColumn,
            highlightDueBalance = highlightDueBalance,
            soundVibrationEnabled = soundVibration,
            compactDensity = compactDensity
        )
    }

    fun updateSettings(newSettings: AppSettings) {
        prefs.edit()
            .putString("app_language", newSettings.appLanguage.name)
            .putString("theme_mode", newSettings.themeMode.name)
            .putString("color_palette", newSettings.colorPalette.name)
            .putString("ledger_layout", newSettings.ledgerLayoutStyle.name)
            .putString("currency_symbol", newSettings.currencySymbol)
            .putInt("standard_work_hours", newSettings.standardWorkHours)
            .putString("card_corner", newSettings.cardCornerStyle.name)
            .putBoolean("show_phone_numbers", newSettings.showPhoneNumbers)
            .putBoolean("show_ot_column", newSettings.showOtColumn)
            .putBoolean("highlight_due_balance", newSettings.highlightDueBalance)
            .putBoolean("sound_vibration", newSettings.soundVibrationEnabled)
            .putBoolean("compact_density", newSettings.compactDensity)
            .apply()

        _settings.value = newSettings
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _settings.value = AppSettings()
    }
}
