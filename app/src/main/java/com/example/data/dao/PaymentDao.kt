package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY date DESC, id DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE worker_id = :workerId ORDER BY date DESC, id DESC")
    fun getPaymentsForWorker(workerId: Int): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE worker_id = :workerId AND date LIKE :yearMonthPattern || '%' ORDER BY date DESC, id DESC")
    fun getPaymentsForWorkerInMonth(workerId: Int, yearMonthPattern: String): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE date LIKE :yearMonthPattern || '%' ORDER BY date DESC, id DESC")
    fun getPaymentsByMonth(yearMonthPattern: String): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deletePayment(id: Int)

    @Query("DELETE FROM payments WHERE worker_id = :workerId")
    suspend fun deletePaymentsForWorker(workerId: Int)

    @Query("DELETE FROM payments")
    suspend fun deleteAllPayments()
}
