package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.Worker
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkerDao {
    @Query("SELECT * FROM workers ORDER BY name ASC")
    fun getAllWorkers(): Flow<List<Worker>>

    @Query("SELECT * FROM workers WHERE id = :id LIMIT 1")
    fun getWorkerById(id: Int): Flow<Worker?>

    @Query("SELECT * FROM workers WHERE id = :id LIMIT 1")
    suspend fun getWorkerByIdOnce(id: Int): Worker?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: Worker): Long

    @Update
    suspend fun updateWorker(worker: Worker)

    @Delete
    suspend fun deleteWorker(worker: Worker)

    @Query("DELETE FROM workers WHERE id = :id")
    suspend fun deleteWorkerById(id: Int)

    @Query("DELETE FROM workers")
    suspend fun deleteAllWorkers()
}
