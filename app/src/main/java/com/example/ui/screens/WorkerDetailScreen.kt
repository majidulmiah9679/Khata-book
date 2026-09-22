package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Attendance
import com.example.data.entity.Payment
import com.example.data.entity.Worker
import com.example.data.settings.AppSettings
import com.example.ui.components.StatusBadge
import com.example.ui.model.CurrencyFormatter
import com.example.ui.model.KhataDateUtils
import com.example.ui.model.KhataDayRow
import com.example.ui.model.WorkerMonthlySummary
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.DueAmber
import com.example.ui.theme.HalfOrange
import com.example.ui.theme.KhataPrimary
import com.example.ui.theme.KhataSecondary
import com.example.ui.theme.KhataTertiary
import com.example.ui.theme.LedgerRed
import com.example.ui.theme.PresentGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDetailScreen(
    workerId: Int,
    workerSummary: WorkerMonthlySummary?,
    workerAttendanceList: List<Attendance>,
    workerPaymentsList: List<Payment>,
    selectedYear: Int,
    selectedMonth: Int,
    onBack: () -> Unit,
    onMonthChange: (Int, Int) -> Unit,
    onAddPaymentClick: (Int) -> Unit,
    onEditWorkerClick: (Worker) -> Unit,
    onDeletePayment: (Int) -> Unit,
    onAttendanceStatusChange: (date: String, status: String) -> Unit,
    onSetAttendanceForDate: (date: String, status: String, overtime: Double) -> Unit = { _, _, _ -> },
    onDeleteAttendanceForDate: (date: String) -> Unit = { _ -> },
    onAddPaymentForDate: (date: String, amount: Double, note: String) -> Unit = { _, _, _ -> },
    settings: AppSettings = AppSettings(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Khata Register, 1: Payments List
    var showMonthMenu by remember { mutableStateOf(false) }
    var paymentToDelete by remember { mutableStateOf<Payment?>(null) }
    var selectedDayForEdit by remember { mutableStateOf<KhataDayRow?>(null) }
    var highlightedDayNumber by remember { mutableIntStateOf(-1) }

    val worker = workerSummary?.worker
    val daysInMonth = remember(selectedYear, selectedMonth) {
        KhataDateUtils.getDaysInMonth(selectedYear, selectedMonth)
    }
    val todayIso = remember { KhataDateUtils.getTodayIso() }

    // Map attendance and payments by date for O(1) lookups
    val attendanceByDate = remember(workerAttendanceList) {
        workerAttendanceList.associateBy { it.date }
    }
    val paymentsByDate = remember(workerPaymentsList) {
        workerPaymentsList.groupBy { it.date }
    }

    // Build the 28-31 full month line-by-line Khata rows
    val khataDays: List<KhataDayRow> = remember(
        daysInMonth,
        selectedYear,
        selectedMonth,
        attendanceByDate,
        paymentsByDate,
        worker?.dailyWage,
        settings.standardWorkHours
    ) {
        val wage = worker?.dailyWage ?: 0.0
        (1..daysInMonth).map { day ->
            val iso = KhataDateUtils.buildIsoDate(selectedYear, selectedMonth, day)
            val shortDay = KhataDateUtils.getDayOfWeekShort(selectedYear, selectedMonth, day)
            val fullDay = KhataDateUtils.getDayOfWeekFull(selectedYear, selectedMonth, day)
            val isSun = KhataDateUtils.isSunday(selectedYear, selectedMonth, day)
            val isToday = (iso == todayIso)
            KhataDayRow(
                dayNumber = day,
                isoDate = iso,
                dayOfWeekShort = shortDay,
                dayOfWeekFull = fullDay,
                isSunday = isSun,
                isToday = isToday,
                attendance = attendanceByDate[iso],
                payments = paymentsByDate[iso] ?: emptyList(),
                dailyWage = wage,
                standardHours = settings.standardWorkHours.toDouble()
            )
        }
    }

    val computedSummary = remember(
        worker,
        workerAttendanceList,
        workerPaymentsList,
        settings.standardWorkHours
    ) {
        if (worker == null) return@remember null
        var present = 0
        var oneAndHalf = 0
        var doubleHajira = 0
        var half = 0
        var absent = 0
        var totalOvertimeHours = 0.0

        workerAttendanceList.forEach { att ->
            when (att.status) {
                "Present", "1.0" -> present++
                "OneAndHalf", "1.5" -> oneAndHalf++
                "Double", "2.0" -> doubleHajira++
                "Half", "0.5" -> half++
                "Absent" -> absent++
            }
            totalOvertimeHours += att.overtime
        }

        val totalHajira = present * 1.0 + oneAndHalf * 1.5 + doubleHajira * 2.0 + half * 0.5
        val stdHours = if (settings.standardWorkHours > 0) settings.standardWorkHours.toDouble() else 8.0
        val hourlyWage = worker.dailyWage / stdHours
        val totalOvertimeWage = totalOvertimeHours * hourlyWage
        val baseEarned = (present * 1.0 + oneAndHalf * 1.5 + doubleHajira * 2.0 + half * 0.5) * worker.dailyWage
        val totalEarned = baseEarned + totalOvertimeWage
        val totalMoneyTaken = workerPaymentsList.sumOf { it.amountTaken }
        val balanceDue = totalEarned - totalMoneyTaken

        WorkerMonthlySummary(
            worker = worker,
            totalHajira = totalHajira,
            presentDays = present,
            oneAndHalfDays = oneAndHalf,
            doubleDays = doubleHajira,
            halfDays = half,
            absentDays = absent,
            totalOvertimeHours = totalOvertimeHours,
            totalOvertimeWage = totalOvertimeWage,
            totalEarned = totalEarned,
            totalMoneyTaken = totalMoneyTaken,
            balanceDue = balanceDue,
            todayStatus = null,
            todayOvertime = 0.0,
            todayWage = 0.0
        )
    }

    // Helper to share khata statement
    fun shareStatement() {
        val activeSummary = computedSummary ?: workerSummary
        if (worker == null || activeSummary == null) return
        val text = buildString {
            appendLine("===============================")
            appendLine("📖 LABOR ATTENDANCE KHATA (হাজিরা খাতা)")
            appendLine("Worker: ${worker.name}")
            if (worker.phone.isNotBlank()) appendLine("Mobile: ${worker.phone}")
            appendLine("Daily Rate: ${CurrencyFormatter.formatTaka(worker.dailyWage)}/day")
            appendLine("Month: ${KhataDateUtils.getMonthName(selectedMonth)} $selectedYear")
            appendLine("-------------------------------")
            appendLine("Total Working Days: ${CurrencyFormatter.formatDays(activeSummary.totalHajira)}")
            appendLine("• Full (১.০): ${activeSummary.presentDays} | 1.5x (১.৫): ${activeSummary.oneAndHalfDays} | Half (০.৫): ${activeSummary.halfDays} | 2.0x (২.০): ${activeSummary.doubleDays} | Absent: ${activeSummary.absentDays}")
            if (activeSummary.totalOvertimeHours > 0) {
                appendLine("• Overtime (OT): ${activeSummary.totalOvertimeHours} hrs (+${CurrencyFormatter.formatTaka(activeSummary.totalOvertimeWage)})")
            }
            appendLine("-------------------------------")
            appendLine("Total Wage Earned: ${CurrencyFormatter.formatTaka(activeSummary.totalEarned)}")
            appendLine("Total Advance Taken: ${CurrencyFormatter.formatTaka(activeSummary.totalMoneyTaken)}")
            appendLine("-------------------------------")
            val dueLabel = if (activeSummary.balanceDue >= 0) "NET BALANCE DUE (বাকি পাওনা)" else "OVERPAID ADVANCE (অতিরিক্ত অগ্রিম)"
            appendLine("$dueLabel: ${CurrencyFormatter.formatTaka(kotlin.math.abs(activeSummary.balanceDue))}")
            appendLine("===============================")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Khata Statement"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = worker?.name ?: "Worker Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Attendance Khata & Wage Register",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("worker_detail_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { shareStatement() },
                        modifier = Modifier.testTag("worker_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Khata Statement",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (worker != null && worker.phone.isNotBlank() && settings.showPhoneNumbers) {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${worker.phone}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.testTag("worker_call_button")
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    if (worker != null) {
                        IconButton(
                            onClick = { onEditWorkerClick(worker) },
                            modifier = Modifier.testTag("worker_edit_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Worker")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("worker_detail_screen")
    ) { innerPadding ->
        if (worker == null || workerSummary == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Worker not found")
            }
            return@Scaffold
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Month Navigation Header (Prev, Month Dropdown, Next) + Rate Badge
            item {
                KhataMonthHeader(
                    selectedYear = selectedYear,
                    selectedMonth = selectedMonth,
                    dailyWage = worker.dailyWage,
                    showMonthMenu = showMonthMenu,
                    onToggleMonthMenu = { showMonthMenu = it },
                    onMonthChange = onMonthChange
                )
            }

            // 2. Authentic Khata Summary Ledger Card (শ্রমিকের সম্পূর্ণ হিসাব ও খতিয়ান) - PLACED FIRST AT TOP
            item {
                val activeSummary = computedSummary ?: workerSummary
                if (activeSummary != null) {
                    KhataSummaryCard(
                        worker = worker,
                        workerSummary = activeSummary,
                        selectedMonth = selectedMonth,
                        selectedYear = selectedYear,
                        highlightDue = settings.highlightDueBalance,
                        onAddPaymentClick = { onAddPaymentClick(worker.id) }
                    )
                }
            }

            // 3. Top Calendar & Day of Week Ribbon ("উপরে month ar kon din ki bar oi ta thakbe")
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${KhataDateUtils.getMonthName(selectedMonth)} Calendar (তারিখ ও বার)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Tap day to view/mark",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    KhataCalendarDateStrip(
                        days = khataDays,
                        highlightedDay = highlightedDayNumber,
                        onDayClick = { dayRow ->
                            highlightedDayNumber = dayRow.dayNumber
                            selectedDayForEdit = dayRow
                        }
                    )
                }
            }

            // 4. View Tabs: Khata Register (Line-by-Line 31 Days Table) vs Payment Ledger
            item {
                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Khata Register (${daysInMonth} Days)",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Payments (${workerPaymentsList.size})",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                }
            }

            // 5. Line by Line Khata Table or Payments View
            if (selectedTab == 0) {
                // Tab 0: True Line-by-Line Khata Register Table
                item {
                    KhataRegisterHeaderRow(
                        showOt = settings.showOtColumn,
                        currencySymbol = CurrencyFormatter.defaultCurrencySymbol
                    )
                }

                items(khataDays, key = { it.isoDate }) { dayRow ->
                    KhataRegisterDayLine(
                        dayRow = dayRow,
                        dailyWage = worker.dailyWage,
                        showOt = settings.showOtColumn,
                        isHighlighted = (dayRow.dayNumber == highlightedDayNumber),
                        onLineClick = {
                            highlightedDayNumber = dayRow.dayNumber
                            selectedDayForEdit = dayRow
                        },
                        onQuickStatusChange = { newStatus ->
                            onSetAttendanceForDate(dayRow.isoDate, newStatus, dayRow.overtimeHours)
                        }
                    )
                }
            } else {
                // Tab 1: Detailed Payment & Advance Records
                if (workerPaymentsList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No payment or advance records for this month yet",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(workerPaymentsList, key = { it.id }) { payment ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = KhataDateUtils.formatDisplayDate(payment.date),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (payment.note.isNotBlank()) {
                                        Text(
                                            text = payment.note,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "-${CurrencyFormatter.formatTaka(payment.amountTaken)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = KhataTertiary
                                    )

                                    IconButton(
                                        onClick = { paymentToDelete = payment },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete payment",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Day Khata Entry Dialog (when tapping any day row or date card)
    if (selectedDayForEdit != null) {
        val dayRow = selectedDayForEdit!!
        KhataDayEntryDialog(
            dayRow = dayRow,
            dailyWage = worker?.dailyWage ?: 0.0,
            standardWorkHours = settings.standardWorkHours,
            onDismiss = { selectedDayForEdit = null },
            onSave = { status, overtime, advanceAmount, advanceNote ->
                if (status != null) {
                    onSetAttendanceForDate(dayRow.isoDate, status, overtime)
                } else {
                    onDeleteAttendanceForDate(dayRow.isoDate)
                }

                if (advanceAmount > 0.0) {
                    onAddPaymentForDate(dayRow.isoDate, advanceAmount, advanceNote)
                }
                selectedDayForEdit = null
            }
        )
    }

    // Delete Payment Confirmation Dialog
    if (paymentToDelete != null) {
        val p = paymentToDelete!!
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            title = { Text("Delete Payment Entry?") },
            text = {
                Text("Are you sure you want to delete payment of ${CurrencyFormatter.formatTaka(p.amountTaken)} recorded on ${KhataDateUtils.formatDisplayDate(p.date)}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePayment(p.id)
                        paymentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// -------------------------------------------------------------------------
// Sub-components: Month Navigation Header
// -------------------------------------------------------------------------

@Composable
fun KhataMonthHeader(
    selectedYear: Int,
    selectedMonth: Int,
    dailyWage: Double,
    showMonthMenu: Boolean,
    onToggleMonthMenu: (Boolean) -> Unit,
    onMonthChange: (Int, Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Prev button, Month Dropdown, Next button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = {
                    if (selectedMonth == 1) {
                        onMonthChange(selectedYear - 1, 12)
                    } else {
                        onMonthChange(selectedYear, selectedMonth - 1)
                    }
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Month",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Box {
                Surface(
                    onClick = { onToggleMonthMenu(true) },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.testTag("worker_detail_month_picker")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${KhataDateUtils.getMonthName(selectedMonth)} $selectedYear",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                DropdownMenu(
                    expanded = showMonthMenu,
                    onDismissRequest = { onToggleMonthMenu(false) }
                ) {
                    KhataDateUtils.monthNames.forEachIndexed { index, monthName ->
                        val monthNum = index + 1
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "$monthName $selectedYear",
                                    fontWeight = if (monthNum == selectedMonth) FontWeight.Bold else FontWeight.Normal,
                                    color = if (monthNum == selectedMonth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onMonthChange(selectedYear, monthNum)
                                onToggleMonthMenu(false)
                            }
                        )
                    }
                }
            }

            IconButton(
                onClick = {
                    if (selectedMonth == 12) {
                        onMonthChange(selectedYear + 1, 1)
                    } else {
                        onMonthChange(selectedYear, selectedMonth + 1)
                    }
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Month",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Daily Rate Badge
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Rate: ${CurrencyFormatter.formatTaka(dailyWage)}/day",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

// -------------------------------------------------------------------------
// Sub-components: Top Month Calendar Ribbon ("উপরে month ar kon din ki bar")
// -------------------------------------------------------------------------

@Composable
fun KhataCalendarDateStrip(
    days: List<KhataDayRow>,
    highlightedDay: Int,
    onDayClick: (KhataDayRow) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        days.forEach { dayRow ->
            val isSelected = dayRow.dayNumber == highlightedDay
            val isSunday = dayRow.isSunday

            val cardBg = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                dayRow.isToday -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                isSunday -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                else -> MaterialTheme.colorScheme.surface
            }

            val borderColor = when {
                isSelected -> MaterialTheme.colorScheme.primary
                dayRow.isToday -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            }

            Surface(
                modifier = Modifier
                    .width(58.dp)
                    .clickable { onDayClick(dayRow) },
                shape = RoundedCornerShape(12.dp),
                color = cardBg,
                border = androidx.compose.foundation.BorderStroke(if (isSelected || dayRow.isToday) 1.5.dp else 1.dp, borderColor),
                tonalElevation = if (isSelected) 3.dp else 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Day of Week (Sun, Mon, Tue...)
                    Text(
                        text = dayRow.dayOfWeekShort,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSunday) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSunday) AbsentRed else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )

                    // Day Number (01, 02... 31)
                    Text(
                        text = String.format("%02d", dayRow.dayNumber),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )

                    // Status Indicator Dot or Icon
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (dayRow.status) {
                            "Present", "1.0" -> Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(PresentGreen, CircleShape)
                            )
                            "OneAndHalf", "1.5" -> Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(KhataPrimary, CircleShape)
                            )
                            "Double", "2.0" -> Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(Color(0xFF1E88E5), CircleShape)
                            )
                            "Half", "0.5" -> Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(HalfOrange, CircleShape)
                            )
                            "Absent" -> Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(AbsentRed, CircleShape)
                            )
                            else -> Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            )
                        }

                        // Blue dot if advance/money taken on this date
                        if (dayRow.totalAdvance > 0) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(KhataTertiary, CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// Sub-components: Khata Summary Card
// -------------------------------------------------------------------------

@Composable
fun KhataSummaryCard(
    worker: Worker,
    workerSummary: WorkerMonthlySummary,
    selectedMonth: Int,
    selectedYear: Int,
    highlightDue: Boolean,
    onAddPaymentClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("worker_financial_summary_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Worker name & month with 100% Offline Secured Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(KhataPrimary, CircleShape)
                    )
                    Column {
                        Text(
                            text = "${KhataDateUtils.getMonthName(selectedMonth)} $selectedYear — হিসাব খতিয়ান",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "শ্রমিকের সম্পূর্ণ হিসাব ও খতিয়ান (Full Ledger)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 100% Offline Status Indicator
                Surface(
                    color = PresentGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PresentGreen.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = PresentGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "১০০% অফলাইন",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PresentGreen,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // SECTION 1 & 2: হাজিরা কয়টা & ওভারটাইম কয়টা
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ১. হাজিরা কয়টা
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "১. মোট হাজিরা কয়টা",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${CurrencyFormatter.formatDays(workerSummary.totalHajira)} দিন",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // Attendance Breakdown (1.0, 1.5, 0.5, 2.0, A)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "1.0:${workerSummary.presentDays}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PresentGreen,
                                fontSize = 10.sp
                            )
                            if (workerSummary.oneAndHalfDays > 0) {
                                Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "1.5:${workerSummary.oneAndHalfDays}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = KhataPrimary,
                                    fontSize = 10.sp
                                )
                            }
                            Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "0.5:${workerSummary.halfDays}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = HalfOrange,
                                fontSize = 10.sp
                            )
                            if (workerSummary.doubleDays > 0) {
                                Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "2.0:${workerSummary.doubleDays}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E88E5),
                                    fontSize = 10.sp
                                )
                            }
                            Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "A:${workerSummary.absentDays}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AbsentRed,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // ২. ওভারটাইম কয়টা
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "২. ওভারটাইম কয় ঘণ্টা",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${workerSummary.totalOvertimeHours} ঘণ্টা",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (workerSummary.totalOvertimeHours > 0) DueAmber else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (workerSummary.totalOvertimeWage > 0) {
                                "ওটি মজুরি: +${CurrencyFormatter.formatTaka(workerSummary.totalOvertimeWage)}"
                            } else {
                                "অতিরিক্ত সময় নেই"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // SECTION 3 & 4: টোটাল টাকা কত হয়েছে & তার থেকে নেওয়া টাকা কত
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ৩. টোটাল টাকা কত হয়েছে
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = PresentGreen.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PresentGreen.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "৩. টোটাল টাকা হয়েছে",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PresentGreen
                        )
                        Text(
                            text = CurrencyFormatter.formatTaka(workerSummary.totalEarned),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = PresentGreen
                        )
                        Text(
                            text = "মোট কামাই (মজুরি + ওটি)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }

                // ৪. তার থেকে নেওয়া টাকা কত
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = KhataTertiary.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, KhataTertiary.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "৪. নেওয়া টাকা কত",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = KhataTertiary
                        )
                        Text(
                            text = CurrencyFormatter.formatTaka(workerSummary.totalMoneyTaken),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = KhataTertiary
                        )
                        Text(
                            text = "অগ্রিম / পরিশোধ নেওয়া",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // SECTION 5: তারপরে পাওয়া যাবে কত (বাকি পাওনা)
            val isDue = workerSummary.balanceDue >= 0
            val bannerBg = if (isDue) {
                if (highlightDue) DueAmber.copy(alpha = 0.16f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            }
            val bannerBorder = if (isDue) DueAmber else MaterialTheme.colorScheme.error

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = bannerBg,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, bannerBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isDue) "৫. তারপরে পাওয়া যাবে কত (বাকি পাওনা):" else "৫. অতিরিক্ত টাকা নেওয়া (Overpaid):",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDue) DueAmber else MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = if (isDue) {
                                    "কামাই (${CurrencyFormatter.formatTaka(workerSummary.totalEarned)}) - নেওয়া (${CurrencyFormatter.formatTaka(workerSummary.totalMoneyTaken)}) = বাকি পাবে"
                                } else {
                                    "কামাইয়ের চেয়ে বেশি টাকা নেওয়া হয়েছে (আগামী মাসে সমন্বয় হবে)"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = CurrencyFormatter.formatTaka(kotlin.math.abs(workerSummary.balanceDue)),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = if (isDue) DueAmber else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Quick Add Advance / Payment Button
            Button(
                onClick = onAddPaymentClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KhataTertiary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("worker_detail_add_payment_button")
            ) {
                Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "টাকা পরিশোধ / অগ্রিম দিন (+ Add Payment)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// Sub-components: Line-by-Line Khata Register Table Header & Rows
// -------------------------------------------------------------------------

@Composable
fun KhataRegisterHeaderRow(
    showOt: Boolean,
    currencySymbol: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date & Day column
            Text(
                text = "Date & Day",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(78.dp)
            )

            // Hajira column
            Text(
                text = "Hajira",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(68.dp)
            )

            if (showOt) {
                // OT column
                Text(
                    text = "OT (hrs)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(52.dp)
                )
            }

            // Advance column
            Text(
                text = "Advance",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )

            // Earned column
            Text(
                text = "Earned",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun KhataRegisterDayLine(
    dayRow: KhataDayRow,
    dailyWage: Double,
    showOt: Boolean,
    isHighlighted: Boolean,
    onLineClick: () -> Unit,
    onQuickStatusChange: (String) -> Unit
) {
    val isSunday = dayRow.isSunday
    val isToday = dayRow.isToday

    val rowBg = when {
        isHighlighted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        isToday -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
        isSunday -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.12f)
        dayRow.dayNumber % 2 == 0 -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
    }

    Surface(
        color = rowBg,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLineClick() }
            .testTag("khata_row_${dayRow.dayNumber}")
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Date & Day (01 Mon, 07 Sun)
                Row(
                    modifier = Modifier.width(78.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isToday) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }

                    Column {
                        Text(
                            text = "${String.format("%02d", dayRow.dayNumber)} ${dayRow.dayOfWeekShort}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSunday || isToday) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSunday) AbsentRed else MaterialTheme.colorScheme.onSurface
                        )
                        if (isToday) {
                            Text(
                                text = "Today",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 2. Hajira Status Button / Badge
                Box(
                    modifier = Modifier.width(74.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (dayRow.status) {
                        "Present", "1.0" -> Surface(
                            color = PresentGreen.copy(alpha = 0.18f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PresentGreen),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { onQuickStatusChange("OneAndHalf") }
                        ) {
                            Text(
                                text = "1.0 • পুরো",
                                color = PresentGreen,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp)
                            )
                        }

                        "OneAndHalf", "1.5" -> Surface(
                            color = KhataPrimary.copy(alpha = 0.18f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, KhataPrimary),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { onQuickStatusChange("Half") }
                        ) {
                            Text(
                                text = "1.5 • দেড়",
                                color = KhataPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp)
                            )
                        }

                        "Half", "0.5" -> Surface(
                            color = HalfOrange.copy(alpha = 0.18f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HalfOrange),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { onQuickStatusChange("Double") }
                        ) {
                            Text(
                                text = "0.5 • হাফ",
                                color = HalfOrange,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp)
                            )
                        }

                        "Double", "2.0" -> Surface(
                            color = Color(0xFF1E88E5).copy(alpha = 0.18f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E88E5)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { onQuickStatusChange("Absent") }
                        ) {
                            Text(
                                text = "2.0 • ডবল",
                                color = Color(0xFF1E88E5),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp)
                            )
                        }

                        "Absent" -> Surface(
                            color = AbsentRed.copy(alpha = 0.18f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AbsentRed),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { onQuickStatusChange("Present") }
                        ) {
                            Text(
                                text = "A • ছুটি",
                                color = AbsentRed,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp)
                            )
                        }

                        else -> Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { onQuickStatusChange("Present") }
                        ) {
                            Text(
                                text = "+ হাজিরা",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // 3. Overtime
                if (showOt) {
                    Box(
                        modifier = Modifier.width(52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayRow.overtimeHours > 0) {
                            Text(
                                text = "${dayRow.overtimeHours}h",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = DueAmber
                            )
                        } else {
                            Text(
                                text = "—",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // 4. Advance Taken (নিলো)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (dayRow.totalAdvance > 0) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "-${CurrencyFormatter.formatTaka(dayRow.totalAdvance)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = KhataTertiary
                            )
                            if (dayRow.payments.isNotEmpty() && dayRow.payments[0].note.isNotBlank()) {
                                Text(
                                    text = dayRow.payments[0].note,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "—",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                // 5. Total Day Earned (কামাই)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (dayRow.totalEarned > 0) {
                        Text(
                            text = "+${CurrencyFormatter.formatTaka(dayRow.totalEarned)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = PresentGreen
                        )
                    } else {
                        Text(
                            text = CurrencyFormatter.formatTaka(0.0),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
        }
    }
}

// -------------------------------------------------------------------------
// Sub-components: Interactive Khata Day Entry Dialog (When tapping any day)
// -------------------------------------------------------------------------

@Composable
fun KhataDayEntryDialog(
    dayRow: KhataDayRow,
    dailyWage: Double,
    standardWorkHours: Int,
    onDismiss: () -> Unit,
    onSave: (status: String?, overtime: Double, advanceAmount: Double, advanceNote: String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(dayRow.status) }
    var overtimeHours by remember { mutableDoubleStateOf(dayRow.overtimeHours) }
    var advanceText by remember { mutableStateOf("") }
    var advanceNote by remember { mutableStateOf("") }

    val baseEarned = when (selectedStatus) {
        "Present", "1.0" -> dailyWage * 1.0
        "OneAndHalf", "1.5" -> dailyWage * 1.5
        "Double", "2.0" -> dailyWage * 2.0
        "Half", "0.5" -> dailyWage * 0.5
        else -> 0.0
    }
    val otEarned = if (overtimeHours > 0 && standardWorkHours > 0) {
        (dailyWage / standardWorkHours) * overtimeHours
    } else 0.0
    val totalEstimatedEarned = baseEarned + otEarned

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Khata Entry: Day ${dayRow.dayNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${dayRow.dayOfWeekFull}, ${KhataDateUtils.formatDisplayDate(dayRow.isoDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Attendance Status Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Attendance (হাজিরা নির্বাচন):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Row 1: 1.0 (Full), 1.5 (One and half), 0.5 (Half)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 1.0 Present Button
                        val is10 = selectedStatus == "Present" || selectedStatus == "1.0"
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (is10) PresentGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (is10) androidx.compose.foundation.BorderStroke(1.5.dp, PresentGreen) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedStatus = "Present" }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "১.০ হাজিরা",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (is10) PresentGreen else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Full Day",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = if (is10) PresentGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // 1.5 One and Half Button
                        val is15 = selectedStatus == "OneAndHalf" || selectedStatus == "1.5"
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (is15) KhataPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (is15) androidx.compose.foundation.BorderStroke(1.5.dp, KhataPrimary) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedStatus = "OneAndHalf" }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "১.৫ হাজিরা",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (is15) KhataPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "1.5x Day",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = if (is15) KhataPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // 0.5 Half Day Button
                        val is05 = selectedStatus == "Half" || selectedStatus == "0.5"
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (is05) HalfOrange.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (is05) androidx.compose.foundation.BorderStroke(1.5.dp, HalfOrange) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedStatus = "Half" }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "০.৫ হাজিরা",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (is05) HalfOrange else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Half Day",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = if (is05) HalfOrange else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Row 2: 2.0 (Double), Absent (ছুটি), Clear (✕)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 2.0 Double Day Button
                        val is20 = selectedStatus == "Double" || selectedStatus == "2.0"
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (is20) Color(0xFF1E88E5).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (is20) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF1E88E5)) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedStatus = "Double" }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "২.০ হাজিরা",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (is20) Color(0xFF1E88E5) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Double (2x)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = if (is20) Color(0xFF1E88E5) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Absent Button
                        val isAbsent = selectedStatus == "Absent"
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isAbsent) AbsentRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isAbsent) androidx.compose.foundation.BorderStroke(1.5.dp, AbsentRed) else null,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedStatus = "Absent" }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = "ছুটি (A)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAbsent) AbsentRed else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Absent",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = if (isAbsent) AbsentRed else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Clear Button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedStatus == null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { selectedStatus = null }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Status",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Overtime Stepper
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Overtime (ওভারটাইম):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$overtimeHours hrs (+${CurrencyFormatter.formatTaka(otEarned)})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = DueAmber
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(0.0, 1.0, 1.5, 2.0, 3.0, 4.0).forEach { hrs ->
                            val isSelected = (overtimeHours == hrs)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) DueAmber.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, DueAmber) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { overtimeHours = hrs }
                            ) {
                                Text(
                                    text = if (hrs == 0.0) "0" else "${hrs}h",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) DueAmber else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Cash Advance Taken for this Date
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Add Cash Advance Taken (টাকা নিলো):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = advanceText,
                        onValueChange = { advanceText = it },
                        label = { Text("Advance Amount (${CurrencyFormatter.defaultCurrencySymbol})") },
                        placeholder = { Text("e.g. 500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = advanceNote,
                        onValueChange = { advanceNote = it },
                        label = { Text("Note / Purpose (optional)") },
                        placeholder = { Text("e.g. Bazar khoroch, Medicine") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Calculation Preview Box
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estimated Day Earned:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = CurrencyFormatter.formatTaka(totalEstimatedEarned),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PresentGreen
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val adv = advanceText.toDoubleOrNull() ?: 0.0
                    onSave(selectedStatus, overtimeHours, adv, advanceNote)
                },
                colors = ButtonDefaults.buttonColors(containerColor = KhataPrimary)
            ) {
                Text("Save to Khata")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
