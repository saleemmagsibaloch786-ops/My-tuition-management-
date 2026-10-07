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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AttendanceRecord
import com.example.data.model.Student
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.TertiaryEmerald
import com.example.ui.viewmodel.TuitionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
  viewModel: TuitionViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val activeStudents by viewModel.activeStudents.collectAsStateWithLifecycle()
  val allAttendance by viewModel.allAttendance.collectAsStateWithLifecycle()

  var selectedDate by remember {
    mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
  }
  var selectedClass by remember { mutableStateOf("All") }

  val dateDisplay = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())

  // Classes for filter
  val classesList = listOf("All") + activeStudents.map { it.className }.distinct()

  val filteredStudents = activeStudents.filter {
    if (selectedClass == "All") true else it.className == selectedClass
  }

  val todayAttendanceMap = allAttendance
    .filter { it.date == selectedDate }
    .associateBy { it.studentId }

  val presentCount = todayAttendanceMap.values.count { it.status == "Present" }
  val absentCount = todayAttendanceMap.values.count { it.status == "Absent" }
  val totalMarked = todayAttendanceMap.size
  val attendancePct = if (totalMarked > 0) (presentCount * 100 / totalMarked) else 0

  Column(
    modifier = modifier.fillMaxSize().testTag("attendance_screen")
  ) {
    TopAppBar(
      title = { Text("Daily Attendance Register", fontWeight = FontWeight.Bold) },
      navigationIcon = {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
      }
    )

    // Summary Card
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(dateDisplay, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(
              text = "Marked: $totalMarked/${activeStudents.size} Students",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (attendancePct >= 75) Color(0xFFD1FAE5) else Color(0xFFFEF3C7)
          ) {
            Text(
              text = "$attendancePct% Present",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = if (attendancePct >= 75) Color(0xFF065F46) else Color(0xFF92400E),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = { viewModel.markAllPresent(selectedDate, selectedClass) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TertiaryEmerald)
          ) {
            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Mark All Present")
          }
        }
      }
    }

    // Class Filter Chips
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      items(classesList) { c ->
        FilterChip(
          selected = selectedClass == c,
          onClick = { selectedClass = c },
          label = { Text(c) }
        )
      }
    }

    // Students list with 1-tap attendance buttons
    if (filteredStudents.isEmpty()) {
      EmptyStateView(
        icon = Icons.Default.CheckCircle,
        title = "No Students",
        message = "Add students in this class to record attendance."
      )
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredStudents, key = { it.id }) { student ->
          val currentRecord = todayAttendanceMap[student.id]
          val currentStatus = currentRecord?.status ?: "Unmarked"

          StudentAttendanceCard(
            student = student,
            currentStatus = currentStatus,
            onStatusSelect = { status ->
              viewModel.recordAttendance(student, selectedDate, status)
            }
          )
        }
        item {
          Spacer(modifier = Modifier.height(70.dp))
        }
      }
    }
  }
}

@Composable
fun StudentAttendanceCard(
  student: Student,
  currentStatus: String,
  onStatusSelect: (String) -> Unit
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = student.name.firstOrNull()?.uppercase() ?: "S",
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(student.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("${student.className} • Roll: ${student.studentId}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
        StatusBadge(currentStatus)
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 4 One-tap chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        AttendanceTapChip(
          title = "Present",
          isSelected = currentStatus == "Present",
          color = TertiaryEmerald,
          modifier = Modifier.weight(1f),
          onClick = { onStatusSelect("Present") }
        )
        AttendanceTapChip(
          title = "Absent",
          isSelected = currentStatus == "Absent",
          color = ErrorCrimson,
          modifier = Modifier.weight(1f),
          onClick = { onStatusSelect("Absent") }
        )
        AttendanceTapChip(
          title = "Late",
          isSelected = currentStatus == "Late",
          color = SecondaryAmber,
          modifier = Modifier.weight(1f),
          onClick = { onStatusSelect("Late") }
        )
        AttendanceTapChip(
          title = "Leave",
          isSelected = currentStatus == "Leave",
          color = Color(0xFF6366F1),
          modifier = Modifier.weight(1f),
          onClick = { onStatusSelect("Leave") }
        )
      }
    }
  }
}

@Composable
fun AttendanceTapChip(
  title: String,
  isSelected: Boolean,
  color: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = if (isSelected) color else color.copy(alpha = 0.1f),
    modifier = modifier
      .clickable { onClick() }
      .testTag("attendance_${title.lowercase()}_chip")
  ) {
    Box(
      modifier = Modifier.padding(vertical = 8.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) Color.White else color
      )
    }
  }
}
