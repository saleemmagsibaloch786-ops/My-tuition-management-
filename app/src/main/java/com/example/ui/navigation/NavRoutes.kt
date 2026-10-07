package com.example.ui.navigation

sealed class Screen(val route: String, val title: String) {
  data object Home : Screen("home", "Home")
  data object Students : Screen("students", "Students")
  data object Classes : Screen("classes", "Classes")
  data object Fees : Screen("fees", "Fees")
  data object More : Screen("more", "More")

  // Sub-screens
  data object StudentDetail : Screen("student_detail/{studentId}", "Student Profile") {
    fun createRoute(studentId: Long) = "student_detail/$studentId"
  }
  data object AttendanceHub : Screen("attendance_hub", "Attendance")
  data object TestsHub : Screen("tests_hub", "Tests & Marks")
  data object HomeworkHub : Screen("homework_hub", "Homework")
  data object PerformanceHub : Screen("performance_hub", "Performance")
  data object ReportsHub : Screen("reports_hub", "Reports & Analytics")
  data object SettingsScreen : Screen("settings_screen", "Settings")
}
