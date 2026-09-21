package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.Attendance
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceByDate(date: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE year = :year AND month = :month")
    fun getAttendanceByMonth(year: Int, month: Int): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE worker_id = :workerId ORDER BY date DESC")
    fun getAttendanceForWorker(workerId: Int): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE worker_id = :workerId AND year = :year AND month = :month ORDER BY date DESC")
    fun getAttendanceForWorkerInMonth(workerId: Int, year: Int, month: Int): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendance(attendance: Attendance)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(attendances: List<Attendance>)

    @Query("DELETE FROM attendance WHERE id = :id")
    suspend fun deleteAttendance(id: Int)

    @Query("DELETE FROM attendance WHERE worker_id = :workerId AND date = :date")
    suspend fun deleteAttendanceByDate(workerId: Int, date: String)

    @Query("DELETE FROM attendance WHERE worker_id = :workerId")
    suspend fun deleteAttendanceForWorker(workerId: Int)

    @Query("DELETE FROM attendance")
    suspend fun deleteAllAttendance()
}
