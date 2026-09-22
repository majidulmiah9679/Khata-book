package com.example.ui.model

import com.example.data.entity.Attendance
import com.example.data.entity.Payment
import com.example.data.entity.Worker
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class WorkerMonthlySummary(
    val worker: Worker,
    val totalHajira: Double,
    val presentDays: Int,
    val oneAndHalfDays: Int = 0,
    val doubleDays: Int = 0,
    val halfDays: Int,
    val absentDays: Int,
    val totalOvertimeHours: Double = 0.0,
    val totalOvertimeWage: Double = 0.0,
    val totalEarned: Double,
    val totalMoneyTaken: Double,
    val balanceDue: Double,
    val todayStatus: String? = null,
    val todayOvertime: Double = 0.0,
    val todayWage: Double = 0.0
)

data class AttendanceRecordItem(
    val attendance: Attendance,
    val dailyWage: Double
) {
    val baseAmount: Double
        get() = when (attendance.status) {
            "Present", "1.0" -> dailyWage
            "OneAndHalf", "1.5" -> dailyWage * 1.5
            "Double", "2.0" -> dailyWage * 2.0
            "Half", "0.5" -> dailyWage * 0.5
            else -> 0.0
        }

    val overtimeAmount: Double
        get() = attendance.overtime * (dailyWage / 8.0)

    val earnedAmount: Double
        get() = baseAmount + overtimeAmount
}

object KhataDateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
    private val fullDisplayFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.US)
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    fun getTodayIso(): String {
        return isoFormat.format(Calendar.getInstance().time)
    }

    fun getCurrentYear(): Int {
        return Calendar.getInstance().get(Calendar.YEAR)
    }

    fun getCurrentMonth(): Int {
        return Calendar.getInstance().get(Calendar.MONTH) + 1 // 1-12
    }

    fun formatDisplayDate(isoDate: String): String {
        return try {
            val date = isoFormat.parse(isoDate)
            if (date != null) displayFormat.format(date) else isoDate
        } catch (e: Exception) {
            isoDate
        }
    }

    fun formatFullDate(isoDate: String): String {
        return try {
            val date = isoFormat.parse(isoDate)
            if (date != null) fullDisplayFormat.format(date) else isoDate
        } catch (e: Exception) {
            isoDate
        }
    }

    fun getMonthName(monthNumber: Int): String {
        return if (monthNumber in 1..12) monthNames[monthNumber - 1] else "Month $monthNumber"
    }

    fun toYearMonthPattern(year: Int, month: Int): String {
        return String.format(Locale.US, "%04d-%02d", year, month)
    }

    fun shiftDate(isoDate: String, days: Int): String {
        return try {
            val cal = Calendar.getInstance()
            val date = isoFormat.parse(isoDate)
            if (date != null) {
                cal.time = date
                cal.add(Calendar.DAY_OF_YEAR, days)
                isoFormat.format(cal.time)
            } else isoDate
        } catch (e: Exception) {
            isoDate
        }
    }

    fun parseYearFromIso(isoDate: String): Int {
        return try {
            isoDate.substring(0, 4).toInt()
        } catch (e: Exception) {
            getCurrentYear()
        }
    }

    fun parseMonthFromIso(isoDate: String): Int {
        return try {
            isoDate.substring(5, 7).toInt()
        } catch (e: Exception) {
            getCurrentMonth()
        }
    }

    fun getDaysInMonth(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun getDayOfWeekShort(year: Int, month: Int, day: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, day)
        val shortDays = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val dayOfWeekIndex = cal.get(Calendar.DAY_OF_WEEK) - 1
        return if (dayOfWeekIndex in shortDays.indices) shortDays[dayOfWeekIndex] else ""
    }

    fun getDayOfWeekFull(year: Int, month: Int, day: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, day)
        val fullDays = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val dayOfWeekIndex = cal.get(Calendar.DAY_OF_WEEK) - 1
        return if (dayOfWeekIndex in fullDays.indices) fullDays[dayOfWeekIndex] else ""
    }

    fun isSunday(year: Int, month: Int, day: Int): Boolean {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, day)
        return cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
    }

    fun buildIsoDate(year: Int, month: Int, day: Int): String {
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
    }
}

data class KhataDayRow(
    val dayNumber: Int,
    val isoDate: String,
    val dayOfWeekShort: String,
    val dayOfWeekFull: String,
    val isSunday: Boolean,
    val isToday: Boolean,
    val attendance: Attendance?,
    val payments: List<Payment>,
    val dailyWage: Double,
    val standardHours: Double = 8.0
) {
    val status: String? get() = attendance?.status
    val overtimeHours: Double get() = attendance?.overtime ?: 0.0

    val baseEarned: Double
        get() = when (status) {
            "Present", "1.0" -> dailyWage
            "OneAndHalf", "1.5" -> dailyWage * 1.5
            "Double", "2.0" -> dailyWage * 2.0
            "Half", "0.5" -> dailyWage * 0.5
            else -> 0.0
        }

    val overtimeEarned: Double
        get() = if (overtimeHours > 0.0 && standardHours > 0.0) {
            (dailyWage / standardHours) * overtimeHours
        } else 0.0

    val totalEarned: Double get() = baseEarned + overtimeEarned

    val totalAdvance: Double get() = payments.sumOf { it.amountTaken }

    val netDailyBalance: Double get() = totalEarned - totalAdvance
}

object CurrencyFormatter {
    var defaultCurrencySymbol: String = "Rs"

    fun formatTaka(amount: Double, symbol: String = defaultCurrencySymbol): String {
        val rounded = if (amount % 1.0 == 0.0) {
            String.format(Locale.US, "%,.0f", amount)
        } else {
            String.format(Locale.US, "%,.2f", amount)
        }
        return "$symbol $rounded"
    }

    fun formatDays(days: Double): String {
        return if (days % 1.0 == 0.0) {
            "${days.toInt()} days"
        } else {
            "$days days"
        }
    }
}
