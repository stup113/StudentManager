package com.example.studentmanager.ui

import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.studentmanager.R
import com.example.studentmanager.data.Student
import kotlinx.coroutines.launch

/**
 * TRANG 3: Thêm mới / Sửa thông tin sinh viên.
 * studentId == 0 -> thêm mới; studentId > 0 -> sửa (cần xác nhận "Có/Không" trước khi lưu).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFormScreen(
    studentId: Long,
    vm: StudentViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val isEdit = studentId > 0
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var avatar by rememberSaveable { mutableStateOf("") }
    var originalAvatar by rememberSaveable { mutableStateOf("") }
    var loaded by rememberSaveable { mutableStateOf(!isEdit) }

    var nameError by rememberSaveable { mutableStateOf<Int?>(null) }
    var codeError by rememberSaveable { mutableStateOf<Int?>(null) }
    var emailError by rememberSaveable { mutableStateOf<Int?>(null) }
    var showConfirmEdit by remember { mutableStateOf(false) }

    // Khi sửa: nạp dữ liệu hiện tại vào form (chỉ 1 lần)
    LaunchedEffect(studentId) {
        if (isEdit && !loaded) {
            vm.get(studentId)?.let {
                name = it.fullName
                code = it.studentCode
                email = it.email
                avatar = it.avatarUrl
                originalAvatar = it.avatarUrl
            }
            loaded = true
        }
    }

    // Ảnh mới chọn nhưng chưa lưu -> xóa file tạm nếu bị thay / hủy
    fun discardUnsavedAvatar() {
        if (avatar != originalAvatar) vm.deleteLocalAvatar(avatar)
    }

    fun cancel() {
        discardUnsavedAvatar()
        onBack()
    }

    BackHandler { cancel() }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                vm.copyAvatar(uri)?.let { newPath ->
                    discardUnsavedAvatar()
                    avatar = newPath
                }
            }
        }
    }

    fun doSave() {
        val student = Student(
            id = if (isEdit) studentId else 0L,
            fullName = name.trim(),
            studentCode = code.trim(),
            email = email.trim(),
            avatarUrl = avatar.trim()
        )
        vm.save(student, originalAvatar) {
            Toast.makeText(
                context,
                if (isEdit) R.string.msg_updated else R.string.msg_added,
                Toast.LENGTH_SHORT
            ).show()
            onSaved()
        }
    }

    fun onSaveClick() {
        scope.launch {
            nameError = if (name.isBlank()) R.string.error_required else null
            codeError = if (code.isBlank()) R.string.error_required else null
            emailError = when {
                email.isBlank() -> R.string.error_required
                !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> R.string.error_email_invalid
                else -> null
            }
            if (codeError == null && vm.isCodeTaken(code.trim(), studentId)) {
                codeError = R.string.error_code_taken
            }
            if (nameError != null || codeError != null || emailError != null) return@launch

            if (isEdit) showConfirmEdit = true else doSave()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (isEdit) R.string.edit_student_title else R.string.add_student_title
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { cancel() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StudentAvatar(url = avatar, size = 120.dp)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.full_name)) },
                isError = nameError != null,
                supportingText = if (nameError != null) { { Text(stringResource(nameError!!)) } } else null,
                singleLine = true
            )
            OutlinedTextField(
                value = code,
                onValueChange = { code = it; codeError = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.student_code)) },
                isError = codeError != null,
                supportingText = if (codeError != null) { { Text(stringResource(codeError!!)) } } else null,
                singleLine = true
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; emailError = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.email)) },
                isError = emailError != null,
                supportingText = if (emailError != null) { { Text(stringResource(emailError!!)) } } else null,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                singleLine = true
            )

            // Avatar: nhập link ảnh HOẶC chọn ảnh từ thiết bị
            if (avatar.startsWith("file:")) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.avatar_from_device),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = {
                        discardUnsavedAvatar()
                        avatar = ""
                    }) { Text(stringResource(R.string.remove_avatar)) }
                }
            } else {
                OutlinedTextField(
                    value = avatar,
                    onValueChange = { avatar = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.avatar_url)) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Uri
                    ),
                    singleLine = true
                )
            }
            OutlinedButton(
                onClick = {
                    picker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.pick_from_device)) }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = { cancel() }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.cancel))
                }
                Button(onClick = { onSaveClick() }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }

    if (showConfirmEdit) {
        ConfirmDialog(
            message = stringResource(R.string.confirm_edit_message),
            onConfirm = {
                showConfirmEdit = false
                doSave()
            },
            onDismiss = { showConfirmEdit = false }
        )
    }
}
