package com.example

import com.example.data.entity.Attendance
import com.example.data.entity.Worker
import com.example.ui.model.CurrencyFormatter
import com.example.ui.model.KhataDateUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testHajiraCalculations() {
        val worker = Worker(id = 1, name = "Md. Rafiqul Islam", phone = "01712-345678", dailyWage = 500.0)

        // 20 Present days (1.0 each) + 4 Half days (0.5 each) = 22.0 days total hajira
        val presentAttendances = List(20) {
            Attendance(id = it, workerId = worker.id, date = "2026-09-${String.format("%02d", it + 1)}", month = 9, year = 2026, status = "Present")
        }
        val halfAttendances = List(4) {
            Attendance(id = 20 + it, workerId = worker.id, date = "2026-09-${String.format("%02d", 21 + it)}", month = 9, year = 2026, status = "Half")
        }

        val allAttendances = presentAttendances + halfAttendances
        val totalHajira = allAttendances.sumOf { it.hajiraUnits }
        assertEquals(22.0, totalHajira, 0.001)

        val totalEarned = totalHajira * worker.dailyWage
        assertEquals(11000.0, totalEarned, 0.001)

        val advanceTaken = 3000.0
        val balanceDue = totalEarned - advanceTaken
        assertEquals(8000.0, balanceDue, 0.001)

        // Formatter checks
        assertEquals("৳ 11,000", CurrencyFormatter.formatTaka(totalEarned))
        assertEquals("৳ 8,000", CurrencyFormatter.formatTaka(balanceDue))
        assertEquals("22 days", CurrencyFormatter.formatDays(totalHajira))
    }

    @Test
    fun testDateUtils() {
        assertEquals("September", KhataDateUtils.getMonthName(9))
        assertEquals("2026-09", KhataDateUtils.toYearMonthPattern(2026, 9))
        assertEquals("21 Sep 2026", KhataDateUtils.formatDisplayDate("2026-09-21"))
    }
}
