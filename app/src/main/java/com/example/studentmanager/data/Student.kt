package com.example.studentmanager.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    indices = [Index(value = ["studentCode"], unique = true)]
)
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val studentCode: String,
    val email: String,
    /** Link ảnh (http/https) hoặc đường dẫn file local (file://...) của ảnh upload từ thiết bị */
    val avatarUrl: String = ""
)
