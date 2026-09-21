package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.settings.AppSettings
import com.example.ui.components.DateSelectorBar
import com.example.ui.model.CurrencyFormatter
import com.example.ui.model.KhataDateUtils
import com.example.ui.model.WorkerMonthlySummary
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.AbsentRedLight
import com.example.ui.theme.DueAmber
import com.example.ui.theme.HalfOrange
import com.example.ui.theme.HalfOrangeLight
import com.example.ui.theme.KhataPrimary
import com.example.ui.theme.KhataTertiary
import com.example.ui.theme.LedgerBorder
import com.example.ui.theme.LedgerGold
import com.example.ui.theme.LedgerGridBorder
import com.example.ui.theme.LedgerHeaderBg
import com.example.ui.theme.LedgerParchment
import com.example.ui.theme.LedgerRed
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenLight
import com.example.ui.util.PdfExportUtil

@Composable
fun AttendanceScreen(
    selectedDate: String,
    selectedYear: Int,
    selectedMonth: Int,
    workerSummaries: List<WorkerMonthlySummary>,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onDateSelect: (String) -> Unit,
    onMonthSelect: (Int, Int) -> Unit,
    onStatusChange: (workerId: Int, status: String) -> Unit,
    onOvertimeChange: ((workerId: Int, overtime: Double) -> Unit)? = null,
    onMarkAll: (status: String) -> Unit,
    onWorkerClick: (Int) -> Unit,
    settings: AppSettings = AppSettings(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val totalWorkers = workerSummaries.size
    val presentCount = workerSummaries.count { it.todayStatus == "Present" }
    val halfCount = workerSummaries.count { it.todayStatus == "Half" }
    val absentCount = workerSummaries.count { it.todayStatus == "Absent" }
    val unmarkedCount = totalWorkers - (presentCount + halfCount + absentCount)
    val todayWageTotal = workerSummaries.sumOf { it.todayWage }

    val monthTotalEarned = workerSummaries.sumOf { it.totalEarned }
    val monthTotalTaken = workerSummaries.sumOf { it.totalMoneyTaken }
    val monthBalanceDue = workerSummaries.sumOf { it.balanceDue }

    val monthName = KhataDateUtils.getMonthName(selectedMonth)
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("attendance_screen_content")
    ) {
        // 1. Top Indian Ledger Banner with Compact Calculation & PDF Share Button
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Header Top Row: Title + PDF Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = LedgerRed,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.size(10.dp, 16.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LABOR HAJIRA KHATA",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = "Daily Attendance & Ledger ($totalWorkers Workers Registered)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // PDF Export & Share Button
                    Button(
                        onClick = {
                            PdfExportUtil.generateAndShareMonthlyReport(
                                context = context,
                                monthName = monthName,
                                year = selectedYear,
                                workerSummaries = workerSummaries,
                                totalEarned = monthTotalEarned,
                                totalTaken = monthTotalTaken,
                                balanceDue = monthBalanceDue
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LedgerRed),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("pdf_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Share PDF",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PDF Share",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Compact Right-Side Balance Due & Calculation Strip (As requested by user)
                Surface(
                    color = LedgerHeaderBg,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Attendance summary counters
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "P: $presentCount",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                color = PresentGreen
                            )
                            Text(
                                text = "•",
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "H: $halfCount",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                color = HalfOrange
                            )
                            Text(
                                text = "•",
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "A: $absentCount",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                color = AbsentRed
                            )
                        }

                        // Right: Calculation after money taken -> Current Balance Due
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Earned: ${CurrencyFormatter.formatTaka(monthTotalEarned)} | Taken: ${CurrencyFormatter.formatTaka(monthTotalTaken)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Current Due: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = CurrencyFormatter.formatTaka(monthBalanceDue),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (monthBalanceDue >= 0) DueAmber else AbsentRed
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Date & Month Selector Bar
        Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
            DateSelectorBar(
                currentDate = selectedDate,
                selectedYear = selectedYear,
                selectedMonth = selectedMonth,
                onPreviousDay = onPreviousDay,
                onNextDay = onNextDay,
                onDateSelect = onDateSelect,
                onMonthSelect = onMonthSelect
            )
        }

        // 3. Bulk Action Shortcuts (Mark all 20+ present / absent)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { onMarkAll("Present") },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PresentGreen),
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .testTag("mark_all_present_button")
            ) {
                Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Mark All Present (20)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { onMarkAll("Absent") },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .testTag("mark_all_absent_button")
            ) {
                Text("Mark All Absent", style = MaterialTheme.typography.labelSmall, color = AbsentRed, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 4. Ledger Grid Table for 20+ Workers
        // Supports horizontal scroll so columns (Date, Month, Name, Hajira, Overtime, Taken Tk, Due Tk) align cleanly
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, LedgerGridBorder),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Scrollable Table Container
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        Column(modifier = Modifier.width(740.dp)) {
                            // Ledger Table Header Row
                            LedgerTableHeader(monthName = monthName, dateStr = KhataDateUtils.formatDisplayDate(selectedDate))

                            HorizontalDivider(color = LedgerGridBorder, thickness = 1.dp)

                            // 20+ Workers Scrollable Rows
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                itemsIndexed(workerSummaries, key = { _, item -> item.worker.id }) { index, summary ->
                                    LedgerWorkerRow(
                                        index = index + 1,
                                        summary = summary,
                                        dateStr = selectedDate,
                                        monthStr = monthName,
                                        onStatusChange = { newStatus -> onStatusChange(summary.worker.id, newStatus) },
                                        onOvertimeChange = { newOt -> onOvertimeChange?.invoke(summary.worker.id, newOt) },
                                        onClick = { onWorkerClick(summary.worker.id) }
                                    )
                                    HorizontalDivider(color = LedgerBorder, thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Fixed Live Footer Summary Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today's Attendance: $presentCount P • $halfCount H • $absentCount A",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Total Registered: $totalWorkers Workers (Fast 20-entry ledger)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Today's Total Wage",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatTaka(todayWageTotal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = PresentGreen
                    )
                }
            }
        }
    }
}

/**
 * Traditional Indian Ledger Table Header
 */
@Composable
private fun LedgerTableHeader(
    monthName: String,
    dateStr: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = LedgerHeaderBg,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // # / SL
            Text(
                text = "#",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = LedgerGold,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(28.dp)
            )

            // Worker Name & Rate
            Text(
                text = "Worker (Daily Rate)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(170.dp)
            )

            // Attendance (Present / Half / Absent)
            Text(
                text = "Hajira (Today)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(140.dp)
            )

            // Overtime (Hours)
            Text(
                text = "Overtime (OT)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(120.dp)
            )

            // Taka Taken (Advance)
            Text(
                text = "Taken (${CurrencyFormatter.defaultCurrencySymbol})",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = KhataTertiary,
                textAlign = TextAlign.End,
                modifier = Modifier.width(110.dp)
            )

            // Net Due (৳)
            Text(
                text = "Balance Due (${CurrencyFormatter.defaultCurrencySymbol})",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = LedgerRed,
                textAlign = TextAlign.End,
                modifier = Modifier.width(130.dp)
            )
        }
    }
}

/**
 * Row representing one worker in the 20-worker Indian ledger grid
 */
@Composable
private fun LedgerWorkerRow(
    index: Int,
    summary: WorkerMonthlySummary,
    dateStr: String,
    monthStr: String,
    onStatusChange: (String) -> Unit,
    onOvertimeChange: (Double) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPresent = summary.todayStatus == "Present"
    val isHalf = summary.todayStatus == "Half"
    val isAbsent = summary.todayStatus == "Absent"

    val rowBg = when {
        isPresent -> PresentGreenLight.copy(alpha = 0.35f)
        isHalf -> HalfOrangeLight.copy(alpha = 0.45f)
        isAbsent -> AbsentRedLight.copy(alpha = 0.35f)
        index % 2 == 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.surface
    }

    Surface(
        color = rowBg,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("ledger_row_${summary.worker.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Index SL
            Text(
                text = "$index",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(28.dp)
            )

            // Worker Name & Daily Rate
            Column(
                modifier = Modifier
                    .width(170.dp)
                    .clickable(onClick = onClick)
            ) {
                Text(
                    text = summary.worker.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Rate: ${CurrencyFormatter.formatTaka(summary.worker.dailyWage)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• 📖 Khata",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp
                    )
                }
            }

            // Attendance Selector (Present / Half / Absent toggle)
            Row(
                modifier = Modifier.width(140.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Present Checkbox / Button
                Surface(
                    onClick = {
                        if (isPresent) onStatusChange("Absent") else onStatusChange("Present")
                    },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isPresent) PresentGreen else Color.Transparent,
                    border = if (isPresent) null else androidx.compose.foundation.BorderStroke(1.dp, PresentGreen),
                    modifier = Modifier
                        .size(36.dp, 32.dp)
                        .testTag("ledger_present_${summary.worker.id}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "P",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isPresent) Color.White else PresentGreen
                        )
                    }
                }

                // Half Day
                Surface(
                    onClick = { onStatusChange("Half") },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isHalf) HalfOrange else Color.Transparent,
                    border = if (isHalf) null else androidx.compose.foundation.BorderStroke(1.dp, HalfOrange),
                    modifier = Modifier
                        .size(36.dp, 32.dp)
                        .testTag("ledger_half_${summary.worker.id}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "½",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isHalf) Color.White else HalfOrange
                        )
                    }
                }

                // Absent
                Surface(
                    onClick = { onStatusChange("Absent") },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isAbsent) AbsentRed else Color.Transparent,
                    border = if (isAbsent) null else androidx.compose.foundation.BorderStroke(1.dp, AbsentRed),
                    modifier = Modifier
                        .size(36.dp, 32.dp)
                        .testTag("ledger_absent_${summary.worker.id}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "A",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isAbsent) Color.White else AbsentRed
                        )
                    }
                }
            }

            // Overtime (Hours selector - minus, display, plus)
            Row(
                modifier = Modifier.width(120.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = {
                        val next = maxOf(0.0, summary.todayOvertime - 1.0)
                        onOvertimeChange(next)
                    },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Minus OT",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (summary.todayOvertime > 0) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = if (summary.todayOvertime % 1.0 == 0.0) "${summary.todayOvertime.toInt()}h" else "${summary.todayOvertime}h",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (summary.todayOvertime > 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (summary.todayOvertime > 0) LedgerGold else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                IconButton(
                    onClick = {
                        val next = summary.todayOvertime + 1.0
                        onOvertimeChange(next)
                    },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Plus OT",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Taka Taken (Advance for this month)
            Column(
                modifier = Modifier.width(110.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = CurrencyFormatter.formatTaka(summary.totalMoneyTaken),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = KhataTertiary
                )
                Text(
                    text = "Advance",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Net Balance Due (৳)
            Column(
                modifier = Modifier.width(130.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = CurrencyFormatter.formatTaka(summary.balanceDue),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (summary.balanceDue >= 0) DueAmber else AbsentRed
                )
                Text(
                    text = if (summary.balanceDue >= 0) "Payable Due" else "Advance Excess",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (summary.balanceDue >= 0) MaterialTheme.colorScheme.onSurfaceVariant else AbsentRed
                )
            }
        }
    }
}
