package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Student
import com.example.data.model.TeacherSettings
import com.example.service.NotificationHelper
import com.example.ui.components.BulkChalanProgressDialog
import com.example.ui.components.WhatsAppComposerDialog
import com.example.ui.navigation.Screen
import com.example.ui.screens.AddEditStudentDialog
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.ClassesScreen
import com.example.ui.screens.CollectFeeDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FeesScreen
import com.example.ui.screens.HomeworkScreen
import com.example.ui.screens.MoreHubScreen
import com.example.ui.screens.PerformanceScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudentDetailScreen
import com.example.ui.screens.StudentsScreen
import com.example.ui.screens.TestsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TuitionViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: TuitionViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize notification channel
    NotificationHelper.createNotificationChannel(this)

    setContent {
      MyApplicationTheme {
        TuitionApp(viewModel = viewModel)
      }
    }
  }
}

enum class MainTab(val title: String, val icon: ImageVector) {
  Home("Home", Icons.Default.Home),
  Students("Students", Icons.Default.School),
  Classes("Classes", Icons.Default.CalendarMonth),
  Fees("Fees", Icons.Default.Payments),
  More("More", Icons.Default.GridView)
}

sealed class AppDestination {
  data class Tab(val tab: MainTab) : AppDestination()
  data class StudentDetail(val studentId: Long) : AppDestination()
  data object Attendance : AppDestination()
  data object Tests : AppDestination()
  data object Homework : AppDestination()
  data object Performance : AppDestination()
  data object Reports : AppDestination()
  data object Settings : AppDestination()
}

@Composable
fun TuitionApp(viewModel: TuitionViewModel) {
  var currentDestination by remember {
    mutableStateOf<AppDestination>(AppDestination.Tab(MainTab.Home))
  }
  val backStack = remember { mutableListOf<AppDestination>() }

  fun navigateTo(dest: AppDestination) {
    backStack.add(currentDestination)
    currentDestination = dest
  }

  fun navigateBack() {
    if (backStack.isNotEmpty()) {
      currentDestination = backStack.removeAt(backStack.size - 1)
    } else {
      currentDestination = AppDestination.Tab(MainTab.Home)
    }
  }

  // BackHandler
  BackHandler(enabled = currentDestination !is AppDestination.Tab || (currentDestination as AppDestination.Tab).tab != MainTab.Home) {
    if (currentDestination is AppDestination.Tab && (currentDestination as AppDestination.Tab).tab != MainTab.Home) {
      currentDestination = AppDestination.Tab(MainTab.Home)
    } else {
      navigateBack()
    }
  }

  // Dialog states
  var studentToEdit by remember { mutableStateOf<Student?>(null) }
  var showAddEditStudentDialog by remember { mutableStateOf(false) }
  var showCollectFeeDialog by remember { mutableStateOf(false) }
  var studentForFeeCollection by remember { mutableStateOf<Student?>(null) }

  val whatsAppDialogData by viewModel.whatsAppDialogState.collectAsStateWithLifecycle()
  val bulkProgress by viewModel.bulkProgress.collectAsStateWithLifecycle()
  val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val activeStudents by viewModel.activeStudents.collectAsStateWithLifecycle()
  val allChalans by viewModel.allChalans.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(userMessage) {
    userMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      viewModel.clearUserMessage()
    }
  }

  val isBottomBarVisible = currentDestination is AppDestination.Tab

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.safeDrawing),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      if (isBottomBarVisible) {
        val currentTab = (currentDestination as AppDestination.Tab).tab
        NavigationBar(
          modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("bottom_nav_bar"),
          containerColor = MaterialTheme.colorScheme.surface,
          tonalElevation = 8.dp
        ) {
          MainTab.entries.forEach { tab ->
            NavigationBarItem(
              selected = currentTab == tab,
              onClick = {
                currentDestination = AppDestination.Tab(tab)
              },
              icon = {
                Icon(
                  imageVector = tab.icon,
                  contentDescription = tab.title
                )
              },
              label = {
                Text(
                  text = tab.title,
                  fontSize = 11.sp,
                  fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Medium
                )
              },
              modifier = Modifier.testTag("nav_${tab.name.lowercase()}"),
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
              )
            )
          }
        }
      }
    }
  ) { padding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      when (val dest = currentDestination) {
        is AppDestination.Tab -> {
          when (dest.tab) {
            MainTab.Home -> {
              DashboardScreen(
                viewModel = viewModel,
                onNavigateToStudents = { currentDestination = AppDestination.Tab(MainTab.Students) },
                onNavigateToClasses = { currentDestination = AppDestination.Tab(MainTab.Classes) },
                onNavigateToFees = { currentDestination = AppDestination.Tab(MainTab.Fees) },
                onNavigateToAttendance = { navigateTo(AppDestination.Attendance) },
                onNavigateToTests = { navigateTo(AppDestination.Tests) },
                onNavigateToHomework = { navigateTo(AppDestination.Homework) },
                onNavigateToSettings = { navigateTo(AppDestination.Settings) },
                onOpenAddStudent = {
                  studentToEdit = null
                  showAddEditStudentDialog = true
                },
                onOpenCollectFee = {
                  studentForFeeCollection = null
                  showCollectFeeDialog = true
                },
                onOpenGenerateChalan = {
                  currentDestination = AppDestination.Tab(MainTab.Fees)
                }
              )
            }
            MainTab.Students -> {
              StudentsScreen(
                viewModel = viewModel,
                onStudentClick = { studentId ->
                  navigateTo(AppDestination.StudentDetail(studentId))
                },
                onAddStudentClick = {
                  studentToEdit = null
                  showAddEditStudentDialog = true
                }
              )
            }
            MainTab.Classes -> {
              ClassesScreen(viewModel = viewModel)
            }
            MainTab.Fees -> {
              FeesScreen(viewModel = viewModel)
            }
            MainTab.More -> {
              MoreHubScreen(
                viewModel = viewModel,
                onNavigateToAttendance = { navigateTo(AppDestination.Attendance) },
                onNavigateToTests = { navigateTo(AppDestination.Tests) },
                onNavigateToHomework = { navigateTo(AppDestination.Homework) },
                onNavigateToPerformance = { navigateTo(AppDestination.Performance) },
                onNavigateToReports = { navigateTo(AppDestination.Reports) },
                onNavigateToSettings = { navigateTo(AppDestination.Settings) }
              )
            }
          }
        }
        is AppDestination.StudentDetail -> {
          StudentDetailScreen(
            studentId = dest.studentId,
            viewModel = viewModel,
            onBack = { navigateBack() },
            onEditStudent = { student ->
              studentToEdit = student
              showAddEditStudentDialog = true
            },
            onOpenCollectFee = { student ->
              studentForFeeCollection = student
              showCollectFeeDialog = true
            },
            onOpenGenerateChalan = { student ->
              currentDestination = AppDestination.Tab(MainTab.Fees)
            }
          )
        }
        AppDestination.Attendance -> {
          AttendanceScreen(
            viewModel = viewModel,
            onBack = { navigateBack() }
          )
        }
        AppDestination.Tests -> {
          TestsScreen(
            viewModel = viewModel,
            onBack = { navigateBack() }
          )
        }
        AppDestination.Homework -> {
          HomeworkScreen(
            viewModel = viewModel,
            onBack = { navigateBack() }
          )
        }
        AppDestination.Performance -> {
          PerformanceScreen(
            viewModel = viewModel,
            onBack = { navigateBack() }
          )
        }
        AppDestination.Reports -> {
          ReportsScreen(
            viewModel = viewModel,
            onBack = { navigateBack() }
          )
        }
        AppDestination.Settings -> {
          SettingsScreen(
            viewModel = viewModel,
            onBack = { navigateBack() }
          )
        }
      }
    }
  }

  // WhatsApp Composer Dialog Overlay
  if (whatsAppDialogData != null) {
    WhatsAppComposerDialog(
      data = whatsAppDialogData!!,
      settings = settings ?: TeacherSettings(),
      onDismiss = { viewModel.dismissWhatsAppDialog() }
    )
  }

  // Bulk Chalan Progress Dialog Overlay
  if (bulkProgress != null) {
    BulkChalanProgressDialog(
      current = bulkProgress!!.first,
      total = bulkProgress!!.second
    )
  }

  // Add/Edit Student Dialog Overlay
  if (showAddEditStudentDialog) {
    AddEditStudentDialog(
      student = studentToEdit,
      defaultFee = settings?.defaultFee ?: 3500.0,
      onDismiss = { showAddEditStudentDialog = false },
      onSave = { student ->
        viewModel.saveStudent(student)
        showAddEditStudentDialog = false
      }
    )
  }

  // Collect Fee Dialog Overlay
  if (showCollectFeeDialog) {
    CollectFeeDialog(
      students = activeStudents,
      chalans = allChalans,
      onDismiss = { showCollectFeeDialog = false },
      onConfirm = { studentId, month, amount, method, ref, notes, chalanId ->
        viewModel.recordPayment(studentId, month, amount, method, ref, notes, chalanId)
        showCollectFeeDialog = false
      }
    )
  }
}
