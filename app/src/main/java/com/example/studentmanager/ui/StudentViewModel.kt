package com.example.studentmanager.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentmanager.data.AppDatabase
import com.example.studentmanager.data.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class StudentViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.get(app).studentDao()

    private val avatarDir: File
        get() = File(getApplication<Application>().filesDir, "avatars").apply { mkdirs() }

    val students: StateFlow<List<Student>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun observe(id: Long): Flow<Student?> = dao.observeById(id)

    suspend fun get(id: Long): Student? = dao.getById(id)

    suspend fun isCodeTaken(code: String, excludeId: Long): Boolean =
        dao.countByCode(code, excludeId) > 0

    /** Thêm mới (id == 0) hoặc cập nhật (id > 0). */
    fun save(student: Student, originalAvatar: String, onDone: () -> Unit) {
        viewModelScope.launch {
            if (student.id == 0L) {
                dao.insert(student)
            } else {
                dao.update(student)
                if (originalAvatar != student.avatarUrl) deleteLocalAvatar(originalAvatar)
            }
            onDone()
        }
    }

    fun delete(student: Student, onDone: () -> Unit) {
        viewModelScope.launch {
            dao.delete(student)
            deleteLocalAvatar(student.avatarUrl)
            onDone()
        }
    }

    /** Copy ảnh người dùng chọn từ thiết bị vào bộ nhớ trong của app để dùng lâu dài. */
    suspend fun copyAvatar(uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(avatarDir, "avatar_${System.currentTimeMillis()}.jpg")
            val input = getApplication<Application>().contentResolver.openInputStream(uri)
                ?: return@runCatching null
            input.use { src -> file.outputStream().use { dst -> src.copyTo(dst) } }
            Uri.fromFile(file).toString()
        }.getOrNull()
    }

    /** Xóa file ảnh local (nếu là ảnh do app lưu) – dùng khi hủy / thay ảnh / xóa SV. */
    fun deleteLocalAvatar(url: String) {
        if (!url.startsWith("file:")) return
        runCatching {
            val path = Uri.parse(url).path ?: return
            val file = File(path)
            if (file.absolutePath.startsWith(avatarDir.absolutePath)) file.delete()
        }
    }
}
