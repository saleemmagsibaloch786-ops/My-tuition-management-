package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.HomeworkRecord
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.viewmodel.TuitionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkScreen(
  viewModel: TuitionViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val allHomework by viewModel.allHomework.collectAsStateWithLifecycle()
  var showAddDialog by remember { mutableStateOf(false) }

  Scaffold(
    modifier = modifier.fillMaxSize().testTag("homework_screen"),
    topBar = {
      TopAppBar(
        title = { Text("Homework Tracker", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showAddDialog = true },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Homework")
      }
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      if (allHomework.isEmpty()) {
        EmptyStateView(
          icon = Icons.Default.MenuBook,
          title = "No Homework Assigned",
          message = "Assign homework exercises to tuition classes and share directly with parents on WhatsApp.",
          actionButtonText = "Assign Homework",
          onActionClick = { showAddDialog = true }
        )
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(allHomework, key = { it.id }) { hw ->
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
                    Text(hw.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("${hw.className} • ${hw.subject}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                  StatusBadge(hw.status)
                }

                if (hw.description.isNotBlank()) {
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(hw.description, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Assigned: ${hw.assignedDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Text("Due: ${hw.dueDate}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  OutlinedButton(
                    onClick = { viewModel.toggleHomeworkStatus(hw) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Text("Status: ${hw.status}", fontSize = 11.sp)
                  }
                  Button(
                    onClick = { viewModel.prepareHomeworkMessage(hw) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                  ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", fontSize = 11.sp)
                  }
                  IconButton(
                    onClick = { viewModel.deleteHomework(hw) },
                    modifier = Modifier.size(36.dp)
                  ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                  }
                }
              }
            }
          }
          item { Spacer(modifier = Modifier.height(70.dp)) }
        }
      }
    }
  }

  if (showAddDialog) {
    CreateHomeworkDialog(
      onDismiss = { showAddDialog = false },
      onSave = { hw ->
        viewModel.addHomework(hw)
        showAddDialog = false
      }
    )
  }
}

@Composable
fun CreateHomeworkDialog(
  onDismiss: () -> Unit,
  onSave: (HomeworkRecord) -> Unit
) {
  var className by remember { mutableStateOf("Class 10") }
  var subject by remember { mutableStateOf("Mathematics") }
  var title by remember { mutableStateOf("Exercise 4.2 Questions 1 to 10") }
  var description by remember { mutableStateOf("Complete quadratic equation exercises in neat tuition register.") }
  val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
  var dueDate by remember { mutableStateOf("Tomorrow 4:00 PM") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Assign Tuition Homework", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Homework Title *") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )
        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Instructions / Details") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )
        OutlinedTextField(
          value = dueDate,
          onValueChange = { dueDate = it },
          label = { Text("Due Date / Time") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(
            HomeworkRecord(
              className = className.trim(),
              subject = subject.trim(),
              title = title.trim(),
              description = description.trim(),
              assignedDate = today,
              dueDate = dueDate.trim(),
              status = "Assigned"
            )
          )
        },
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("Assign & Share")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
