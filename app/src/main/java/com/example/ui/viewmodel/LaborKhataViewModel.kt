package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.entity.Attendance
import com.example.data.entity.Payment
import com.example.data.entity.Worker
import com.example.data.repository.LaborKhataRepository
import com.example.data.settings.AppSettings
import com.example.data.settings.CardCornerStyle
import com.example.data.settings.ColorPalette
import com.example.data.settings.LedgerLayoutStyle
import com.example.data.settings.SettingsPreferences
import com.example.data.settings.ThemeMode
import com.example.ui.model.CurrencyFormatter
import com.example.ui.model.KhataDateUtils
import com.example.ui.model.WorkerMonthlySummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LaborKhataViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LaborKhataRepository
    private val settingsPrefs = SettingsPreferences(application)
    val appSettings: StateFlow<AppSettings> = settingsPrefs.settings

    init {
        val database = AppDatabase.getDatabase(application)
        repository = LaborKhataRepository(
            database.workerDao(),
            database.attendanceDao(),
            database.paymentDao()
        )

        // Sync initial currency symbol
        CurrencyFormatter.defaultCurrencySymbol = settingsPrefs.settings.value.currencySymbol

        // Observe settings to sync CurrencyFormatter
        viewModelScope.launch {
            settingsPrefs.settings.collect { settings ->
                CurrencyFormatter.defaultCurrencySymbol = settings.currencySymbol
            }
        }

        // Auto-seeding disabled to ensure real user data and prevent fake names
    }

    // Selected Date & Month State
    private val _selectedDate = MutableStateFlow(KhataDateUtils.getTodayIso())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedYear = MutableStateFlow(KhataDateUtils.getCurrentYear())
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(KhataDateUtils.getCurrentMonth())
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    // Currently selected worker for details screen
    private val _selectedWorkerId = MutableStateFlow<Int?>(null)
    val selectedWorkerId: StateFlow<Int?> = _selectedWorkerId.asStateFlow()

    // Filter month for worker detail screen (null = same as general selectedMonth, or can be changed)
    private val _workerDetailMonth = MutableStateFlow(KhataDateUtils.getCurrentMonth())
    val workerDetailMonth: StateFlow<Int> = _workerDetailMonth.asStateFlow()

    private val _workerDetailYear = MutableStateFlow(KhataDateUtils.getCurrentYear())
    val workerDetailYear: StateFlow<Int> = _workerDetailYear.asStateFlow()

    // Workers Stream
    val allWorkers: StateFlow<List<Worker>> = repository.allWorkers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Attendance for the currently selected date
    val attendanceForSelectedDate: StateFlow<List<Attendance>> = _selectedDate
        .flatMapLatest { date -> repository.getAttendanceByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Attendance for the currently selected month
    val attendanceForSelectedMonth: StateFlow<List<Attendance>> = combine(
        _selectedYear, _selectedMonth
    ) { year, month -> Pair(year, month) }
        .flatMapLatest { (year, month) -> repository.getAttendanceByMonth(year, month) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Payments for the selected month
    val paymentsForSelectedMonth: StateFlow<List<Payment>> = combine(
        _selectedYear, _selectedMonth
    ) { year, month -> KhataDateUtils.toYearMonthPattern(year, month) }
        .flatMapLatest { yearMonthPattern -> repository.getPaymentsByMonth(yearMonthPattern) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All payments
    val allPayments: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined Worker Monthly Summaries
    val workerSummaries: StateFlow<List<WorkerMonthlySummary>> = combine(
        allWorkers,
        attendanceForSelectedMonth,
        paymentsForSelectedMonth,
        attendanceForSelectedDate
    ) { workers, monthlyAttendance, monthlyPayments, todayAttendance ->
        val todayMap = todayAttendance.associateBy { it.workerId }
        val attendanceByWorker = monthlyAttendance.groupBy { it.workerId }
        val paymentsByWorker = monthlyPayments.groupBy { it.workerId }

        workers.map { worker ->
            val workerAtt = attendanceByWorker[worker.id] ?: emptyList()
            val workerPay = paymentsByWorker[worker.id] ?: emptyList()

            var present = 0
            var oneAndHalf = 0
            var doubleHajira = 0
            var half = 0
            var absent = 0
            var totalHajira = 0.0
            var totalOvertimeHours = 0.0

            for (att in workerAtt) {
                when (att.status) {
                    "Present", "1.0" -> {
                        present++
                        totalHajira += 1.0
                    }
                    "OneAndHalf", "1.5" -> {
                        oneAndHalf++
                        totalHajira += 1.5
                    }
                    "Double", "2.0" -> {
                        doubleHajira++
                        totalHajira += 2.0
                    }
                    "Half", "0.5" -> {
                        half++
                        totalHajira += 0.5
                    }
                    "Absent" -> {
                        absent++
                    }
                }
                totalOvertimeHours += att.overtime
            }

            // Standard 8-hour workday assumed for hourly overtime rate calculation: (dailyWage / 8) * overtime
            val hourlyWage = worker.dailyWage / 8.0
            val totalOvertimeWage = totalOvertimeHours * hourlyWage
            val baseEarned = totalHajira * worker.dailyWage
            val totalEarned = baseEarned + totalOvertimeWage
            val totalMoneyTaken = workerPay.sumOf { it.amountTaken }
            val balanceDue = totalEarned - totalMoneyTaken

            val todayAtt = todayMap[worker.id]
            val todayStatus = todayAtt?.status
            val todayOvertime = todayAtt?.overtime ?: 0.0
            val todayBaseWage = when (todayStatus) {
                "Present", "1.0" -> worker.dailyWage
                "OneAndHalf", "1.5" -> worker.dailyWage * 1.5
                "Double", "2.0" -> worker.dailyWage * 2.0
                "Half", "0.5" -> worker.dailyWage * 0.5
                else -> 0.0
            }
            val todayOvertimeWage = todayOvertime * hourlyWage
            val todayWage = todayBaseWage + todayOvertimeWage

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
                todayStatus = todayStatus,
                todayOvertime = todayOvertime,
                todayWage = todayWage
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Worker detail attendance stream
    val selectedWorkerAttendance: StateFlow<List<Attendance>> = combine(
        _selectedWorkerId, _workerDetailYear, _workerDetailMonth
    ) { workerId, year, month -> Triple(workerId, year, month) }
        .flatMapLatest { (workerId, year, month) ->
            if (workerId != null) {
                repository.getAttendanceForWorkerInMonth(workerId, year, month)
            } else {
                MutableStateFlow(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Worker detail payments stream
    val selectedWorkerPayments: StateFlow<List<Payment>> = combine(
        _selectedWorkerId, _workerDetailYear, _workerDetailMonth
    ) { workerId, year, month ->
        Pair(workerId, KhataDateUtils.toYearMonthPattern(year, month))
    }.flatMapLatest { (workerId, pattern) ->
        if (workerId != null) {
            repository.getPaymentsForWorkerInMonth(workerId, pattern)
        } else {
            MutableStateFlow(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User Actions
    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        _selectedYear.value = KhataDateUtils.parseYearFromIso(date)
        _selectedMonth.value = KhataDateUtils.parseMonthFromIso(date)
    }

    fun setSelectedMonth(year: Int, month: Int) {
        _selectedYear.value = year
        _selectedMonth.value = month
        // Update selected date to 1st of month if out of current month
        val currentYear = KhataDateUtils.parseYearFromIso(_selectedDate.value)
        val currentMonth = KhataDateUtils.parseMonthFromIso(_selectedDate.value)
        if (currentYear != year || currentMonth != month) {
            _selectedDate.value = String.format(java.util.Locale.US, "%04d-%02d-01", year, month)
        }
    }

    fun shiftDate(days: Int) {
        val newDate = KhataDateUtils.shiftDate(_selectedDate.value, days)
        setSelectedDate(newDate)
    }

    fun setAttendanceStatus(workerId: Int, status: String, overtime: Double? = null) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val year = _selectedYear.value
            val month = _selectedMonth.value
            val currentOt = overtime ?: (attendanceForSelectedDate.value.find { it.workerId == workerId }?.overtime ?: 0.0)
            repository.setAttendance(workerId, date, month, year, status, currentOt)
        }
    }

    fun setOvertime(workerId: Int, overtime: Double) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val year = _selectedYear.value
            val month = _selectedMonth.value
            val currentStatus = attendanceForSelectedDate.value.find { it.workerId == workerId }?.status ?: "Present"
            repository.setAttendance(workerId, date, month, year, currentStatus, overtime)
        }
    }

    fun toggleAttendance(workerId: Int, currentStatus: String?) {
        val nextStatus = when (currentStatus) {
            null -> "Present"
            "Present" -> "Half"
            "Half" -> "Absent"
            "Absent" -> "Present"
            else -> "Present"
        }
        val currentOt = attendanceForSelectedDate.value.find { it.workerId == workerId }?.overtime ?: 0.0
        setAttendanceStatus(workerId, nextStatus, currentOt)
    }

    fun markAllAttendance(status: String) {
        viewModelScope.launch {
            val workers = allWorkers.value
            val date = _selectedDate.value
            val year = _selectedYear.value
            val month = _selectedMonth.value
            repository.markAllAttendance(workers.map { it.id }, date, month, year, status)
        }
    }

    fun addWorker(name: String, phone: String, dailyWage: Double, initialAdvance: Double = 0.0) {
        viewModelScope.launch {
            val workerId = repository.insertWorker(
                Worker(
                    name = name.trim(),
                    phone = phone.trim(),
                    dailyWage = dailyWage
                )
            ).toInt()

            if (initialAdvance > 0.0) {
                repository.insertPayment(
                    Payment(
                        workerId = workerId,
                        date = _selectedDate.value,
                        amountTaken = initialAdvance,
                        note = "Initial advance"
                    )
                )
            }
        }
    }

    fun updateWorker(worker: Worker) {
        viewModelScope.launch {
            repository.updateWorker(worker)
        }
    }

    fun deleteWorker(workerId: Int) {
        viewModelScope.launch {
            repository.deleteWorker(workerId)
            if (_selectedWorkerId.value == workerId) {
                _selectedWorkerId.value = null
            }
        }
    }

    fun addPayment(workerId: Int, date: String, amount: Double, note: String) {
        viewModelScope.launch {
            repository.insertPayment(
                Payment(
                    workerId = workerId,
                    date = date,
                    amountTaken = amount,
                    note = note.trim()
                )
            )
        }
    }

    fun deletePayment(paymentId: Int) {
        viewModelScope.launch {
            repository.deletePayment(paymentId)
        }
    }

    fun selectWorker(workerId: Int?) {
        _selectedWorkerId.value = workerId
        if (workerId != null) {
            _workerDetailYear.value = _selectedYear.value
            _workerDetailMonth.value = _selectedMonth.value
        }
    }

    fun setWorkerDetailMonth(year: Int, month: Int) {
        _workerDetailYear.value = year
        _workerDetailMonth.value = month
    }

    fun setWorkerAttendanceForDate(workerId: Int, date: String, status: String, overtime: Double = 0.0) {
        viewModelScope.launch {
            val year = KhataDateUtils.parseYearFromIso(date)
            val month = KhataDateUtils.parseMonthFromIso(date)
            repository.setAttendance(workerId, date, month, year, status, overtime)
        }
    }

    fun deleteWorkerAttendanceForDate(workerId: Int, date: String) {
        viewModelScope.launch {
            repository.deleteAttendanceByDate(workerId, date)
        }
    }

    // App Settings Management
    fun updateSettings(newSettings: AppSettings) {
        settingsPrefs.updateSettings(newSettings)
    }

    fun updateAppLanguage(language: com.example.data.settings.AppLanguage) {
        updateSettings(appSettings.value.copy(appLanguage = language))
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        updateSettings(appSettings.value.copy(themeMode = themeMode))
    }

    fun updateColorPalette(colorPalette: ColorPalette) {
        updateSettings(appSettings.value.copy(colorPalette = colorPalette))
    }

    fun updateLedgerLayoutStyle(style: LedgerLayoutStyle) {
        updateSettings(appSettings.value.copy(ledgerLayoutStyle = style))
    }

    fun updateCurrency(symbol: String) {
        updateSettings(appSettings.value.copy(currencySymbol = symbol))
    }

    fun updateStandardWorkHours(hours: Int) {
        updateSettings(appSettings.value.copy(standardWorkHours = hours))
    }

    fun updateCardCornerStyle(style: CardCornerStyle) {
        updateSettings(appSettings.value.copy(cardCornerStyle = style))
    }

    fun togglePhoneNumbers(show: Boolean) {
        updateSettings(appSettings.value.copy(showPhoneNumbers = show))
    }

    fun toggleOtColumn(show: Boolean) {
        updateSettings(appSettings.value.copy(showOtColumn = show))
    }

    fun toggleHighlightDue(highlight: Boolean) {
        updateSettings(appSettings.value.copy(highlightDueBalance = highlight))
    }

    fun toggleSoundVibration(enabled: Boolean) {
        updateSettings(appSettings.value.copy(soundVibrationEnabled = enabled))
    }

    fun toggleCompactDensity(compact: Boolean) {
        updateSettings(appSettings.value.copy(compactDensity = compact))
    }

    fun resetSettingsToDefaults() {
        settingsPrefs.resetToDefaults()
    }

    fun reseedSampleData() {
        viewModelScope.launch {
            repository.reseedData(
                currentDate = _selectedDate.value,
                currentYear = _selectedYear.value,
                currentMonth = _selectedMonth.value
            )
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _selectedWorkerId.value = null
        }
    }
}
