package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TeacherSettings
import com.example.service.PdfGenerator
import com.example.service.WhatsAppHelper
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.TertiaryEmerald
import com.example.ui.viewmodel.TuitionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
  viewModel: TuitionViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val activeStudents by viewModel.activeStudents.collectAsStateWithLifecycle()
  val allAttendance by viewModel.allAttendance.collectAsStateWithLifecycle()
  val allChalans by viewModel.allChalans.collectAsStateWithLifecycle()
  val allReceipts by viewModel.allReceipts.collectAsStateWithLifecycle()
  val allTests by viewModel.allTests.collectAsStateWithLifecycle()
  val allPerformance by viewModel.allPerformance.collectAsStateWithLifecycle()
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val currentSettings = settings ?: TeacherSettings()

  val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
  val totalCollected = allReceipts.sumOf { it.amountPaid }
  val totalPending = allChalans.filter { it.status != "Paid" }.sumOf { it.totalPayable - it.paidAmount }

  Scaffold(
    modifier = modifier.fillMaxSize().testTag("reports_screen"),
    topBar = {
      TopAppBar(
        title = { Text("Reports & Analytics", fontWeight = FontWeight.Bold) },
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
        ReportActionCard(
          title = "Fee Collection & Outstanding Report",
          description = "Collected: Rs. ${totalCollected.toInt()} • Outstanding: Rs. ${totalPending.toInt()} • ${allChalans.size} Chalans issued",
          icon = Icons.Default.TrendingUp,
          accentColor = TertiaryEmerald,
          onGeneratePdf = {
            if (allChalans.isNotEmpty()) {
              val pdf = PdfGenerator.generateFeeChalanPdf(context, allChalans.first(), currentSettings)
              WhatsAppHelper.sharePdf(context, pdf, "Monthly Fee Collection Summary ($currentMonth)")
            } else {
              viewModel.showMessage("No chalans available yet to generate summary.")
            }
          }
        )
      }

      item {
        ReportActionCard(
          title = "Attendance Register Report",
          description = "Total attendance sessions logged: ${allAttendance.size}. Review presence and absence records.",
          icon = Icons.Default.CheckCircle,
          accentColor = PrimaryNavy,
          onGeneratePdf = {
            if (activeStudents.isNotEmpty()) {
              val s = activeStudents.first()
              val pdf = PdfGenerator.generateStudentReportPdf(context, s, 92, emptyList(), null, currentSettings)
              WhatsAppHelper.sharePdf(context, pdf, "Attendance & Performance Summary")
            }
          }
        )
      }

      item {
        ReportActionCard(
          title = "Student Progress & Report Cards",
          description = "Comprehensive report card with 7 rubrics (Reading, Writing, Math, English, Behaviour) and test marks.",
          icon = Icons.Default.School,
          accentColor = SecondaryAmber,
          onGeneratePdf = {
            if (activeStudents.isNotEmpty()) {
              val s = activeStudents.first()
              val perf = allPerformance.find { it.studentId == s.id }
              val pdf = PdfGenerator.generateStudentReportPdf(context, s, 90, emptyList(), perf, currentSettings)
              WhatsAppHelper.sharePdf(context, pdf, "Monthly Progress Report Card for ${s.name}")
            }
          }
        )
      }

      item {
        ReportActionCard(
          title = "Test Results & Marks Ledger",
          description = "${allTests.size} Tests created. Automated calculation of percentages, grades (A+, A, B, C, D, F).",
          icon = Icons.Default.Assignment,
          accentColor = Color(0xFF7C3AED),
          onGeneratePdf = {
            if (activeStudents.isNotEmpty()) {
              val s = activeStudents.first()
              val pdf = PdfGenerator.generateStudentReportPdf(context, s, 88, emptyList(), null, currentSettings)
              WhatsAppHelper.sharePdf(context, pdf, "Test Marks & Assessment Ledger")
            }
          }
        )
      }

      item {
        ReportActionCard(
          title = "Pending Arrears Notice Report",
          description = "List of students with overdue fees ready for WhatsApp reminder dispatch.",
          icon = Icons.Default.CurrencyRupee,
          accentColor = ErrorCrimson,
          onGeneratePdf = {
            val overdue = allChalans.filter { it.status == "Pending" || it.status == "Overdue" }
            if (overdue.isNotEmpty()) {
              val pdf = PdfGenerator.generateFeeChalanPdf(context, overdue.first(), currentSettings)
              WhatsAppHelper.sharePdf(context, pdf, "Fee Reminder and Chalan Notice")
            } else {
              viewModel.showMessage("All fees are cleared! No pending arrears.")
            }
          }
        )
      }
      item { Spacer(modifier = Modifier.height(70.dp)) }
    }
  }
}

@Composable
fun ReportActionCard(
  title: String,
  description: String,
  icon: ImageVector,
  accentColor: Color,
  onGeneratePdf: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .background(accentColor.copy(alpha = 0.15f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(22.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = title,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = description,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 18.sp
      )
      Spacer(modifier = Modifier.height(12.dp))
      Button(
        onClick = onGeneratePdf,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Generate & Share PDF Report")
      }
    }
  }
}
