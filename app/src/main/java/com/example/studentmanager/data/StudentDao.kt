package com.example.studentmanager.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Query("SELECT * FROM students ORDER BY fullName COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    fun observeById(id: Long): Flow<Student?>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getById(id: Long): Student?

    @Query("SELECT COUNT(*) FROM students WHERE studentCode = :code AND id != :excludeId")
    suspend fun countByCode(code: String, excludeId: Long): Int

    @Insert
    suspend fun insert(student: Student): Long

    @Update
    suspend fun update(student: Student)

    @Delete
    suspend fun delete(student: Student)
}
