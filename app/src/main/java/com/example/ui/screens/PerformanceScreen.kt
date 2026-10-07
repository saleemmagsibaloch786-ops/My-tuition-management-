package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Student
import com.example.data.model.StudentPerformance
import com.example.data.model.TeacherSettings
import com.example.service.PdfGenerator
import com.example.service.WhatsAppHelper
import com.example.ui.theme.SecondaryAmber
import com.example.ui.viewmodel.TuitionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
  viewModel: TuitionViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val activeStudents by viewModel.activeStudents.collectAsStateWithLifecycle()
  val allPerformance by viewModel.allPerformance.collectAsStateWithLifecycle()
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val currentSettings = settings ?: TeacherSettings()

  var selectedStudent by remember { mutableStateOf(activeStudents.firstOrNull()) }
  var expanded by remember { mutableStateOf(false) }

  val defaultMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
  var monthYear by remember { mutableStateOf(defaultMonth) }

  var reading by remember { mutableIntStateOf(4) }
  var writing by remember { mutableIntStateOf(4) }
  var math by remember { mutableIntStateOf(4) }
  var english by remember { mutableIntStateOf(4) }
  var homework by remember { mutableIntStateOf(4) }
  var participation by remember { mutableIntStateOf(4) }
  var behaviour by remember { mutableIntStateOf(5) }
  var remarks by remember { mutableStateOf("Consistent performance, polite behaviour and regular homework submission.") }

  Scaffold(
    modifier = modifier.fillMaxSize().testTag("performance_screen"),
    topBar = {
      TopAppBar(
        title = { Text("Student Performance Rubrics", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      )
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("Monthly Assessment Entry", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))

            // Student Picker
            ExposedDropdownMenuBox(
              expanded = expanded,
              onExpandedChange = { expanded = !expanded }
            ) {
              OutlinedTextField(
                value = selectedStudent?.name ?: "Select Student",
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Student *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
              )
              ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
              ) {
                activeStudents.forEach { s ->
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

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
              value = monthYear,
              onValueChange = { monthYear = it },
              label = { Text("Evaluation Month") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))
            Text("Rate Performance (1 to 5 Stars):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))

            StarRatingRow("Reading Skills", reading) { reading = it }
            StarRatingRow("Writing & Neatness", writing) { writing = it }
            StarRatingRow("Mathematics", math) { math = it }
            StarRatingRow("English", english) { english = it }
            StarRatingRow("Homework Regularity", homework) { homework = it }
            StarRatingRow("Class Participation", participation) { participation = it }
            StarRatingRow("Behaviour & Discipline", behaviour) { behaviour = it }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
              value = remarks,
              onValueChange = { remarks = it },
              label = { Text("Teacher Remarks for Parents") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = {
                  val s = selectedStudent ?: return@Button
                  val perf = StudentPerformance(
                    studentId = s.id,
                    studentName = s.name,
                    className = s.className,
                    monthYear = monthYear,
                    readingRating = reading,
                    writingRating = writing,
                    mathRating = math,
                    englishRating = english,
                    homeworkRating = homework,
                    participationRating = participation,
                    behaviourRating = behaviour,
                    teacherRemarks = remarks
                  )
                  viewModel.savePerformance(perf)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("Save Report")
              }
              OutlinedButton(
                onClick = {
                  val s = selectedStudent ?: return@OutlinedButton
                  val perf = StudentPerformance(
                    studentId = s.id,
                    studentName = s.name,
                    className = s.className,
                    monthYear = monthYear,
                    readingRating = reading,
                    writingRating = writing,
                    mathRating = math,
                    englishRating = english,
                    homeworkRating = homework,
                    participationRating = participation,
                    behaviourRating = behaviour,
                    teacherRemarks = remarks
                  )
                  val pdf = PdfGenerator.generateStudentReportPdf(context, s, 95, emptyList(), perf, currentSettings)
                  WhatsAppHelper.sharePdf(context, pdf, "Monthly Progress Report for ${s.name} ($monthYear)")
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Generate PDF")
              }
            }
          }
        }
      }

      // Past evaluations
      item {
        Text("Recorded Performance Reports", fontWeight = FontWeight.Bold, fontSize = 15.sp)
      }

      items(allPerformance, key = { it.id }) { perf ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(perf.studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Text(perf.monthYear, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
            Text("${perf.className} • Behaviour: ${perf.behaviourRating}/5 • Homework: ${perf.homeworkRating}/5", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (perf.teacherRemarks.isNotBlank()) {
              Spacer(modifier = Modifier.height(4.dp))
              Text("Remarks: \"${perf.teacherRemarks}\"", fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
              onClick = { viewModel.preparePerformanceMessage(perf) },
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF25D366))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Send on WhatsApp", fontSize = 11.sp)
            }
          }
        }
      }
      item { Spacer(modifier = Modifier.height(70.dp)) }
    }
  }
}

@Composable
fun StarRatingRow(title: String, current: Int, onSelect: (Int) -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(title, fontSize = 12.sp)
    Row {
      (1..5).forEach { star ->
        Icon(
          imageVector = Icons.Default.Star,
          contentDescription = null,
          tint = if (star <= current) SecondaryAmber else Color(0xFFCBD5E1),
          modifier = Modifier
            .size(22.dp)
            .clickable { onSelect(star) }
            .padding(2.dp)
        )
      }
    }
  }
}
