package com.example.data.settings

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val regionalSubtitle: String,
    val flagEmoji: String
) {
    BENGALI_IN(
        code = "bn-IN",
        nativeName = "বাংলা (ভারত)",
        englishName = "Bengali (India)",
        regionalSubtitle = "পশ্চিমবঙ্গ ও ভারতীয় কথ্য বাংলা",
        flagEmoji = "🇮🇳"
    ),
    ENGLISH(
        code = "en",
        nativeName = "English",
        englishName = "English",
        regionalSubtitle = "Standard Global English",
        flagEmoji = "🌐"
    )
}

enum class ThemeMode(val label: String, val description: String) {
    SYSTEM("System Default", "Follows device dark/light theme"),
    LIGHT("Light Mode", "Crisp daytime high-contrast theme"),
    DARK("Dark Mode", "Night-time eye friendly dark theme")
}

enum class ColorPalette(
    val label: String,
    val description: String,
    val primaryColor: Long,
    val secondaryColor: Long,
    val previewHex: String
) {
    TRADITIONAL_KHATA(
        label = "Traditional Khata",
        description = "Classic ledger vermilion red & amber gold",
        primaryColor = 0xFF8B1E1E,
        secondaryColor = 0xFFD97706,
        previewHex = "#8B1E1E"
    ),
    EMERALD_GREEN(
        label = "Emerald Contractor",
        description = "Fresh forest green & mint accents",
        primaryColor = 0xFF0D6E48,
        secondaryColor = 0xFF059669,
        previewHex = "#0D6E48"
    ),
    ROYAL_INDIGO(
        label = "Royal Indigo",
        description = "Executive deep navy & lavender blue",
        primaryColor = 0xFF1E3A8A,
        secondaryColor = 0xFF3B82F6,
        previewHex = "#1E3A8A"
    ),
    SLATE_CHARCOAL(
        label = "Modern Slate",
        description = "Industrial charcoal & steel cyan",
        primaryColor = 0xFF334155,
        secondaryColor = 0xFF0284C7,
        previewHex = "#334155"
    ),
    WARM_TERRACOTTA(
        label = "Warm Terracotta",
        description = "Earthy brick amber & warm clay",
        primaryColor = 0xFF9A3412,
        secondaryColor = 0xFFEA580C,
        previewHex = "#9A3412"
    ),
    ROYAL_PURPLE(
        label = "Majestic Plum",
        description = "Imperial purple & rose accents",
        primaryColor = 0xFF581C87,
        secondaryColor = 0xFF9333EA,
        previewHex = "#581C87"
    )
}

enum class LedgerLayoutStyle(val label: String, val subtitle: String) {
    CLASSIC_GRID(
        "Bahi Khata Ledger Grid",
        "Authentic horizontal-scroll table with SL, Name, P/H/A, OT, Taken, Due"
    ),
    CARD_FEED(
        "Modern Card Feed",
        "Spacious cards with worker avatars, call buttons, and visual badges"
    ),
    COMPACT_LIST(
        "High-Density Fast List",
        "Dense rows for rapid scanning and bulk attendance entry"
    )
}

enum class CurrencyOption(val symbol: String, val label: String) {
    RS("Rs", "Rupees (Rs)"),
    INR("₹", "Rupee (₹)"),
    TAK("৳", "Taka (৳)"),
    USD("$", "Dollar ($)"),
    AED("AED", "Dirham (AED)"),
    EUR("€", "Euro (€)")
}

enum class CardCornerStyle(val label: String, val cornerDp: Int) {
    ROUNDED("Rounded Modern (16dp)", 16),
    CLASSIC("Subtle Classic (8dp)", 8),
    SHARP("Ledger Sharp (4dp)", 4),
    PILL("Soft Pill (24dp)", 24)
}

data class AppSettings(
    val appLanguage: AppLanguage = AppLanguage.BENGALI_IN,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val colorPalette: ColorPalette = ColorPalette.TRADITIONAL_KHATA,
    val ledgerLayoutStyle: LedgerLayoutStyle = LedgerLayoutStyle.CLASSIC_GRID,
    val currencySymbol: String = "Rs",
    val standardWorkHours: Int = 8,
    val cardCornerStyle: CardCornerStyle = CardCornerStyle.ROUNDED,
    val showPhoneNumbers: Boolean = true,
    val showOtColumn: Boolean = true,
    val highlightDueBalance: Boolean = true,
    val soundVibrationEnabled: Boolean = true,
    val compactDensity: Boolean = false
)
