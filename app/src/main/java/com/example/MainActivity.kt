package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.entity.Worker
import com.example.ui.locale.AppStrings
import com.example.ui.locale.LocalAppStrings
import com.example.ui.screens.AddWorkerDialog
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MonthlySummaryScreen
import com.example.ui.screens.PaymentDialog
import com.example.ui.screens.PaymentsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WorkerDetailScreen
import com.example.ui.screens.WorkersScreen
import com.example.ui.theme.KhataPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.LaborKhataViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LaborKhataViewModel = viewModel()
            val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
            val strings = remember(appSettings.appLanguage) { AppStrings.forLanguage(appSettings.appLanguage) }
            CompositionLocalProvider(LocalAppStrings provides strings) {
                MyApplicationTheme(
                    themeMode = appSettings.themeMode,
                    colorPalette = appSettings.colorPalette
                ) {
                    LaborKhataApp(viewModel = viewModel)
                }
            }
        }
    }
}

enum class NavigationTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("Home", Icons.Default.Home),
    ATTENDANCE("Attendance", Icons.Default.EventAvailable),
    SUMMARY("Summary", Icons.Default.BarChart),
    WORKERS("Workers", Icons.Default.Groups),
    PAYMENTS("Payments", Icons.Default.AccountBalanceWallet),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun LaborKhataApp(viewModel: LaborKhataViewModel = viewModel()) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val workerSummaries by viewModel.workerSummaries.collectAsStateWithLifecycle()
    val allWorkers by viewModel.allWorkers.collectAsStateWithLifecycle()
    val paymentsForMonth by viewModel.paymentsForSelectedMonth.collectAsStateWithLifecycle()

    val selectedWorkerId by viewModel.selectedWorkerId.collectAsStateWithLifecycle()
    val workerDetailAttendance by viewModel.selectedWorkerAttendance.collectAsStateWithLifecycle()
    val workerDetailPayments by viewModel.selectedWorkerPayments.collectAsStateWithLifecycle()
    val workerDetailMonth by viewModel.workerDetailMonth.collectAsStateWithLifecycle()
    val workerDetailYear by viewModel.workerDetailYear.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()

    val strings = LocalAppStrings.current
    var currentTab by remember { mutableStateOf(NavigationTab.HOME) }
    var showAddWorkerDialog by remember { mutableStateOf(false) }
    var workerToEdit by remember { mutableStateOf<Worker?>(null) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var paymentWorkerId by remember { mutableStateOf<Int?>(null) }

    // Handle back press if inside worker detail screen
    BackHandler(enabled = selectedWorkerId != null) {
        viewModel.selectWorker(null)
    }

    if (selectedWorkerId != null) {
        val selectedSummary = workerSummaries.find { it.worker.id == selectedWorkerId }
        WorkerDetailScreen(
            workerId = selectedWorkerId!!,
            workerSummary = selectedSummary,
            workerAttendanceList = workerDetailAttendance,
            workerPaymentsList = workerDetailPayments,
            selectedYear = workerDetailYear,
            selectedMonth = workerDetailMonth,
            onBack = { viewModel.selectWorker(null) },
            onMonthChange = { year, month ->
                viewModel.setWorkerDetailMonth(year, month)
            },
            onAddPaymentClick = { id ->
                paymentWorkerId = id
                showPaymentDialog = true
            },
            onEditWorkerClick = { worker ->
                workerToEdit = worker
                showAddWorkerDialog = true
            },
            onDeletePayment = { paymentId ->
                viewModel.deletePayment(paymentId)
            },
            onAttendanceStatusChange = { date, status ->
                viewModel.setWorkerAttendanceForDate(selectedWorkerId!!, date, status)
            },
            onSetAttendanceForDate = { date, status, overtime ->
                viewModel.setWorkerAttendanceForDate(selectedWorkerId!!, date, status, overtime)
            },
            onDeleteAttendanceForDate = { date ->
                viewModel.deleteWorkerAttendanceForDate(selectedWorkerId!!, date)
            },
            onAddPaymentForDate = { date, amount, note ->
                viewModel.addPayment(selectedWorkerId!!, date, amount, note)
            },
            settings = appSettings
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("bottom_navigation_bar")
                ) {
                    NavigationTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab
                        val tabLabel = when (tab) {
                            NavigationTab.HOME -> strings.navHome
                            NavigationTab.ATTENDANCE -> strings.navAttendance
                            NavigationTab.SUMMARY -> strings.navSummary
                            NavigationTab.WORKERS -> strings.navWorkers
                            NavigationTab.PAYMENTS -> strings.navPayments
                            NavigationTab.SETTINGS -> strings.navSettings
                        }
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tabLabel
                                )
                            },
                            label = {
                                Text(
                                    text = tabLabel,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    NavigationTab.HOME -> {
                        HomeScreen(
                            selectedDate = selectedDate,
                            selectedYear = selectedYear,
                            selectedMonth = selectedMonth,
                            workerSummaries = workerSummaries,
                            onPreviousDay = { viewModel.shiftDate(-1) },
                            onNextDay = { viewModel.shiftDate(1) },
                            onDateSelect = { newDate -> viewModel.setSelectedDate(newDate) },
                            onMonthSelect = { year, month -> viewModel.setSelectedMonth(year, month) },
                            onStatusChange = { workerId, status ->
                                viewModel.setAttendanceStatus(workerId, status)
                            },
                            onWorkerClick = { workerId ->
                                viewModel.selectWorker(workerId)
                            },
                            onAddWorkerClick = {
                                workerToEdit = null
                                showAddWorkerDialog = true
                            },
                            onAddPaymentClick = {
                                paymentWorkerId = null
                                showPaymentDialog = true
                            },
                            onNavigateToAttendance = {
                                currentTab = NavigationTab.ATTENDANCE
                            },
                            onNavigateToSettings = {
                                currentTab = NavigationTab.SETTINGS
                            },
                            onNavigateToSummary = {
                                currentTab = NavigationTab.SUMMARY
                            }
                        )
                    }

                    NavigationTab.ATTENDANCE -> {
                        AttendanceScreen(
                            selectedDate = selectedDate,
                            selectedYear = selectedYear,
                            selectedMonth = selectedMonth,
                            workerSummaries = workerSummaries,
                            onPreviousDay = { viewModel.shiftDate(-1) },
                            onNextDay = { viewModel.shiftDate(1) },
                            onDateSelect = { newDate -> viewModel.setSelectedDate(newDate) },
                            onMonthSelect = { year, month -> viewModel.setSelectedMonth(year, month) },
                            onStatusChange = { workerId, status ->
                                viewModel.setAttendanceStatus(workerId, status)
                            },
                            onOvertimeChange = { workerId, overtime ->
                                viewModel.setOvertime(workerId, overtime)
                            },
                            onMarkAll = { status ->
                                viewModel.markAllAttendance(status)
                            },
                            onWorkerClick = { workerId ->
                                viewModel.selectWorker(workerId)
                            },
                            settings = appSettings
                        )
                    }

                    NavigationTab.SUMMARY -> {
                        MonthlySummaryScreen(
                            workerSummaries = workerSummaries,
                            selectedYear = selectedYear,
                            selectedMonth = selectedMonth,
                            onMonthChange = { year, month ->
                                viewModel.setSelectedMonth(year, month)
                            },
                            onWorkerClick = { workerId ->
                                viewModel.selectWorker(workerId)
                            },
                            onAddPaymentClick = { workerId ->
                                paymentWorkerId = workerId
                                showPaymentDialog = true
                            },
                            settings = appSettings
                        )
                    }

                    NavigationTab.WORKERS -> {
                        WorkersScreen(
                            workerSummaries = workerSummaries,
                            selectedMonth = selectedMonth,
                            selectedYear = selectedYear,
                            onWorkerClick = { workerId ->
                                viewModel.selectWorker(workerId)
                            },
                            onAddWorkerClick = {
                                workerToEdit = null
                                showAddWorkerDialog = true
                            },
                            onEditWorkerClick = { worker ->
                                workerToEdit = worker
                                showAddWorkerDialog = true
                            },
                            onDeleteWorker = { workerId ->
                                viewModel.deleteWorker(workerId)
                            }
                        )
                    }

                    NavigationTab.PAYMENTS -> {
                        PaymentsScreen(
                            payments = paymentsForMonth,
                            workers = allWorkers,
                            selectedMonth = selectedMonth,
                            selectedYear = selectedYear,
                            onAddPaymentClick = {
                                paymentWorkerId = null
                                showPaymentDialog = true
                            },
                            onDeletePayment = { paymentId ->
                                viewModel.deletePayment(paymentId)
                            },
                            onWorkerClick = { workerId ->
                                viewModel.selectWorker(workerId)
                            }
                        )
                    }

                    NavigationTab.SETTINGS -> {
                        SettingsScreen(
                            settings = appSettings,
                            onUpdateLanguage = { viewModel.updateAppLanguage(it) },
                            onUpdateThemeMode = { viewModel.updateThemeMode(it) },
                            onUpdateColorPalette = { viewModel.updateColorPalette(it) },
                            onUpdateLedgerLayout = { viewModel.updateLedgerLayoutStyle(it) },
                            onUpdateCurrency = { viewModel.updateCurrency(it) },
                            onUpdateStandardWorkHours = { viewModel.updateStandardWorkHours(it) },
                            onUpdateCardCornerStyle = { viewModel.updateCardCornerStyle(it) },
                            onTogglePhoneNumbers = { viewModel.togglePhoneNumbers(it) },
                            onToggleOtColumn = { viewModel.toggleOtColumn(it) },
                            onToggleHighlightDue = { viewModel.toggleHighlightDue(it) },
                            onToggleSoundVibration = { viewModel.toggleSoundVibration(it) },
                            onToggleCompactDensity = { viewModel.toggleCompactDensity(it) },
                            onResetSettings = { viewModel.resetSettingsToDefaults() },
                            onReseedData = { viewModel.reseedSampleData() },
                            onClearAllData = { viewModel.clearAllData() }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Worker Dialog
    if (showAddWorkerDialog) {
        AddWorkerDialog(
            workerToEdit = workerToEdit,
            onDismiss = {
                showAddWorkerDialog = false
                workerToEdit = null
            },
            onSaveWorker = { name, phone, dailyWage, initialAdvance ->
                viewModel.addWorker(name, phone, dailyWage, initialAdvance)
            },
            onUpdateWorker = { updatedWorker ->
                viewModel.updateWorker(updatedWorker)
            }
        )
    }

    // Record Payment Dialog
    if (showPaymentDialog) {
        PaymentDialog(
            initialWorkerId = paymentWorkerId,
            workers = allWorkers,
            workerSummaries = workerSummaries,
            currentDate = selectedDate,
            onDismiss = {
                showPaymentDialog = false
                paymentWorkerId = null
            },
            onSavePayment = { workerId, date, amount, note ->
                viewModel.addPayment(workerId, date, amount, note)
            }
        )
    }
}

// Kept for backward compatibility with GreetingScreenshotTest
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Labor Hajira Khata")
    }
}
