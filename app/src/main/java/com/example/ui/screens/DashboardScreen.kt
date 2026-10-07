package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TeacherSettings
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.SyncStatusPill
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.TertiaryEmerald
import com.example.ui.viewmodel.TuitionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
  viewModel: TuitionViewModel,
  onNavigateToStudents: () -> Unit,
  onNavigateToClasses: () -> Unit,
  onNavigateToFees: () -> Unit,
  onNavigateToAttendance: () -> Unit,
  onNavigateToTests: () -> Unit,
  onNavigateToHomework: () -> Unit,
  onNavigateToSettings: () -> Unit,
  onOpenAddStudent: () -> Unit,
  onOpenCollectFee: () -> Unit,
  onOpenGenerateChalan: () -> Unit,
  modifier: Modifier = Modifier
) {
  val activeStudents by viewModel.activeStudents.collectAsStateWithLifecycle()
  val allClasses by viewModel.allClasses.collectAsStateWithLifecycle()
  val allAttendance by viewModel.allAttendance.collectAsStateWithLifecycle()
  val allChalans by viewModel.allChalans.collectAsStateWithLifecycle()
  val allReceipts by viewModel.allReceipts.collectAsStateWithLifecycle()
  val allTests by viewModel.allTests.collectAsStateWithLifecycle()
  val allHomework by viewModel.allHomework.collectAsStateWithLifecycle()
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val currentSettings = settings ?: TeacherSettings()

  val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
  val todayDisplay = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())

  // Calculations
  val todayAttendance = allAttendance.filter { it.date == today }
  val presentCount = todayAttendance.count { it.status == "Present" }
  val absentCount = todayAttendance.count { it.status == "Absent" }

  val pendingFeesSum = allChalans.filter { it.status == "Pending" || it.status == "Overdue" || it.status == "Partial" }
    .sumOf { it.totalPayable - it.paidAmount }

  val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
  val currentMonthCollection = allReceipts.filter { it.feeMonthYear.contains(currentMonth.split(" ")[0]) }
    .sumOf { it.amountPaid }

  val pendingHomework = allHomework.filter { it.status == "Assigned" }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("dashboard_screen"),
    contentPadding = PaddingValues(bottom = 90.dp)
  ) {
    // Top Teacher Greeting Header
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
              )
            )
          )
          .padding(top = 18.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = currentSettings.tuitionName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Welcome, ${currentSettings.teacherName}",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f)
              )
            }
            SyncStatusPill(
              syncStatus = currentSettings.syncStatus,
              onClick = { viewModel.triggerSync() }
            )
          }

          Spacer(modifier = Modifier.height(14.dp))
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.15f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = todayDisplay,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
              )
              Text(
                text = "Active: ${activeStudents.size} Students",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFEF3C7)
              )
            }
          }
        }
      }
    }

    // Quick Action Bar
    item {
      Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
        Text(
          text = "Quick Actions",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          item {
            QuickActionButton(
              title = "Add Student",
              icon = Icons.Default.Add,
              color = MaterialTheme.colorScheme.primary,
              onClick = onOpenAddStudent
            )
          }
          item {
            QuickActionButton(
              title = "Attendance",
              icon = Icons.Default.CheckCircle,
              color = TertiaryEmerald,
              onClick = onNavigateToAttendance
            )
          }
          item {
            QuickActionButton(
              title = "Collect Fee",
              icon = Icons.Default.Payment,
              color = SecondaryAmber,
              onClick = onOpenCollectFee
            )
          }
          item {
            QuickActionButton(
              title = "Gen Chalan",
              icon = Icons.Default.ReceiptLong,
              color = Color(0xFF0284C7),
              onClick = onOpenGenerateChalan
            )
          }
          item {
            QuickActionButton(
              title = "Add Test",
              icon = Icons.Default.Assignment,
              color = Color(0xFF7C3AED),
              onClick = onNavigateToTests
            )
          }
          item {
            QuickActionButton(
              title = "Homework",
              icon = Icons.Default.MenuBook,
              color = Color(0xFFD97706),
              onClick = onNavigateToHomework
            )
          }
          item {
            QuickActionButton(
              title = "WhatsApp",
              icon = Icons.Default.Chat,
              color = Color(0xFF25D366),
              onClick = { viewModel.openCustomWhatsApp() }
            )
          }
        }
      }
    }

    // Dashboard Overview Cards
    item {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
        Text(
          text = "Tuition Summary",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Grid Row 1
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          StatCard(
            title = "Active Students",
            value = "${activeStudents.size}",
            icon = Icons.Default.School,
            accentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
            onClick = onNavigateToStudents
          )
          StatCard(
            title = "Month Collection",
            value = "Rs. ${currentMonthCollection.toInt()}",
            icon = Icons.Default.TrendingUp,
            accentColor = TertiaryEmerald,
            modifier = Modifier.weight(1f),
            subtitle = currentMonth.split(" ")[0],
            onClick = onNavigateToFees
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid Row 2
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          StatCard(
            title = "Today's Attendance",
            value = "$presentCount P / $absentCount A",
            icon = Icons.Default.CheckCircle,
            accentColor = if (absentCount > 0) SecondaryAmber else TertiaryEmerald,
            modifier = Modifier.weight(1f),
            subtitle = if (todayAttendance.isEmpty()) "Not marked" else "${(presentCount * 100 / (presentCount + absentCount).coerceAtLeast(1))}% present",
            onClick = onNavigateToAttendance
          )
          StatCard(
            title = "Pending Fees",
            value = "Rs. ${pendingFeesSum.toInt()}",
            icon = Icons.Default.Receipt,
            accentColor = if (pendingFeesSum > 0) ErrorCrimson else TertiaryEmerald,
            modifier = Modifier.weight(1f),
            subtitle = "${allChalans.count { it.status == "Pending" || it.status == "Overdue" }} chalans",
            onClick = onNavigateToFees
          )
        }
      }
    }

    // Today's Classes Schedule
    item {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Today's Classes",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "View Schedule",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { onNavigateToClasses() }
          )
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (allClasses.isEmpty()) {
          EmptyStateView(
            icon = Icons.Default.CalendarMonth,
            title = "No Classes Added",
            message = "Add tuition batches and lecture timings to organize your schedule.",
            actionButtonText = "Add Class",
            onActionClick = onNavigateToClasses
          )
        } else {
          allClasses.take(3).forEach { schedule ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "${schedule.className} • ${schedule.subject}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                  Text(
                    text = "${schedule.startTime} - ${schedule.endTime} (${schedule.dayOfWeek})",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  if (schedule.studentNames.isNotBlank()) {
                    Text(
                      text = "Students: ${schedule.studentNames}",
                      fontSize = 11.sp,
                      color = MaterialTheme.colorScheme.primary
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // Upcoming Tests & Pending Homework side-by-side or stacked
    item {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Upcoming Tests & Homework",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          )
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (allTests.isNotEmpty()) {
          val latestTest = allTests.first()
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onNavigateToTests() }
              .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF))
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Assignment,
                contentDescription = null,
                tint = Color(0xFF7C3AED),
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Test: ${latestTest.title}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = Color(0xFF581C87)
                )
                Text(
                  text = "${latestTest.className} (${latestTest.subject}) • ${latestTest.testDate} • Total Marks: ${latestTest.totalMarks.toInt()}",
                  fontSize = 11.sp,
                  color = Color(0xFF6B21A8)
                )
              }
            }
          }
        }

        if (pendingHomework.isNotEmpty()) {
          val hw = pendingHomework.first()
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onNavigateToHomework() }
              .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                tint = Color(0xFFD97706),
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Homework: ${hw.title}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = Color(0xFF78350F)
                )
                Text(
                  text = "${hw.className} (${hw.subject}) • Due: ${hw.dueDate}",
                  fontSize = 11.sp,
                  color = Color(0xFF92400E)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun QuickActionButton(
  title: String,
  icon: ImageVector,
  color: Color,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .padding(horizontal = 6.dp, vertical = 4.dp)
      .testTag("quick_action_${title.lowercase().replace(" ", "_")}")
  ) {
    Box(
      modifier = Modifier
        .size(48.dp)
        .background(color.copy(alpha = 0.15f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = color,
        modifier = Modifier.size(22.dp)
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = title,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}
