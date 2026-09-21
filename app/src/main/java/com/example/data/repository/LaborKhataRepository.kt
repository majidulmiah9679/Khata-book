package com.example.data.repository

import com.example.data.dao.AttendanceDao
import com.example.data.dao.PaymentDao
import com.example.data.dao.WorkerDao
import com.example.data.entity.Attendance
import com.example.data.entity.Payment
import com.example.data.entity.Worker
import kotlinx.coroutines.flow.Flow

class LaborKhataRepository(
    private val workerDao: WorkerDao,
    private val attendanceDao: AttendanceDao,
    private val paymentDao: PaymentDao
) {
    val allWorkers: Flow<List<Worker>> = workerDao.getAllWorkers()

    fun getWorkerById(id: Int): Flow<Worker?> = workerDao.getWorkerById(id)

    suspend fun insertWorker(worker: Worker): Long = workerDao.insertWorker(worker)

    suspend fun updateWorker(worker: Worker) = workerDao.updateWorker(worker)

    suspend fun deleteWorker(workerId: Int) {
        attendanceDao.deleteAttendanceForWorker(workerId)
        paymentDao.deletePaymentsForWorker(workerId)
        workerDao.deleteWorkerById(workerId)
    }

    // Attendance
    fun getAttendanceByDate(date: String): Flow<List<Attendance>> =
        attendanceDao.getAttendanceByDate(date)

    fun getAttendanceByMonth(year: Int, month: Int): Flow<List<Attendance>> =
        attendanceDao.getAttendanceByMonth(year, month)

    fun getAttendanceForWorker(workerId: Int): Flow<List<Attendance>> =
        attendanceDao.getAttendanceForWorker(workerId)

    fun getAttendanceForWorkerInMonth(workerId: Int, year: Int, month: Int): Flow<List<Attendance>> =
        attendanceDao.getAttendanceForWorkerInMonth(workerId, year, month)

    suspend fun setAttendance(workerId: Int, date: String, month: Int, year: Int, status: String, overtime: Double = 0.0) {
        val attendance = Attendance(
            workerId = workerId,
            date = date,
            month = month,
            year = year,
            status = status,
            overtime = overtime
        )
        attendanceDao.insertOrUpdateAttendance(attendance)
    }

    suspend fun markAllAttendance(workerIds: List<Int>, date: String, month: Int, year: Int, status: String) {
        val list = workerIds.map { workerId ->
            Attendance(
                workerId = workerId,
                date = date,
                month = month,
                year = year,
                status = status,
                overtime = 0.0
            )
        }
        attendanceDao.insertOrUpdateAll(list)
    }

    suspend fun deleteAttendance(id: Int) = attendanceDao.deleteAttendance(id)

    suspend fun deleteAttendanceByDate(workerId: Int, date: String) =
        attendanceDao.deleteAttendanceByDate(workerId, date)

    // Payments
    val allPayments: Flow<List<Payment>> = paymentDao.getAllPayments()

    fun getPaymentsForWorker(workerId: Int): Flow<List<Payment>> =
        paymentDao.getPaymentsForWorker(workerId)

    fun getPaymentsForWorkerInMonth(workerId: Int, yearMonth: String): Flow<List<Payment>> =
        paymentDao.getPaymentsForWorkerInMonth(workerId, yearMonth)

    fun getPaymentsByMonth(yearMonth: String): Flow<List<Payment>> =
        paymentDao.getPaymentsByMonth(yearMonth)

    suspend fun insertPayment(payment: Payment): Long = paymentDao.insertPayment(payment)

    suspend fun deletePayment(id: Int) = paymentDao.deletePayment(id)

    // Initial seed if needed (Seeds 20 workers as requested by user to support bulk entry of 20 people)
    suspend fun seedInitialDataIfEmpty(currentDate: String, currentYear: Int, currentMonth: Int) {
        val sampleWorkers = listOf(
            Worker(name = "1. Md. Rafiqul Islam", phone = "01712-345678", dailyWage = 500.0),
            Worker(name = "2. Abdul Karim", phone = "01823-456789", dailyWage = 600.0),
            Worker(name = "3. Jahirul Alam", phone = "01934-567890", dailyWage = 550.0),
            Worker(name = "4. Mizanur Rahman", phone = "01645-678901", dailyWage = 500.0),
            Worker(name = "5. Sujon Mia", phone = "01556-789012", dailyWage = 450.0),
            Worker(name = "6. Alamin Hossain", phone = "01777-112233", dailyWage = 520.0),
            Worker(name = "7. Shah Alam", phone = "01888-223344", dailyWage = 500.0),
            Worker(name = "8. Ripon Das", phone = "01999-334455", dailyWage = 480.0),
            Worker(name = "9. Kamal Hossain", phone = "01611-445566", dailyWage = 650.0),
            Worker(name = "10. Faruk Ahmed", phone = "01522-556677", dailyWage = 550.0),
            Worker(name = "11. Babul Mia", phone = "01733-667788", dailyWage = 500.0),
            Worker(name = "12. Harun Ur Rashid", phone = "01844-778899", dailyWage = 580.0),
            Worker(name = "13. Shafiqul Islam", phone = "01955-889900", dailyWage = 500.0),
            Worker(name = "14. Rubel Hasan", phone = "01666-990011", dailyWage = 480.0),
            Worker(name = "15. Anisur Rahman", phone = "01577-001122", dailyWage = 530.0),
            Worker(name = "16. Delwar Hossain", phone = "01788-112244", dailyWage = 600.0),
            Worker(name = "17. Masud Rana", phone = "01899-223355", dailyWage = 500.0),
            Worker(name = "18. Zakir Hossain", phone = "01911-334466", dailyWage = 520.0),
            Worker(name = "19. Monir Hossain", phone = "01622-445577", dailyWage = 470.0),
            Worker(name = "20. Liton Sheikh", phone = "01533-556688", dailyWage = 500.0)
        )

        val insertedIds = mutableListOf<Long>()
        for (w in sampleWorkers) {
            val id = workerDao.insertWorker(w)
            insertedIds.add(id)
        }

        // Add some attendance for today and earlier days in this month
        if (insertedIds.isNotEmpty()) {
            setAttendance(insertedIds[0].toInt(), currentDate, currentMonth, currentYear, "Present", 2.0)
            setAttendance(insertedIds[1].toInt(), currentDate, currentMonth, currentYear, "Present", 1.5)
            setAttendance(insertedIds[2].toInt(), currentDate, currentMonth, currentYear, "Half", 0.0)
            setAttendance(insertedIds[3].toInt(), currentDate, currentMonth, currentYear, "Present", 3.0)
            setAttendance(insertedIds[4].toInt(), currentDate, currentMonth, currentYear, "Absent", 0.0)
            setAttendance(insertedIds[5].toInt(), currentDate, currentMonth, currentYear, "Present", 1.0)
            setAttendance(insertedIds[6].toInt(), currentDate, currentMonth, currentYear, "Present", 0.0)

            // Add an advance payment
            insertPayment(
                Payment(
                    workerId = insertedIds[0].toInt(),
                    date = currentDate,
                    amountTaken = 1000.0,
                    note = "Weekly groceries advance"
                )
            )
            insertPayment(
                Payment(
                    workerId = insertedIds[1].toInt(),
                    date = currentDate,
                    amountTaken = 1500.0,
                    note = "Family expense"
                )
            )
            insertPayment(
                Payment(
                    workerId = insertedIds[3].toInt(),
                    date = currentDate,
                    amountTaken = 500.0,
                    note = "Emergency cash"
                )
            )
        }
    }

    suspend fun clearAllData() {
        attendanceDao.deleteAllAttendance()
        paymentDao.deleteAllPayments()
        workerDao.deleteAllWorkers()
    }

    suspend fun reseedData(currentDate: String, currentYear: Int, currentMonth: Int) {
        clearAllData()
        seedInitialDataIfEmpty(currentDate, currentYear, currentMonth)
    }
}
