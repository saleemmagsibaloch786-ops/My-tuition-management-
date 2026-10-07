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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.ClassSchedule
import com.example.ui.components.EmptyStateView
import com.example.ui.viewmodel.TuitionViewModel

@Composable
fun ClassesScreen(
  viewModel: TuitionViewModel,
  modifier: Modifier = Modifier
) {
  val allClasses by viewModel.allClasses.collectAsStateWithLifecycle()
  var selectedDay by remember { mutableStateOf("All") }
  var showAddDialog by remember { mutableStateOf(false) }

  val days = listOf("All", "Daily", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

  val filteredClasses = allClasses.filter {
    if (selectedDay == "All") true
    else it.dayOfWeek.contains(selectedDay, ignoreCase = true) || it.dayOfWeek == "Daily"
  }

  Scaffold(
    modifier = modifier.fillMaxSize().testTag("classes_screen"),
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showAddDialog = true },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Class")
      }
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      // Days Filter Chips
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(days) { day ->
          FilterChip(
            selected = selectedDay == day,
            onClick = { selectedDay = day },
            label = { Text(day) }
          )
        }
      }

      if (filteredClasses.isEmpty()) {
        EmptyStateView(
          icon = Icons.Default.CalendarMonth,
          title = "No Class Schedules",
          message = "Schedule class batches with timings and assigned tuition students.",
          actionButtonText = "Create Class",
          onActionClick = { showAddDialog = true }
        )
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredClasses, key = { it.id }) { schedule ->
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                      )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(
                        text = schedule.className,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                      )
                      Text(
                        text = schedule.subject,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }
                  IconButton(onClick = { viewModel.deleteClass(schedule) }) {
                    Icon(
                      imageVector = Icons.Default.Delete,
                      contentDescription = "Delete",
                      tint = MaterialTheme.colorScheme.error,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = "Time: ${schedule.startTime} - ${schedule.endTime}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                  )
                  Text(
                    text = "Day: ${schedule.dayOfWeek}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                if (schedule.studentNames.isNotBlank()) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Enrolled Students: ${schedule.studentNames}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                if (schedule.notes.isNotBlank()) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Notes: ${schedule.notes}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
          item {
            Spacer(modifier = Modifier.height(70.dp))
          }
        }
      }
    }
  }

  if (showAddDialog) {
    AddClassDialog(
      onDismiss = { showAddDialog = false },
      onSave = { schedule ->
        viewModel.saveClass(schedule)
        showAddDialog = false
      }
    )
  }
}

@Composable
private fun AddClassDialog(
  onDismiss: () -> Unit,
  onSave: (ClassSchedule) -> Unit
) {
  var className by remember { mutableStateOf("Class 10") }
  var subject by remember { mutableStateOf("Mathematics") }
  var dayOfWeek by remember { mutableStateOf("Daily") }
  var startTime by remember { mutableStateOf("04:00 PM") }
  var endTime by remember { mutableStateOf("05:30 PM") }
  var studentNames by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Class Schedule", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = className,
          onValueChange = { className = it },
          label = { Text("Class / Batch Name") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )
        OutlinedTextField(
          value = subject,
          onValueChange = { subject = it },
          label = { Text("Subject") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )
        OutlinedTextField(
          value = dayOfWeek,
          onValueChange = { dayOfWeek = it },
          label = { Text("Days (e.g. Daily, Mon-Fri)") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = startTime,
            onValueChange = { startTime = it },
            label = { Text("Start Time") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
          )
          OutlinedTextField(
            value = endTime,
            onValueChange = { endTime = it },
            label = { Text("End Time") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
          )
        }
        OutlinedTextField(
          value = studentNames,
          onValueChange = { studentNames = it },
          label = { Text("Enrolled Students") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Notes / Topic") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(
            ClassSchedule(
              className = className.trim(),
              subject = subject.trim(),
              dayOfWeek = dayOfWeek.trim(),
              startTime = startTime.trim(),
              endTime = endTime.trim(),
              studentNames = studentNames.trim(),
              notes = notes.trim()
            )
          )
        },
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("Save Class")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
