package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Worker
import com.example.data.settings.AppSettings
import com.example.ui.locale.LocalAppStrings
import com.example.ui.model.CurrencyFormatter
import com.example.ui.model.KhataDateUtils
import com.example.ui.model.WorkerMonthlySummary
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.AdvancePurple
import com.example.ui.theme.DueAmber
import com.example.ui.theme.HalfOrange
import com.example.ui.theme.KhataPrimary
import com.example.ui.theme.KhataTertiary
import com.example.ui.theme.LedgerGold
import com.example.ui.theme.PresentGreen
import kotlin.math.max

enum class ChartViewType(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    ALL("All Charts", Icons.Default.Assessment),
    ATTENDANCE("Attendance", Icons.Default.DateRange),
    ADVANCES_VS_EARNED("Advances vs Wages", Icons.Default.BarChart),
    PENDING_DUES("Pending Dues", Icons.Default.AccountBalanceWallet),
    OVERVIEW_DONUT("Overall Donut", Icons.Default.PieChart)
}

enum class SummarySortType(val label: String) {
    PENDING_DUES_HIGH("Dues (High to Low)"),
    ATTENDANCE_HIGH("Attendance (High to Low)"),
    ADVANCES_HIGH("Advances (High to Low)"),
    NAME_AZ("Name (A to Z)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlySummaryScreen(
    workerSummaries: List<WorkerMonthlySummary>,
    selectedYear: Int,
    selectedMonth: Int,
    onMonthChange: (Int, Int) -> Unit,
    onWorkerClick: (Int) -> Unit,
    onAddPaymentClick: (Int) -> Unit = {},
    settings: AppSettings = AppSettings(),
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    var selectedChartTab by remember { mutableIntStateOf(0) }
    var selectedSortType by remember { mutableStateOf(SummarySortType.PENDING_DUES_HIGH) }
    var searchQuery by remember { mutableStateOf("") }
    var showMonthPicker by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Overall Aggregate Metrics for Selected Month
    val totalHajira = remember(workerSummaries) { workerSummaries.sumOf { it.totalHajira } }
    val totalPresentDays = remember(workerSummaries) { workerSummaries.sumOf { it.presentDays } }
    val totalHalfDays = remember(workerSummaries) { workerSummaries.sumOf { it.halfDays } }
    val totalAbsentDays = remember(workerSummaries) { workerSummaries.sumOf { it.absentDays } }
    val totalGrossEarned = remember(workerSummaries) { workerSummaries.sumOf { it.totalEarned } }
    val totalAdvancesTaken = remember(workerSummaries) { workerSummaries.sumOf { it.totalMoneyTaken } }
    val totalPendingDues = remember(workerSummaries) { workerSummaries.sumOf { it.balanceDue } }
    val totalOvertimeHours = remember(workerSummaries) { workerSummaries.sumOf { it.totalOvertimeHours } }
    val totalOvertimeWage = remember(workerSummaries) { workerSummaries.sumOf { it.totalOvertimeWage } }

    // Filter & Sort worker list
    val sortedAndFilteredWorkers = remember(workerSummaries, searchQuery, selectedSortType) {
        val filtered = if (searchQuery.isBlank()) {
            workerSummaries
        } else {
            val q = searchQuery.trim()
            workerSummaries.filter {
                it.worker.name.contains(q, ignoreCase = true) ||
                it.worker.phone.contains(q, ignoreCase = true)
            }
        }

        when (selectedSortType) {
            SummarySortType.PENDING_DUES_HIGH -> filtered.sortedByDescending { it.balanceDue }
            SummarySortType.ATTENDANCE_HIGH -> filtered.sortedByDescending { it.totalHajira }
            SummarySortType.ADVANCES_HIGH -> filtered.sortedByDescending { it.totalMoneyTaken }
            SummarySortType.NAME_AZ -> filtered.sortedBy { it.worker.name.lowercase() }
        }
    }

    // Maximum values for proportional bar scaling
    val maxAttendanceDays = remember(workerSummaries) {
        max(1.0, workerSummaries.maxOfOrNull { it.totalHajira } ?: 1.0)
    }
    val maxEarnings = remember(workerSummaries) {
        max(1.0, workerSummaries.maxOfOrNull { max(it.totalEarned, it.totalMoneyTaken) } ?: 1.0)
    }
    val maxPendingDue = remember(workerSummaries) {
        max(1.0, workerSummaries.maxOfOrNull { it.balanceDue } ?: 1.0)
    }

    // Share report function
    fun shareMonthlyReport() {
        val monthName = KhataDateUtils.getMonthName(selectedMonth)
        val text = buildString {
            appendLine("=======================================")
            appendLine("📊 MONTHLY SUMMARY REPORT (মাসিক হিসাব)")
            appendLine("Period: $monthName $selectedYear")
            appendLine("Total Workers: ${workerSummaries.size}")
            appendLine("---------------------------------------")
            appendLine("• Total Working Days: ${CurrencyFormatter.formatDays(totalHajira)}")
            appendLine("• Total Present: $totalPresentDays | Half: $totalHalfDays | Absent: $totalAbsentDays")
            if (totalOvertimeHours > 0) {
                appendLine("• Total Overtime: $totalOvertimeHours hrs (+${CurrencyFormatter.formatTaka(totalOvertimeWage)})")
            }
            appendLine("---------------------------------------")
            appendLine("• Total Wages Earned: ${CurrencyFormatter.formatTaka(totalGrossEarned)}")
            appendLine("• Total Advances Paid: ${CurrencyFormatter.formatTaka(totalAdvancesTaken)}")
            appendLine("• Net Pending Dues: ${CurrencyFormatter.formatTaka(totalPendingDues)}")
            appendLine("=======================================")
            appendLine("WORKER-WISE SUMMARY (কর্মী অনুযায়ী হিসাব):")
            workerSummaries.forEachIndexed { i, s ->
                val dueSign = if (s.balanceDue >= 0) "Due" else "Overpaid"
                appendLine("${i + 1}. ${s.worker.name}: Days: ${CurrencyFormatter.formatDays(s.totalHajira)} | Earned: ${CurrencyFormatter.formatTaka(s.totalEarned)} | Adv: ${CurrencyFormatter.formatTaka(s.totalMoneyTaken)} | $dueSign: ${CurrencyFormatter.formatTaka(kotlin.math.abs(s.balanceDue))}")
            }
            appendLine("=======================================")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Monthly Summary Report"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = strings.summaryScreenTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${KhataDateUtils.getMonthName(selectedMonth)} $selectedYear • Analytics",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { shareMonthlyReport() },
                        modifier = Modifier.testTag("summary_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = strings.shareReportTooltip,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("monthly_summary_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Month Picker Navigator Bar
            item {
                MonthlyNavigationPicker(
                    selectedYear = selectedYear,
                    selectedMonth = selectedMonth,
                    showMonthPicker = showMonthPicker,
                    onToggleMonthPicker = { showMonthPicker = it },
                    onMonthChange = onMonthChange
                )
            }

            // 2. High-Level Monthly Totals Summary (KPI Cards)
            item {
                MonthlyOverviewCards(
                    totalHajira = totalHajira,
                    presentDays = totalPresentDays,
                    halfDays = totalHalfDays,
                    absentDays = totalAbsentDays,
                    totalEarned = totalGrossEarned,
                    totalAdvances = totalAdvancesTaken,
                    totalPendingDues = totalPendingDues,
                    totalWorkers = workerSummaries.size
                )
            }

            // 3. Interactive Chart Mode Tabs
            item {
                PrimaryTabRow(
                    selectedTabIndex = selectedChartTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ChartViewType.entries.forEachIndexed { index, type ->
                        Tab(
                            selected = selectedChartTab == index,
                            onClick = { selectedChartTab = index },
                            text = {
                                Text(
                                    text = type.label,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = if (selectedChartTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = type.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }

            // 4. Chart Content based on tab
            when (selectedChartTab) {
                0 -> {
                    // ALL CHARTS COMBINED IN SECTIONS
                    item {
                        DonutOverviewCard(
                            totalEarned = totalGrossEarned,
                            totalAdvances = totalAdvancesTaken,
                            totalPendingDues = totalPendingDues,
                            totalOvertimeWage = totalOvertimeWage
                        )
                    }

                    item {
                        ChartSectionHeader(
                            title = "1. Attendance Breakdown per Worker",
                            subtitle = "Full Days, Half Days, and Absent Days comparison",
                            icon = Icons.Default.DateRange
                        )
                    }
                    items(sortedAndFilteredWorkers, key = { "att_${it.worker.id}" }) { summary ->
                        AttendanceBarCard(
                            summary = summary,
                            maxDays = maxAttendanceDays,
                            onClick = { onWorkerClick(summary.worker.id) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        ChartSectionHeader(
                            title = "2. Earnings vs Advances Comparison",
                            subtitle = "Gross wage earned vs advance money taken",
                            icon = Icons.Default.BarChart
                        )
                    }
                    items(sortedAndFilteredWorkers, key = { "adv_${it.worker.id}" }) { summary ->
                        EarningsVsAdvanceBarCard(
                            summary = summary,
                            maxAmount = maxEarnings,
                            onClick = { onWorkerClick(summary.worker.id) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        ChartSectionHeader(
                            title = "3. Pending Dues Balance Chart (বাকি পাওনা)",
                            subtitle = "Unsettled payable balances per worker",
                            icon = Icons.Default.AccountBalanceWallet
                        )
                    }
                    items(sortedAndFilteredWorkers, key = { "due_${it.worker.id}" }) { summary ->
                        PendingDueBarCard(
                            summary = summary,
                            maxDue = maxPendingDue,
                            onClick = { onWorkerClick(summary.worker.id) },
                            onPayClick = { onAddPaymentClick(summary.worker.id) }
                        )
                    }
                }

                1 -> {
                    // ATTENDANCE TAB
                    item {
                        SearchAndSortFilterRow(
                            searchQuery = searchQuery,
                            onSearchChange = { searchQuery = it },
                            selectedSort = selectedSortType,
                            onSelectSort = { selectedSortType = it },
                            showSortMenu = showSortMenu,
                            onToggleSortMenu = { showSortMenu = it }
                        )
                    }
                    item {
                        AttendanceLegendBar()
                    }
                    items(sortedAndFilteredWorkers, key = { it.worker.id }) { summary ->
                        AttendanceBarCard(
                            summary = summary,
                            maxDays = maxAttendanceDays,
                            onClick = { onWorkerClick(summary.worker.id) }
                        )
                    }
                }

                2 -> {
                    // ADVANCES VS EARNED TAB
                    item {
                        SearchAndSortFilterRow(
                            searchQuery = searchQuery,
                            onSearchChange = { searchQuery = it },
                            selectedSort = selectedSortType,
                            onSelectSort = { selectedSortType = it },
                            showSortMenu = showSortMenu,
                            onToggleSortMenu = { showSortMenu = it }
                        )
                    }
                    item {
                        EarningsVsAdvanceLegendBar()
                    }
                    items(sortedAndFilteredWorkers, key = { it.worker.id }) { summary ->
                        EarningsVsAdvanceBarCard(
                            summary = summary,
                            maxAmount = maxEarnings,
                            onClick = { onWorkerClick(summary.worker.id) }
                        )
                    }
                }

                3 -> {
                    // PENDING DUES TAB
                    item {
                        SearchAndSortFilterRow(
                            searchQuery = searchQuery,
                            onSearchChange = { searchQuery = it },
                            selectedSort = selectedSortType,
                            onSelectSort = { selectedSortType = it },
                            showSortMenu = showSortMenu,
                            onToggleSortMenu = { showSortMenu = it }
                        )
                    }
                    item {
                        PendingDuesLegendBar()
                    }
                    items(sortedAndFilteredWorkers, key = { it.worker.id }) { summary ->
                        PendingDueBarCard(
                            summary = summary,
                            maxDue = maxPendingDue,
                            onClick = { onWorkerClick(summary.worker.id) },
                            onPayClick = { onAddPaymentClick(summary.worker.id) }
                        )
                    }
                }

                4 -> {
                    // OVERVIEW DONUT TAB
                    item {
                        DonutOverviewCard(
                            totalEarned = totalGrossEarned,
                            totalAdvances = totalAdvancesTaken,
                            totalPendingDues = totalPendingDues,
                            totalOvertimeWage = totalOvertimeWage
                        )
                    }
                    item {
                        MonthFinancialBreakdownTable(
                            totalEarned = totalGrossEarned,
                            totalAdvances = totalAdvancesTaken,
                            totalPendingDues = totalPendingDues,
                            totalOvertimeWage = totalOvertimeWage,
                            workerCount = workerSummaries.size
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// Sub-components: Month Navigator Bar
// -------------------------------------------------------------------------

@Composable
fun MonthlyNavigationPicker(
    selectedYear: Int,
    selectedMonth: Int,
    showMonthPicker: Boolean,
    onToggleMonthPicker: (Boolean) -> Unit,
    onMonthChange: (Int, Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (selectedMonth == 1) onMonthChange(selectedYear - 1, 12)
                    else onMonthChange(selectedYear, selectedMonth - 1)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Month",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Box {
                Surface(
                    onClick = { onToggleMonthPicker(true) },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.testTag("summary_month_picker")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${KhataDateUtils.getMonthName(selectedMonth)} $selectedYear",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                DropdownMenu(
                    expanded = showMonthPicker,
                    onDismissRequest = { onToggleMonthPicker(false) }
                ) {
                    KhataDateUtils.monthNames.forEachIndexed { index, monthName ->
                        val mNum = index + 1
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "$monthName $selectedYear",
                                    fontWeight = if (mNum == selectedMonth) FontWeight.Bold else FontWeight.Normal,
                                    color = if (mNum == selectedMonth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onMonthChange(selectedYear, mNum)
                                onToggleMonthPicker(false)
                            }
                        )
                    }
                }
            }

            IconButton(
                onClick = {
                    if (selectedMonth == 12) onMonthChange(selectedYear + 1, 1)
                    else onMonthChange(selectedYear, selectedMonth + 1)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Month",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// Sub-components: Monthly Overview Cards (KPIs)
// -------------------------------------------------------------------------

@Composable
fun MonthlyOverviewCards(
    totalHajira: Double,
    presentDays: Int,
    halfDays: Int,
    absentDays: Int,
    totalEarned: Double,
    totalAdvances: Double,
    totalPendingDues: Double,
    totalWorkers: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Row 1: Total Working Days & Total Workers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryMetricCard(
                title = "Total Attendance",
                value = "${CurrencyFormatter.formatDays(totalHajira)} Days",
                subtitle = "$presentDays Full • $halfDays Half • $absentDays Absent",
                accentColor = PresentGreen,
                icon = Icons.Default.DateRange,
                modifier = Modifier.weight(1f)
            )

            SummaryMetricCard(
                title = "Total Wages Earned",
                value = CurrencyFormatter.formatTaka(totalEarned),
                subtitle = "Across $totalWorkers active workers",
                accentColor = MaterialTheme.colorScheme.primary,
                icon = Icons.Default.Payments,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Advances Paid & Net Pending Dues
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryMetricCard(
                title = "Advances Disbursed",
                value = CurrencyFormatter.formatTaka(totalAdvances),
                subtitle = "Cash loans taken",
                accentColor = KhataTertiary,
                icon = Icons.Default.AccountBalanceWallet,
                modifier = Modifier.weight(1f)
            )

            SummaryMetricCard(
                title = "Net Pending Dues",
                value = CurrencyFormatter.formatTaka(totalPendingDues),
                subtitle = "Payable balance due",
                accentColor = DueAmber,
                icon = Icons.Default.BarChart,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SummaryMetricCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(accentColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// -------------------------------------------------------------------------
// Sub-components: Search & Sort Filter Row
// -------------------------------------------------------------------------

@Composable
fun SearchAndSortFilterRow(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedSort: SummarySortType,
    onSelectSort: (SummarySortType) -> Unit,
    showSortMenu: Boolean,
    onToggleSortMenu: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search worker...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
        )

        Box {
            Surface(
                onClick = { onToggleSortMenu(true) },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.height(50.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "Sort",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sort",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { onToggleSortMenu(false) }
            ) {
                SummarySortType.entries.forEach { sortOption ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = sortOption.label,
                                fontWeight = if (selectedSort == sortOption) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedSort == sortOption) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            onSelectSort(sortOption)
                            onToggleSortMenu(false)
                        }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// Sub-components: Chart 1 - Attendance Breakdown Stacked Bar Card
// -------------------------------------------------------------------------

@Composable
fun AttendanceLegendBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(color = PresentGreen, label = "Present (P)")
        LegendItem(color = HalfOrange, label = "Half Day (H)")
        LegendItem(color = AbsentRed, label = "Absent (A)")
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AttendanceBarCard(
    summary: WorkerMonthlySummary,
    maxDays: Double,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("attendance_chart_card_${summary.worker.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Worker Name, Daily Wage, Total Days
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = summary.worker.name.take(1).uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column {
                        Text(
                            text = summary.worker.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${CurrencyFormatter.formatTaka(summary.worker.dailyWage)}/day",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${CurrencyFormatter.formatDays(summary.totalHajira)} Days",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = PresentGreen
                    )
                    Text(
                        text = "${summary.presentDays}P • ${summary.halfDays}H • ${summary.absentDays}A",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }

            // Stacked Multi-Color Attendance Bar (Canvas)
            val totalLogged = summary.presentDays + summary.halfDays + summary.absentDays
            val safeTotal = if (totalLogged == 0) 1f else totalLogged.toFloat()

            val presentFraction = (summary.presentDays / safeTotal).coerceIn(0f, 1f)
            val halfFraction = (summary.halfDays / safeTotal).coerceIn(0f, 1f)
            val absentFraction = (summary.absentDays / safeTotal).coerceIn(0f, 1f)

            val animatedPresent by animateFloatAsState(targetValue = presentFraction, animationSpec = tween(600), label = "p")
            val animatedHalf by animateFloatAsState(targetValue = halfFraction, animationSpec = tween(600), label = "h")
            val animatedAbsent by animateFloatAsState(targetValue = absentFraction, animationSpec = tween(600), label = "a")

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                val barWidth = size.width
                val barHeight = size.height

                val pWidth = barWidth * animatedPresent
                val hWidth = barWidth * animatedHalf
                val aWidth = barWidth * animatedAbsent

                var currentX = 0f

                // Present Segment
                if (pWidth > 0) {
                    drawRect(
                        color = PresentGreen,
                        topLeft = Offset(currentX, 0f),
                        size = Size(pWidth, barHeight)
                    )
                    currentX += pWidth
                }

                // Half Day Segment
                if (hWidth > 0) {
                    drawRect(
                        color = HalfOrange,
                        topLeft = Offset(currentX, 0f),
                        size = Size(hWidth, barHeight)
                    )
                    currentX += hWidth
                }

                // Absent Segment
                if (aWidth > 0) {
                    drawRect(
                        color = AbsentRed.copy(alpha = 0.85f),
                        topLeft = Offset(currentX, 0f),
                        size = Size(aWidth, barHeight)
                    )
                }
            }

            // Overtime indicator if overtime logged
            if (summary.totalOvertimeHours > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "⏱️ Overtime:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = DueAmber
                    )
                    Text(
                        text = "${summary.totalOvertimeHours} hrs (+${CurrencyFormatter.formatTaka(summary.totalOvertimeWage)})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = DueAmber
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// Sub-components: Chart 2 - Earnings vs Advances Dual Bar Card
// -------------------------------------------------------------------------

@Composable
fun EarningsVsAdvanceLegendBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(color = KhataPrimary, label = "Total Wage Earned (কামাই)")
        LegendItem(color = KhataTertiary, label = "Advances Taken (অগ্রিম)")
    }
}

@Composable
fun EarningsVsAdvanceBarCard(
    summary: WorkerMonthlySummary,
    maxAmount: Double,
    onClick: () -> Unit
) {
    val earnedFraction = ((summary.totalEarned / maxAmount).toFloat()).coerceIn(0f, 1f)
    val advanceFraction = ((summary.totalMoneyTaken / maxAmount).toFloat()).coerceIn(0f, 1f)

    val animatedEarned by animateFloatAsState(targetValue = earnedFraction, animationSpec = tween(600), label = "earned")
    val animatedAdvance by animateFloatAsState(targetValue = advanceFraction, animationSpec = tween(600), label = "adv")

    val advancePercentage = if (summary.totalEarned > 0) {
        ((summary.totalMoneyTaken / summary.totalEarned) * 100).toInt()
    } else 0

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("earnings_vs_advance_card_${summary.worker.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Worker header & advance % badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = summary.worker.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Net Payable: ${CurrencyFormatter.formatTaka(summary.balanceDue)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (summary.balanceDue >= 0) DueAmber else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (advancePercentage > 80) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "$advancePercentage% Taken",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (advancePercentage > 80) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Bar 1: Total Earned (Green)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Earned: ${CurrencyFormatter.formatTaka(summary.totalEarned)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = KhataPrimary
                    )
                    Text(
                        text = "${CurrencyFormatter.formatDays(summary.totalHajira)} Days",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            listOf(KhataPrimary, PresentGreen)
                        ),
                        size = Size(size.width * animatedEarned, size.height)
                    )
                }
            }

            // Bar 2: Advances Taken (Amber / Tertiary)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Advance: ${CurrencyFormatter.formatTaka(summary.totalMoneyTaken)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = KhataTertiary
                    )
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            listOf(KhataTertiary, DueAmber)
                        ),
                        size = Size(size.width * animatedAdvance, size.height)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// Sub-components: Chart 3 - Pending Dues Leaderboard Bar Card
// -------------------------------------------------------------------------

@Composable
fun PendingDuesLegendBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(color = DueAmber, label = "Pending Due (বাকি পাওনা)")
        LegendItem(color = MaterialTheme.colorScheme.error, label = "Overpaid Advance (অতিরিক্ত টাকা)")
    }
}

@Composable
fun PendingDueBarCard(
    summary: WorkerMonthlySummary,
    maxDue: Double,
    onClick: () -> Unit,
    onPayClick: () -> Unit
) {
    val isDue = summary.balanceDue >= 0
    val dueAmount = kotlin.math.abs(summary.balanceDue)
    val fraction = ((dueAmount / maxDue).toFloat()).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(targetValue = fraction, animationSpec = tween(600), label = "due")

    val barColor = if (isDue) DueAmber else MaterialTheme.colorScheme.error

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("pending_due_card_${summary.worker.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = summary.worker.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isDue) "Earned: ${CurrencyFormatter.formatTaka(summary.totalEarned)} • Adv: ${CurrencyFormatter.formatTaka(summary.totalMoneyTaken)}"
                               else "Overpaid by ${CurrencyFormatter.formatTaka(dueAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = CurrencyFormatter.formatTaka(dueAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = barColor
                    )

                    Surface(
                        onClick = onPayClick,
                        shape = RoundedCornerShape(8.dp),
                        color = KhataTertiary.copy(alpha = 0.15f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = "Add Payment",
                                tint = KhataTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Proportional gradient bar
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (animatedFraction > 0) {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            listOf(barColor.copy(alpha = 0.7f), barColor)
                        ),
                        size = Size(size.width * animatedFraction, size.height)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// Sub-components: Chart 4 - Donut Chart & Financial Breakdown
// -------------------------------------------------------------------------

@Composable
fun DonutOverviewCard(
    totalEarned: Double,
    totalAdvances: Double,
    totalPendingDues: Double,
    totalOvertimeWage: Double
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Month Financial Overview (হিসাবের পাই চার্ট)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            val safeEarned = if (totalEarned <= 0.0) 1.0 else totalEarned
            val pendingDuesClamped = max(0.0, totalPendingDues)
            val advancesClamped = max(0.0, totalAdvances)
            val overtimeClamped = max(0.0, totalOvertimeWage)

            val totalSlices = pendingDuesClamped + advancesClamped + overtimeClamped
            val safeTotal = if (totalSlices <= 0.0) 1.0 else totalSlices

            val pendingAngle = ((pendingDuesClamped / safeTotal) * 360f).toFloat()
            val advancesAngle = ((advancesClamped / safeTotal) * 360f).toFloat()
            val overtimeAngle = ((overtimeClamped / safeTotal) * 360f).toFloat()

            // Donut Canvas + Center Text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier.size(160.dp)
                ) {
                    val strokeWidth = 28.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val arcSize = Size(diameter, diameter)
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    var startAngle = -90f

                    // Advances Sector (Tertiary)
                    if (advancesAngle > 0f) {
                        drawArc(
                            color = KhataTertiary,
                            startAngle = startAngle,
                            sweepAngle = advancesAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth)
                        )
                        startAngle += advancesAngle
                    }

                    // Pending Dues Sector (Amber)
                    if (pendingAngle > 0f) {
                        drawArc(
                            color = DueAmber,
                            startAngle = startAngle,
                            sweepAngle = pendingAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth)
                        )
                        startAngle += pendingAngle
                    }

                    // Overtime Sector (Green / Accent)
                    if (overtimeAngle > 0f) {
                        drawArc(
                            color = PresentGreen,
                            startAngle = startAngle,
                            sweepAngle = overtimeAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth)
                        )
                    }

                    // Empty placeholder if 0
                    if (totalSlices <= 0.0) {
                        drawArc(
                            color = Color.LightGray.copy(alpha = 0.5f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth)
                        )
                    }
                }

                // Center Label
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total Gross",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatTaka(totalEarned),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Donut Legend with amounts and percentages
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                DonutLegendChip(
                    color = DueAmber,
                    title = "Pending Dues",
                    amount = CurrencyFormatter.formatTaka(totalPendingDues),
                    percentage = "${((pendingDuesClamped / safeTotal) * 100).toInt()}%"
                )

                DonutLegendChip(
                    color = KhataTertiary,
                    title = "Advances Paid",
                    amount = CurrencyFormatter.formatTaka(totalAdvances),
                    percentage = "${((advancesClamped / safeTotal) * 100).toInt()}%"
                )

                DonutLegendChip(
                    color = PresentGreen,
                    title = "Overtime Wages",
                    amount = CurrencyFormatter.formatTaka(totalOvertimeWage),
                    percentage = "${((overtimeClamped / safeTotal) * 100).toInt()}%"
                )
            }
        }
    }
}

@Composable
fun DonutLegendChip(
    color: Color,
    title: String,
    amount: String,
    percentage: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
        Text(
            text = amount,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = percentage,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}

@Composable
fun MonthFinancialBreakdownTable(
    totalEarned: Double,
    totalAdvances: Double,
    totalPendingDues: Double,
    totalOvertimeWage: Double,
    workerCount: Int
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Detailed Financial Summary",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            BreakdownRow("Total Active Workers", "$workerCount Workers")
            BreakdownRow("Gross Wages (Daily Rates)", CurrencyFormatter.formatTaka(totalEarned - totalOvertimeWage))
            BreakdownRow("Overtime Pay", "+${CurrencyFormatter.formatTaka(totalOvertimeWage)}")
            BreakdownRow("Total Wages Payable", CurrencyFormatter.formatTaka(totalEarned), isBold = true)
            BreakdownRow("Less: Total Cash Advances", "-${CurrencyFormatter.formatTaka(totalAdvances)}", color = KhataTertiary)
            HorizontalDivider()
            BreakdownRow(
                label = "Net Balance Due (বাকি পাওনা)",
                value = CurrencyFormatter.formatTaka(totalPendingDues),
                isBold = true,
                color = DueAmber
            )
        }
    }
}

@Composable
fun BreakdownRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Medium,
            color = color
        )
    }
}

@Composable
fun ChartSectionHeader(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
