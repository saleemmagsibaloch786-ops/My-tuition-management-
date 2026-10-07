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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.FeeChalan
import com.example.data.model.PaymentReceipt
import com.example.data.model.Student
import com.example.data.model.TeacherSettings
import com.example.service.PdfGenerator
import com.example.service.WhatsAppHelper
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.TertiaryEmerald
import com.example.ui.viewmodel.TuitionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FeesScreen(
  viewModel: TuitionViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val allChalans by viewModel.allChalans.collectAsStateWithLifecycle()
  val allReceipts by viewModel.allReceipts.collectAsStateWithLifecycle()
  val activeStudents by viewModel.activeStudents.collectAsStateWithLifecycle()
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val currentSettings = settings ?: TeacherSettings()

  var selectedTab by remember { mutableIntStateOf(0) } // 0: Chalans, 1: Receipts
  var statusFilter by remember { mutableStateOf("All") }

  var showCollectFeeDialog by remember { mutableStateOf(false) }
  var showBulkChalanDialog by remember { mutableStateOf(false) }
  var showSingleChalanDialog by remember { mutableStateOf(false) }

  val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
  val totalCollected = allReceipts.sumOf { it.amountPaid }
  val totalPending = allChalans.filter { it.status != "Paid" }
    .sumOf { it.totalPayable - it.paidAmount }

  val filteredChalans = allChalans.filter {
    if (statusFilter == "All") true else it.status == statusFilter
  }

  Column(
    modifier = modifier.fillMaxSize().testTag("fees_screen")
  ) {
    // Stats Banner
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      StatCard(
        title = "Total Collected",
        value = "Rs. ${totalCollected.toInt()}",
        icon = Icons.Default.TrendingUp,
        accentColor = TertiaryEmerald,
        modifier = Modifier.weight(1f)
      )
      StatCard(
        title = "Outstanding",
        value = "Rs. ${totalPending.toInt()}",
        icon = Icons.Default.Receipt,
        accentColor = if (totalPending > 0) ErrorCrimson else TertiaryEmerald,
        modifier = Modifier.weight(1f)
      )
    }

    // Action Buttons
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Button(
        onClick = { showCollectFeeDialog = true },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(10.dp)
      ) {
        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Collect Fee", fontSize = 12.sp)
      }
      OutlinedButton(
        onClick = { showBulkChalanDialog = true },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(10.dp)
      ) {
        Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Bulk Chalan", fontSize = 12.sp)
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Main Tabs: Fee Chalans & Receipts
    TabRow(selectedTabIndex = selectedTab) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text("Fee Chalans (${allChalans.size})", fontWeight = FontWeight.Bold) }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = { Text("Payment Receipts (${allReceipts.size})", fontWeight = FontWeight.Bold) }
      )
    }

    if (selectedTab == 0) {
      // Status Filters for Chalans
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        items(listOf("All", "Pending", "Overdue", "Partial", "Paid")) { status ->
          FilterChip(
            selected = statusFilter == status,
            onClick = { statusFilter = status },
            label = { Text(status) }
          )
        }
      }

      if (filteredChalans.isEmpty()) {
        EmptyStateView(
          icon = Icons.Default.ReceiptLong,
          title = "No Fee Chalans",
          message = "Use 'Bulk Chalan' to generate fee slips for all active tuition students.",
          actionButtonText = "Generate Chalans",
          onActionClick = { showBulkChalanDialog = true }
        )
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredChalans, key = { it.id }) { chalan ->
            FeeChalanCard(
              chalan = chalan,
              settings = currentSettings,
              onShareWhatsApp = { viewModel.prepareChalanMessage(chalan) },
              onRemindWhatsApp = { viewModel.prepareFeeReminderMessage(chalan) },
              onPdfClick = {
                val pdf = PdfGenerator.generateFeeChalanPdf(context, chalan, currentSettings)
                WhatsAppHelper.sharePdf(context, pdf, "Tuition Fee Chalan for ${chalan.studentName} (${chalan.monthYear})")
              }
            )
          }
          item { Spacer(modifier = Modifier.height(80.dp)) }
        }
      }
    } else {
      // Payment Receipts Tab
      if (allReceipts.isEmpty()) {
        EmptyStateView(
          icon = Icons.Default.Payment,
          title = "No Payment Receipts",
          message = "When you record student fee payments, digital receipts will appear here.",
          actionButtonText = "Record Payment",
          onActionClick = { showCollectFeeDialog = true }
        )
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(allReceipts, key = { it.id }) { receipt ->
            Card(
              shape = RoundedCornerShape(12.dp),
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
                  Text(receipt.receiptNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                  StatusBadge("Paid")
                }
                Text("${receipt.studentName} • ${receipt.className}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Month: ${receipt.feeMonthYear} • Date: ${receipt.paymentDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                  text = "Received: Rs. ${receipt.amountPaid.toInt()} via ${receipt.paymentMethod}",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = TertiaryEmerald
                )
                if (receipt.remainingBalance > 0) {
                  Text("Remaining Arrears: Rs. ${receipt.remainingBalance.toInt()}", fontSize = 11.sp, color = SecondaryAmber)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  OutlinedButton(
                    onClick = {
                      val pdf = PdfGenerator.generatePaymentReceiptPdf(context, receipt, currentSettings)
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
                    onClick = {
                      val student = activeStudents.find { it.id == receipt.studentId }
                      val msg = currentSettings.receiptTemplate
                        .replace("{student}", receipt.studentName)
                        .replace("{month}", receipt.feeMonthYear)
                        .replace("{amount}", receipt.amountPaid.toInt().toString())
                        .replace("{method}", receipt.paymentMethod)
                        .replace("{remaining}", receipt.remainingBalance.toInt().toString())
                        .replace("{tuition}", currentSettings.tuitionName)
                      WhatsAppHelper.sendTextMessage(context, student?.parentWhatsApp ?: "", msg)
                    },
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
          item { Spacer(modifier = Modifier.height(80.dp)) }
        }
      }
    }
  }

  // Bulk Chalan Dialog
  if (showBulkChalanDialog) {
    BulkChalanConfigDialog(
      settings = currentSettings,
      activeStudentsCount = activeStudents.size,
      onDismiss = { showBulkChalanDialog = false },
      onConfirm = { month, dueDate ->
        showBulkChalanDialog = false
        viewModel.generateBulkChalans(month, dueDate)
      }
    )
  }

  // Collect Fee Dialog
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

@Composable
fun FeeChalanCard(
  chalan: FeeChalan,
  settings: TeacherSettings,
  onShareWhatsApp: () -> Unit,
  onRemindWhatsApp: () -> Unit,
  onPdfClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(12.dp),
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
        Column {
          Text(chalan.studentName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Text("${chalan.className} • ${chalan.chalanNumber}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        StatusBadge(chalan.status)
      }

      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Month: ${chalan.monthYear}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Due Date: ${chalan.dueDate}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = ErrorCrimson)
      }

      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Payable: Rs. ${chalan.totalPayable.toInt()}",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        if (chalan.paidAmount > 0) {
          Text("Paid: Rs. ${chalan.paidAmount.toInt()}", fontSize = 12.sp, color = TertiaryEmerald)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = onPdfClick,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(8.dp)
        ) {
          Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("PDF Chalan", fontSize = 11.sp)
        }
        OutlinedButton(
          onClick = onShareWhatsApp,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(8.dp)
        ) {
          Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF25D366))
          Spacer(modifier = Modifier.width(4.dp))
          Text("WhatsApp", fontSize = 11.sp)
        }
        if (chalan.status != "Paid") {
          OutlinedButton(
            onClick = onRemindWhatsApp,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Reminder", fontSize = 11.sp)
          }
        }
      }
    }
  }
}

@Composable
fun BulkChalanConfigDialog(
  settings: TeacherSettings,
  activeStudentsCount: Int,
  onDismiss: () -> Unit,
  onConfirm: (month: String, dueDate: String) -> Unit
) {
  val defaultMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
  var month by remember { mutableStateOf(defaultMonth) }
  var dueDate by remember { mutableStateOf("${settings.feeDueDateDay} $defaultMonth") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Generate Bulk Fee Chalans", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "This will create individual fee chalans for all $activeStudentsCount active students.",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
          value = month,
          onValueChange = { month = it },
          label = { Text("Fee Month") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )
        OutlinedTextField(
          value = dueDate,
          onValueChange = { dueDate = it },
          label = { Text("Due Date") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(month, dueDate) },
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("Generate ($activeStudentsCount)")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectFeeDialog(
  students: List<Student>,
  chalans: List<FeeChalan>,
  onDismiss: () -> Unit,
  onConfirm: (studentId: Long, month: String, amount: Double, method: String, ref: String, notes: String, chalanId: Long?) -> Unit
) {
  var selectedStudent by remember { mutableStateOf(students.firstOrNull()) }
  var expanded by remember { mutableStateOf(false) }

  val defaultMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
  var month by remember { mutableStateOf(defaultMonth) }
  var amountStr by remember {
    mutableStateOf(selectedStudent?.let { (it.monthlyFee - it.discount).toInt().toString() } ?: "3500")
  }
  var paymentMethod by remember { mutableStateOf("Cash") }
  var transactionRef by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("Paid on time") }

  val matchingChalan = chalans.find { it.studentId == selectedStudent?.id && it.monthYear == month }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Collect Tuition Fee", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Student Selector
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
            students.forEach { s ->
              DropdownMenuItem(
                text = { Text("${s.name} (${s.className})") },
                onClick = {
                  selectedStudent = s
                  amountStr = (s.monthlyFee - s.discount).toInt().toString()
                  expanded = false
                }
              )
            }
          }
        }

        OutlinedTextField(
          value = month,
          onValueChange = { month = it },
          label = { Text("Fee Month") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
          value = amountStr,
          onValueChange = { amountStr = it },
          label = { Text("Amount Paid (PKR) *") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        // Payment Method selection
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf("Cash", "Easypaisa", "Other").forEach { method ->
            FilterChip(
              selected = paymentMethod == method,
              onClick = { paymentMethod = method },
              label = { Text(method) }
            )
          }
        }

        if (paymentMethod != "Cash") {
          OutlinedTextField(
            value = transactionRef,
            onValueChange = { transactionRef = it },
            label = { Text("Transaction Reference (TRX ID)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )
        }

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Receipt Notes") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val s = selectedStudent ?: return@Button
          val amt = amountStr.toDoubleOrNull() ?: 0.0
          onConfirm(s.id, month, amt, paymentMethod, transactionRef, notes, matchingChalan?.id)
        },
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("Record Payment & Receipt")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
