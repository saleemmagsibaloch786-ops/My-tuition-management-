package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Student
import com.example.data.model.TestMark
import com.example.data.model.TestRecord
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.viewmodel.TuitionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestsScreen(
  viewModel: TuitionViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val allTests by viewModel.allTests.collectAsStateWithLifecycle()
  val activeStudents by viewModel.activeStudents.collectAsStateWithLifecycle()

  var showCreateTestDialog by remember { mutableStateOf(false) }
  var testForMarksEntry by remember { mutableStateOf<TestRecord?>(null) }

  Scaffold(
    modifier = modifier.fillMaxSize().testTag("tests_screen"),
    topBar = {
      TopAppBar(
        title = { Text("Tests & Assessment", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showCreateTestDialog = true },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White
      ) {
        Icon(Icons.Default.Add, contentDescription = "New Test")
      }
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      if (allTests.isEmpty()) {
        EmptyStateView(
          icon = Icons.Default.Assignment,
          title = "No Tests Created",
          message = "Create regular monthly or weekly class tests to record marks, calculate grades, and share reports with parents.",
          actionButtonText = "Create New Test",
          onActionClick = { showCreateTestDialog = true }
        )
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(allTests, key = { it.id }) { test ->
            TestCard(
              test = test,
              onEnterMarks = { testForMarksEntry = test },
              onDelete = { viewModel.deleteTest(test) }
            )
          }
          item { Spacer(modifier = Modifier.height(70.dp)) }
        }
      }
    }
  }

  if (showCreateTestDialog) {
    CreateTestDialog(
      onDismiss = { showCreateTestDialog = false },
      onSave = { title, className, subject, marks, date ->
        viewModel.addTest(title, className, subject, marks, date)
        showCreateTestDialog = false
      }
    )
  }

  if (testForMarksEntry != null) {
    val currentTest = testForMarksEntry!!
    val studentsInClass = activeStudents.filter { it.className == currentTest.className }.ifEmpty { activeStudents }
    EnterMarksDialog(
      test = currentTest,
      students = studentsInClass,
      onDismiss = { testForMarksEntry = null },
      onSave = { studentId, marks, remarks ->
        viewModel.recordTestMark(currentTest.id, studentId, marks, currentTest.totalMarks, remarks)
        testForMarksEntry = null
      }
    )
  }
}

@Composable
fun TestCard(
  test: TestRecord,
  onEnterMarks: () -> Unit,
  onDelete: () -> Unit
) {
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
        Column(modifier = Modifier.weight(1f)) {
          Text(test.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Text("${test.className} • ${test.subject}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onDelete) {
          Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
        }
      }

      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Total Marks: ${test.totalMarks.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        Text("Date: ${test.testDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }

      Spacer(modifier = Modifier.height(10.dp))
      Button(
        onClick = onEnterMarks,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
      ) {
        Icon(Icons.Default.Grade, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Record Student Marks & Grade")
      }
    }
  }
}

@Composable
fun CreateTestDialog(
  onDismiss: () -> Unit,
  onSave: (title: String, className: String, subject: String, totalMarks: Double, date: String) -> Unit
) {
  var title by remember { mutableStateOf("Weekly Mathematics Test") }
  var className by remember { mutableStateOf("Class 10") }
  var subject by remember { mutableStateOf("Mathematics") }
  var totalMarksStr by remember { mutableStateOf("50") }
  var dateStr by remember {
    mutableStateOf(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()))
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Create New Test", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Test Title *") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = className,
            onValueChange = { className = it },
            label = { Text("Class *") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
          )
          OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject *") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = totalMarksStr,
            onValueChange = { totalMarksStr = it },
            label = { Text("Total Marks *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
          )
          OutlinedTextField(
            value = dateStr,
            onValueChange = { dateStr = it },
            label = { Text("Test Date") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val marks = totalMarksStr.toDoubleOrNull() ?: 50.0
          onSave(title.trim(), className.trim(), subject.trim(), marks, dateStr)
        },
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("Create Test")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterMarksDialog(
  test: TestRecord,
  students: List<Student>,
  onDismiss: () -> Unit,
  onSave: (studentId: Long, marks: Double, remarks: String) -> Unit
) {
  var selectedStudent by remember { mutableStateOf(students.firstOrNull()) }
  var expanded by remember { mutableStateOf(false) }
  var marksStr by remember { mutableStateOf("") }
  var remarks by remember { mutableStateOf("Good effort") }

  val marksNum = marksStr.toDoubleOrNull() ?: 0.0
  val percentage = if (test.totalMarks > 0) (marksNum / test.totalMarks) * 100.0 else 0.0
  val grade = when {
    percentage >= 85 -> "A+"
    percentage >= 75 -> "A"
    percentage >= 65 -> "B"
    percentage >= 55 -> "C"
    percentage >= 45 -> "D"
    else -> "F"
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Record Marks (${test.title})", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ExposedDropdownMenuBox(
          expanded = expanded,
          onExpandedChange = { expanded = !expanded }
        ) {
          OutlinedTextField(
            value = selectedStudent?.name ?: "Select Student",
            onValueChange = {},
            readOnly = true,
            label = { Text("Student *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )
          ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
          ) {
            students.forEach { s ->
              DropdownMenuItem(
                text = { Text("${s.name} (${s.className})") },
                onClick = {
                  selectedStudent = s
                  expanded = false
                }
              )
            }
          }
        }

        OutlinedTextField(
          value = marksStr,
          onValueChange = { marksStr = it },
          label = { Text("Marks Obtained (Out of ${test.totalMarks.toInt()}) *") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        if (marksStr.isNotBlank()) {
          Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Percentage: ${percentage.toInt()}%", fontWeight = FontWeight.Bold)
              Text("Calculated Grade: $grade", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
          }
        }

        OutlinedTextField(
          value = remarks,
          onValueChange = { remarks = it },
          label = { Text("Remarks for Parent") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val s = selectedStudent ?: return@Button
          onSave(s.id, marksNum, remarks)
        },
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("Save & Calculate")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
