package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Worker
import com.example.ui.components.AttendanceToggleGroup
import com.example.ui.components.DateSelectorBar
import com.example.ui.components.StatusBadge
import com.example.ui.components.SummaryStatCard
import com.example.ui.locale.LocalAppStrings
import com.example.ui.model.CurrencyFormatter
import com.example.ui.model.KhataDateUtils
import com.example.ui.model.WorkerMonthlySummary
import com.example.ui.theme.DueAmber
import com.example.ui.theme.KhataPrimary
import com.example.ui.theme.KhataTertiary
import com.example.ui.theme.PresentGreen

@Composable
fun HomeScreen(
    selectedDate: String,
    selectedYear: Int,
    selectedMonth: Int,
    workerSummaries: List<WorkerMonthlySummary>,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onDateSelect: (String) -> Unit,
    onMonthSelect: (Int, Int) -> Unit,
    onStatusChange: (workerId: Int, status: String) -> Unit,
    onWorkerClick: (Int) -> Unit,
    onAddWorkerClick: () -> Unit,
    onAddPaymentClick: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToSettings: (() -> Unit)? = null,
    onNavigateToSummary: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val totalWorkers = workerSummaries.size
    val presentCount = workerSummaries.count { it.todayStatus == "Present" }
    val halfCount = workerSummaries.count { it.todayStatus == "Half" }
    val absentCount = workerSummaries.count { it.todayStatus == "Absent" }
    val todayEarned = workerSummaries.sumOf { it.todayWage }

    val monthTotalEarned = workerSummaries.sumOf { it.totalEarned }
    val monthTotalTaken = workerSummaries.sumOf { it.totalMoneyTaken }
    val monthTotalDue = workerSummaries.sumOf { it.balanceDue }

    var searchQuery by remember { mutableStateOf("") }

    val filteredSummaries = remember(workerSummaries, searchQuery) {
        if (searchQuery.isBlank()) {
            workerSummaries
        } else {
            val query = searchQuery.trim()
            workerSummaries.filter {
                it.worker.name.contains(query, ignoreCase = true) ||
                it.worker.phone.contains(query, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Title Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.homeAppTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = strings.homeAppSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onNavigateToSummary != null) {
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable { onNavigateToSummary() }
                                    .testTag("home_header_summary_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.BarChart,
                                        contentDescription = "Visual Charts & Summary",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        if (onNavigateToSettings != null) {
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable { onNavigateToSettings() }
                                    .testTag("home_header_settings_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.HowToReg,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search Bar at Top of Home Screen
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = strings.searchWorkerPlaceholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.testTag("home_search_clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_search_bar")
            )
        }

        // Calendar Date & Month Bar
        item {
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

        // Quick Stats Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryStatCard(
                        title = strings.todayRegisterHeader,
                        value = "$presentCount ${strings.todayPresentLabel}",
                        subtitle = if (halfCount > 0) "+$halfCount ${strings.todayHalfDayLabel} | $absentCount ${strings.todayAbsentLabel}" else "$absentCount ${strings.todayAbsentLabel}",
                        icon = Icons.Default.Groups,
                        accentColor = PresentGreen,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        title = strings.todayWagesLabel,
                        value = CurrencyFormatter.formatTaka(todayEarned),
                        subtitle = strings.todayWagesLabel,
                        icon = Icons.Default.Payments,
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryStatCard(
                        title = "${KhataDateUtils.getMonthName(selectedMonth)} ${strings.kpiTotalWages}",
                        value = CurrencyFormatter.formatTaka(monthTotalEarned),
                        subtitle = "${strings.kpiAdvancesTaken}: ${CurrencyFormatter.formatTaka(monthTotalTaken)}",
                        accentColor = KhataPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        title = strings.kpiNetPendingDues,
                        value = CurrencyFormatter.formatTaka(monthTotalDue),
                        subtitle = strings.kpiNetPendingDues,
                        accentColor = DueAmber,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (onNavigateToSummary != null) {
                    Card(
                        onClick = onNavigateToSummary,
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_visual_charts_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BarChart,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = strings.monthlyChartsBannerTitle,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = strings.monthlyChartsBannerSubtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Open Charts",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Quick Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddWorkerClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_add_worker_button")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.addWorkerAction, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Button(
                    onClick = onAddPaymentClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KhataTertiary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_add_payment_button")
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${strings.addPaymentAction} (${CurrencyFormatter.defaultCurrencySymbol})", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        // Today's Hajira List Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.workerStatusSectionTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (searchQuery.isNotBlank()) {
                            "${KhataDateUtils.formatDisplayDate(selectedDate)} • ${strings.totalWorkersLabel}: ${filteredSummaries.size} / $totalWorkers"
                        } else {
                            "${KhataDateUtils.formatDisplayDate(selectedDate)} • $totalWorkers ${strings.totalWorkersLabel}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = onNavigateToAttendance,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("home_view_all_attendance_button")
                ) {
                    Text(strings.viewDetail, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Worker List for Today
        if (workerSummaries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No workers added yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tap the button below to add workers with name and daily wage rate.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = onAddWorkerClick,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Add New Worker")
                        }
                    }
                }
            }
        } else if (filteredSummaries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No workers found matching \"$searchQuery\"",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Check the name spelling or clear the search query.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = { searchQuery = "" },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("home_clear_search_empty_button")
                        ) {
                            Text("Clear Search")
                        }
                    }
                }
            }
        } else {
            items(filteredSummaries, key = { it.worker.id }) { summary ->
                WorkerTodayCard(
                    summary = summary,
                    onStatusChange = { newStatus -> onStatusChange(summary.worker.id, newStatus) },
                    onClick = { onWorkerClick(summary.worker.id) }
                )
            }
        }
    }
}

@Composable
fun WorkerTodayCard(
    summary: WorkerMonthlySummary,
    onStatusChange: (String) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("worker_card_${summary.worker.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top row: Name, Daily Rate, and Chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = summary.worker.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Rate: ${CurrencyFormatter.formatTaka(summary.worker.dailyWage)}/day",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (summary.worker.phone.isNotBlank()) {
                            Text(
                                text = "• ${summary.worker.phone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Today's Earned Indicator
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (summary.todayWage > 0) "+${CurrencyFormatter.formatTaka(summary.todayWage)}" else "${CurrencyFormatter.defaultCurrencySymbol} 0",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.todayWage > 0) PresentGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Earned Today",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Attendance Action Row
            AttendanceToggleGroup(
                currentStatus = summary.todayStatus,
                onStatusChange = onStatusChange,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom summary strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "This Month: ${CurrencyFormatter.formatDays(summary.totalHajira)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Due: ${CurrencyFormatter.formatTaka(summary.balanceDue)} • 📖 Khata ›",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (summary.balanceDue >= 0) DueAmber else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
