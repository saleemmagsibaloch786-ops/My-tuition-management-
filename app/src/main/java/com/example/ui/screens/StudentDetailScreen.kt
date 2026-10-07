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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Student
import com.example.data.model.TeacherSettings
import com.example.service.PdfGenerator
import com.example.service.WhatsAppHelper
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.TertiaryEmerald
import com.example.ui.viewmodel.TuitionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
  studentId: Long,
  viewModel: TuitionViewModel,
  onBack: () -> Unit,
  onEditStudent: (Student) -> Unit,
  onOpenCollectFee: (Student) -> Unit,
  onOpenGenerateChalan: (Student) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
  val student = allStudents.find { it.id == studentId }

  val allAttendance by viewModel.allAttendance.collectAsStateWithLifecycle()
  val studentAttendance = allAttendance.filter { it.studentId == studentId }

  val allChalans by viewModel.allChalans.collectAsStateWithLifecycle()
  val studentChalans = allChalans.filter { it.studentId == studentId }

  val allReceipts by viewModel.allReceipts.collectAsStateWithLifecycle()
  val studentReceipts = allReceipts.filter { it.studentId == studentId }

  val allTests by viewModel.allTests.collectAsStateWithLifecycle()
  val marksList by viewModel.allTests.collectAsStateWithLifecycle() // we can join
  val allHomework by viewModel.allHomework.collectAsStateWithLifecycle()
  val classHomework = if (student != null) allHomework.filter { it.className == student.className } else emptyList()

  val allPerformance by viewModel.allPerformance.collectAsStateWithLifecycle()
  val studentPerformance = allPerformance.filter { it.studentId == studentId }

  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val currentSettings = settings ?: TeacherSettings()

  var selectedTab by remember { mutableIntStateOf(0) }
  var showDeleteDialog by remember { mutableStateOf(false) }

  val tabs = listOf(
    "Overview",
    "Attendance",
    "Fees",
    "Chalans",
    "Receipts",
    "Tests",
    "Homework",
    "Performance"
  )

  if (student == null) {
    Column(
      modifier = modifier.fillMaxSize().padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text("Student not found", fontSize = 16.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(12.dp))
      Button(onClick = onBack) { Text("Go Back") }
    }
    return
  }

  // Attendance stats
  val totalDays = studentAttendance.size
  val presentDays = studentAttendance.count { it.status == "Present" }
  val attendancePct = if (totalDays > 0) (presentDays * 100 / totalDays) else 100

  Column(
    modifier = modifier.fillMaxSize().testTag("student_detail_screen")
  ) {
    // Top Bar
    TopAppBar(
      title = { Text(student.name, fontWeight = FontWeight.Bold, maxLines = 1) },
      navigationIcon = {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
      },
      actions = {
        IconButton(onClick = { viewModel.openCustomWhatsApp(student) }) {
          Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color(0xFF25D366))
        }
        IconButton(onClick = { onEditStudent(student) }) {
          Icon(Icons.Default.Edit, contentDescription = "Edit")
        }
        IconButton(onClick = { viewModel.toggleStudentStatus(student) }) {
          Icon(
            if (student.status == "Active") Icons.Default.Archive else Icons.Default.Unarchive,
            contentDescription = "Toggle Status"
          )
        }
        IconButton(onClick = { showDeleteDialog = true }) {
          Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface
      )
    )

    // Student Header Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
    ) {
      Row(
        modifier = Modifier.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(52.dp)
            .background(MaterialTheme.colorScheme.primary, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = student.name.firstOrNull()?.uppercase() ?: "S",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
          )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(student.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            StatusBadge(student.status)
          }
          Text(
            text = "${student.className} • Roll: ${student.studentId} • ${student.school}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Guardian: ${student.fatherGuardianName} (${student.parentWhatsApp})",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Scrollable Tabs
    ScrollableTabRow(
      selectedTabIndex = selectedTab,
      edgePadding = 16.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
        )
      }
    }

    // Tab Contents
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      when (selectedTab) {
        0 -> StudentOverviewTab(student, onEditClick = { onEditStudent(student) })
        1 -> StudentAttendanceTab(
          student = student,
          attendanceList = studentAttendance,
          attendancePct = attendancePct,
          onMarkAttendance = { status ->
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            viewModel.recordAttendance(student, today, status)
          }
        )
        2 -> StudentFeesTab(
          student = student,
          chalans = studentChalans,
          receipts = studentReceipts,
          onCollectFee = { onOpenCollectFee(student) },
          onGenerateChalan = { onOpenGenerateChalan(student) }
        )
        3 -> StudentChalansTab(
          chalans = studentChalans,
          settings = currentSettings,
          onGenerateChalan = { onOpenGenerateChalan(student) },
          onShareWhatsApp = { chalan -> viewModel.prepareChalanMessage(chalan) },
          onRemindWhatsApp = { chalan -> viewModel.prepareFeeReminderMessage(chalan) }
        )
        4 -> StudentReceiptsTab(
          receipts = studentReceipts,
          settings = currentSettings,
          onCollectFee = { onOpenCollectFee(student) },
          onShareWhatsApp = { receipt ->
            val msg = currentSettings.receiptTemplate
              .replace("{student}", receipt.studentName)
              .replace("{month}", receipt.feeMonthYear)
              .replace("{amount}", receipt.amountPaid.toInt().toString())
              .replace("{method}", receipt.paymentMethod)
              .replace("{remaining}", receipt.remainingBalance.toInt().toString())
              .replace("{tuition}", currentSettings.tuitionName)
            viewModel.dismissWhatsAppDialog()
            WhatsAppHelper.sendTextMessage(context, student.parentWhatsApp, msg)
          }
        )
        5 -> StudentTestsTab(
          studentId = studentId,
          allTests = allTests,
          viewModel = viewModel
        )
        6 -> StudentHomeworkTab(
          homeworkList = classHomework,
          onShareHomework = { hw -> viewModel.prepareHomeworkMessage(hw) }
        )
        7 -> StudentPerformanceTab(
          student = student,
          performanceList = studentPerformance,
          attendancePct = attendancePct,
          settings = currentSettings,
          onSavePerformance = { perf -> viewModel.savePerformance(perf) }
        )
      }
    }
  }

  // Delete Confirmation Dialog
  if (showDeleteDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteDialog = false },
      title = { Text("Delete Student?") },
      text = { Text("Are you sure you want to delete ${student.name}? All attendance, test and fee records will be permanently removed.") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteStudent(student)
            showDeleteDialog = false
            onBack()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
      }
    )
  }
}

// ---------------- SUB-TABS ----------------

@Composable
private fun StudentOverviewTab(student: Student, onEditClick: () -> Unit) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          DetailRow("Student ID", student.studentId)
          DetailRow("Full Name", student.name)
          DetailRow("Father / Guardian", student.fatherGuardianName)
          DetailRow("Parent WhatsApp", student.parentWhatsApp)
          DetailRow("Date of Birth", student.dob.ifBlank { "Not specified" })
          DetailRow("Gender", student.gender)
          DetailRow("School", student.school.ifBlank { "Not specified" })
          DetailRow("Class", student.className)
          DetailRow("Enrolled Subjects", student.subjects.ifBlank { "All Subjects" })
          DetailRow("Home Address", student.address.ifBlank { "Not specified" })
          DetailRow("Admission Date", student.admissionDate.ifBlank { "Recent" })
          DetailRow("Monthly Tuition Fee", "Rs. ${student.monthlyFee.toInt()}")
          DetailRow("Discount / Concession", "Rs. ${student.discount.toInt()}")
          DetailRow("Net Payable Monthly", "Rs. ${(student.monthlyFee - student.discount).toInt()}")
          DetailRow("Status", student.status)
          if (student.notes.isNotBlank()) {
            DetailRow("Teacher Notes", student.notes)
          }
        }
      }
    }
    item {
      Button(
        onClick = onEditClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Edit Student Details")
      }
      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
private fun DetailRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 5.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      fontSize = 12.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      fontSize = 13.sp,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
private fun StudentAttendanceTab(
  student: Student,
  attendanceList: List<com.example.data.model.AttendanceRecord>,
  attendancePct: Int,
  onMarkAttendance: (String) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Monthly Attendance Rate", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "$attendancePct%",
              fontSize = 28.sp,
              fontWeight = FontWeight.Bold,
              color = if (attendancePct >= 80) TertiaryEmerald else SecondaryAmber
            )
            Text(
              text = "Total Sessions: ${attendanceList.size}",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
          Text("Mark Today's Attendance:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            OutlinedButton(
              onClick = { onMarkAttendance("Present") },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp)
            ) { Text("Present", fontSize = 11.sp) }
            OutlinedButton(
              onClick = { onMarkAttendance("Absent") },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp)
            ) { Text("Absent", fontSize = 11.sp, color = MaterialTheme.colorScheme.error) }
            OutlinedButton(
              onClick = { onMarkAttendance("Late") },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp)
            ) { Text("Late", fontSize = 11.sp) }
            OutlinedButton(
              onClick = { onMarkAttendance("Leave") },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(8.dp)
            ) { Text("Leave", fontSize = 11.sp) }
          }
        }
      }
    }

    if (attendanceList.isEmpty()) {
      item {
        EmptyStateView(
          icon = Icons.Default.CheckCircle,
          title = "No Attendance Marked",
          message = "Attendance marked for this student will appear here."
        )
      }
    } else {
      items(attendanceList) { record ->
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(record.date, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            StatusBadge(record.status)
          }
        }
      }
    }
  }
}

@Composable
private fun StudentFeesTab(
  student: Student,
  chalans: List<com.example.data.model.FeeChalan>,
  receipts: List<com.example.data.model.PaymentReceipt>,
  onCollectFee: () -> Unit,
  onGenerateChalan: () -> Unit
) {
  val totalBilled = chalans.sumOf { it.totalPayable }
  val totalPaid = receipts.sumOf { it.amountPaid }
  val pending = (totalBilled - totalPaid).coerceAtLeast(0.0)

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Tuition Fee Summary", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Spacer(modifier = Modifier.height(10.dp))
          DetailRow("Monthly Fee", "Rs. ${student.monthlyFee.toInt()}")
          DetailRow("Monthly Discount", "Rs. ${student.discount.toInt()}")
          DetailRow("Total Chalans Issued", "Rs. ${totalBilled.toInt()}")
          DetailRow("Total Paid to Date", "Rs. ${totalPaid.toInt()}")
          DetailRow("Current Outstanding Balance", "Rs. ${pending.toInt()}")
          Spacer(modifier = Modifier.height(12.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = onCollectFee,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Collect Fee")
            }
            OutlinedButton(
              onClick = onGenerateChalan,
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Issue Chalan")
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StudentChalansTab(
  chalans: List<com.example.data.model.FeeChalan>,
  settings: TeacherSettings,
  onGenerateChalan: () -> Unit,
  onShareWhatsApp: (com.example.data.model.FeeChalan) -> Unit,
  onRemindWhatsApp: (com.example.data.model.FeeChalan) -> Unit
) {
  val context = LocalContext.current
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Button(
        onClick = onGenerateChalan,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
      ) {
        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Generate Monthly Fee Chalan")
      }
    }
    if (chalans.isEmpty()) {
      item {
        EmptyStateView(
          icon = Icons.Default.ReceiptLong,
          title = "No Chalans Issued",
          message = "Generate a fee chalan to send to parent with payment details."
        )
      }
    } else {
      items(chalans) { chalan ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(chalan.chalanNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              StatusBadge(chalan.status)
            }
            Text("Month: ${chalan.monthYear} • Due: ${chalan.dueDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Payable: Rs. ${chalan.totalPayable.toInt()} • Paid: Rs. ${chalan.paidAmount.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedButton(
                onClick = {
                  val pdf = PdfGenerator.generateFeeChalanPdf(context, chalan, settings)
                  WhatsAppHelper.sharePdf(context, pdf, "Tuition Fee Chalan for ${chalan.studentName} (${chalan.monthYear})")
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("PDF", fontSize = 11.sp)
              }
              OutlinedButton(
                onClick = { onShareWhatsApp(chalan) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF25D366))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Send", fontSize = 11.sp)
              }
              if (chalan.status != "Paid") {
                OutlinedButton(
                  onClick = { onRemindWhatsApp(chalan) },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text("Remind", fontSize = 11.sp)
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StudentReceiptsTab(
  receipts: List<com.example.data.model.PaymentReceipt>,
  settings: TeacherSettings,
  onCollectFee: () -> Unit,
  onShareWhatsApp: (com.example.data.model.PaymentReceipt) -> Unit
) {
  val context = LocalContext.current
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Button(
        onClick = onCollectFee,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
      ) {
        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Record New Payment")
      }
    }
    if (receipts.isEmpty()) {
      item {
        EmptyStateView(
          icon = Icons.Default.Receipt,
          title = "No Payment Receipts",
          message = "Recorded payments and digital receipts will appear here."
        )
      }
    } else {
      items(receipts) { receipt ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(receipt.receiptNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              StatusBadge("Paid")
            }
            Text("Date: ${receipt.paymentDate} • Month: ${receipt.feeMonthYear}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Amount: Rs. ${receipt.amountPaid.toInt()} via ${receipt.paymentMethod}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TertiaryEmerald)
            if (receipt.remainingBalance > 0) {
              Text("Remaining Balance: Rs. ${receipt.remainingBalance.toInt()}", fontSize = 11.sp, color = SecondaryAmber)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedButton(
                onClick = {
                  val pdf = PdfGenerator.generatePaymentReceiptPdf(context, receipt, settings)
                  WhatsAppHelper.sharePdf(context, pdf, "Payment Receipt for ${receipt.studentName}")
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("PDF Receipt", fontSize = 11.sp)
              }
              OutlinedButton(
                onClick = { onShareWhatsApp(receipt) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
              ) {
                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF25D366))
                Spacer(modifier = Modifier.width(4.dp))
                Text("WhatsApp", fontSize = 11.sp)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StudentTestsTab(
  studentId: Long,
  allTests: List<com.example.data.model.TestRecord>,
  viewModel: TuitionViewModel
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    if (allTests.isEmpty()) {
      item {
        EmptyStateView(
          icon = Icons.Default.Assignment,
          title = "No Tests Added",
          message = "Create tests from the Tests section to track student marks."
        )
      }
    } else {
      items(allTests) { test ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(test.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("${test.className} • ${test.subject} • Total: ${test.totalMarks.toInt()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Date: ${test.testDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    }
  }
}

@Composable
private fun StudentHomeworkTab(
  homeworkList: List<com.example.data.model.HomeworkRecord>,
  onShareHomework: (com.example.data.model.HomeworkRecord) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    if (homeworkList.isEmpty()) {
      item {
        EmptyStateView(
          icon = Icons.Default.Assignment,
          title = "No Homework Assigned",
          message = "Assigned homework for this class will show up here."
        )
      }
    } else {
      items(homeworkList) { hw ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(hw.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              StatusBadge(hw.status)
            }
            Text("${hw.subject} • Due: ${hw.dueDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (hw.description.isNotBlank()) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(hw.description, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
              onClick = { onShareHomework(hw) },
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF25D366))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Share on WhatsApp", fontSize = 11.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StudentPerformanceTab(
  student: Student,
  performanceList: List<com.example.data.model.StudentPerformance>,
  attendancePct: Int,
  settings: TeacherSettings,
  onSavePerformance: (com.example.data.model.StudentPerformance) -> Unit
) {
  val context = LocalContext.current
  var reading by remember { mutableIntStateOf(4) }
  var writing by remember { mutableIntStateOf(4) }
  var math by remember { mutableIntStateOf(4) }
  var english by remember { mutableIntStateOf(4) }
  var homework by remember { mutableIntStateOf(4) }
  var participation by remember { mutableIntStateOf(4) }
  var behaviour by remember { mutableIntStateOf(5) }
  var remarks by remember { mutableStateOf("Consistent progress and active interest in class.") }

  val latestPerf = performanceList.firstOrNull()

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Monthly Performance Rubrics (1 to 5 Stars)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Spacer(modifier = Modifier.height(8.dp))

          RatingSliderRow("Reading Skills", reading) { reading = it }
          RatingSliderRow("Writing & Neatness", writing) { writing = it }
          RatingSliderRow("Mathematics", math) { math = it }
          RatingSliderRow("English", english) { english = it }
          RatingSliderRow("Homework Regularity", homework) { homework = it }
          RatingSliderRow("Participation", participation) { participation = it }
          RatingSliderRow("Behaviour & Discipline", behaviour) { behaviour = it }

          Spacer(modifier = Modifier.height(10.dp))
          Text("Teacher Remarks:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
          Spacer(modifier = Modifier.height(4.dp))
          androidx.compose.material3.OutlinedTextField(
            value = remarks,
            onValueChange = { remarks = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )

          Spacer(modifier = Modifier.height(12.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
                val newPerf = com.example.data.model.StudentPerformance(
                  studentId = student.id,
                  studentName = student.name,
                  className = student.className,
                  monthYear = currentMonth,
                  readingRating = reading,
                  writingRating = writing,
                  mathRating = math,
                  englishRating = english,
                  homeworkRating = homework,
                  participationRating = participation,
                  behaviourRating = behaviour,
                  teacherRemarks = remarks
                )
                onSavePerformance(newPerf)
              },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Save Report")
            }
            OutlinedButton(
              onClick = {
                val pdf = PdfGenerator.generateStudentReportPdf(
                  context,
                  student,
                  attendancePct,
                  emptyList(),
                  latestPerf,
                  settings
                )
                WhatsAppHelper.sharePdf(context, pdf, "Monthly Progress Report for ${student.name}")
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
  }
}

@Composable
private fun RatingSliderRow(title: String, current: Int, onSelect: (Int) -> Unit) {
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
