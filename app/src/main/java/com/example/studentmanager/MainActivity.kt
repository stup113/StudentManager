package com.example.studentmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.studentmanager.ui.StudentDetailScreen
import com.example.studentmanager.ui.StudentFormScreen
import com.example.studentmanager.ui.StudentListScreen
import com.example.studentmanager.ui.StudentViewModel
import com.example.studentmanager.ui.theme.StudentManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudentManagerTheme {
                StudentApp()
            }
        }
    }
}

private object Routes {
    const val LIST = "list"
    const val DETAIL = "detail/{id}"
    const val FORM = "form/{id}"
    fun detail(id: Long) = "detail/$id"
    fun form(id: Long) = "form/$id" // id = 0 -> thêm mới
}

@Composable
fun StudentApp() {
    val navController = rememberNavController()
    val vm: StudentViewModel = viewModel()
    val idArg = listOf(navArgument("id") { type = NavType.LongType })

    NavHost(navController = navController, startDestination = Routes.LIST) {

        // Trang 1: danh sách
        composable(Routes.LIST) {
            StudentListScreen(
                vm = vm,
                onOpenDetail = { navController.navigate(Routes.detail(it)) },
                onAdd = { navController.navigate(Routes.form(0)) },
                onEdit = { navController.navigate(Routes.form(it)) }
            )
        }

        // Trang 2: chi tiết (chỉ xem)
        composable(Routes.DETAIL, arguments = idArg) { entry ->
            StudentDetailScreen(
                studentId = entry.arguments?.getLong("id") ?: 0L,
                vm = vm,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.form(it)) }
            )
        }

        // Trang 3: thêm mới / sửa
        composable(Routes.FORM, arguments = idArg) { entry ->
            StudentFormScreen(
                studentId = entry.arguments?.getLong("id") ?: 0L,
                vm = vm,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
    }
}
