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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TeacherSettings
import com.example.service.GoogleSyncManager
import com.example.service.WhatsAppHelper
import com.example.ui.components.SyncStatusPill
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.TertiaryEmerald
import com.example.ui.viewmodel.TuitionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  viewModel: TuitionViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val currentSettings = settings ?: TeacherSettings()

  var tuitionName by remember(currentSettings) { mutableStateOf(currentSettings.tuitionName) }
  var teacherName by remember(currentSettings) { mutableStateOf(currentSettings.teacherName) }
  var whatsAppNumber by remember(currentSettings) { mutableStateOf(currentSettings.whatsAppNumber) }
  var easypaisaNumber by remember(currentSettings) { mutableStateOf(currentSettings.easypaisaNumber) }
  var easypaisaTitle by remember(currentSettings) { mutableStateOf(currentSettings.easypaisaTitle) }
  var address by remember(currentSettings) { mutableStateOf(currentSettings.address) }
  var defaultFeeStr by remember(currentSettings) { mutableStateOf(currentSettings.defaultFee.toInt().toString()) }
  var dueDateDayStr by remember(currentSettings) { mutableStateOf(currentSettings.feeDueDateDay.toString()) }
  var reminderDayStr by remember(currentSettings) { mutableStateOf(currentSettings.chalanReminderDay.toString()) }
  var receiptPrefix by remember(currentSettings) { mutableStateOf(currentSettings.receiptPrefix) }
  var chalanPrefix by remember(currentSettings) { mutableStateOf(currentSettings.chalanPrefix) }
  var googleEmail by remember(currentSettings) { mutableStateOf(currentSettings.googleAccountEmail) }

  // WhatsApp Templates
  var chalanTemplate by remember(currentSettings) { mutableStateOf(currentSettings.chalanTemplate) }
  var reminderTemplate by remember(currentSettings) { mutableStateOf(currentSettings.reminderTemplate) }
  var receiptTemplate by remember(currentSettings) { mutableStateOf(currentSettings.receiptTemplate) }
  var absenceTemplate by remember(currentSettings) { mutableStateOf(currentSettings.absenceTemplate) }
  var testResultTemplate by remember(currentSettings) { mutableStateOf(currentSettings.testResultTemplate) }
  var homeworkTemplate by remember(currentSettings) { mutableStateOf(currentSettings.homeworkTemplate) }

  Scaffold(
    modifier = modifier.fillMaxSize().testTag("settings_screen"),
    topBar = {
      TopAppBar(
        title = { Text("Teacher Settings & Sync", fontWeight = FontWeight.Bold) },
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
      // Cloud Backup & Sync Section
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Google Sheets & Drive Sync", fontWeight = FontWeight.Bold, fontSize = 15.sp)
              SyncStatusPill(syncStatus = currentSettings.syncStatus)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Connected Account: ${currentSettings.googleAccountEmail}",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Medium
            )
            Text(
              text = "Last synced: ${currentSettings.lastSyncTime} • Folders: My Tuition Manager/Fee Chalans/ & Payment Receipts/",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = { viewModel.triggerSync() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sync Now", fontSize = 12.sp)
              }
              OutlinedButton(
                onClick = { viewModel.testMonthlyReminder() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Test Reminder", fontSize = 12.sp)
              }
            }
          }
        }
      }

      // Teacher & Tuition Profile
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Tuition & Teacher Profile", fontWeight = FontWeight.Bold, fontSize = 15.sp)

            OutlinedTextField(
              value = tuitionName,
              onValueChange = { tuitionName = it },
              label = { Text("Tuition Name (Printed on Chalans)") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
              value = teacherName,
              onValueChange = { teacherName = it },
              label = { Text("Teacher Name") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
              value = whatsAppNumber,
              onValueChange = { whatsAppNumber = it },
              label = { Text("Teacher WhatsApp Number") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
              value = address,
              onValueChange = { address = it },
              label = { Text("Tuition Address / Location") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            )
          }
        }
      }

      // Easypaisa Account Details
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Easypaisa / Payment Details", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(
              "These will appear on every generated PDF fee chalan so parents can pay directly.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
              value = easypaisaTitle,
              onValueChange = { easypaisaTitle = it },
              label = { Text("Easypaisa Account Title") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
              value = easypaisaNumber,
              onValueChange = { easypaisaNumber = it },
              label = { Text("Easypaisa Account Number") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            )
          }
        }
      }

      // Fee & Notification Configuration
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Fee & Reminder Configuration", fontWeight = FontWeight.Bold, fontSize = 15.sp)

            OutlinedTextField(
              value = defaultFeeStr,
              onValueChange = { defaultFeeStr = it },
              label = { Text("Default Monthly Tuition Fee (PKR)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = dueDateDayStr,
                onValueChange = { dueDateDayStr = it },
                label = { Text("Fee Due Day (e.g. 10)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
              )
              OutlinedTextField(
                value = reminderDayStr,
                onValueChange = { reminderDayStr = it },
                label = { Text("Reminder Day (e.g. 28)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = chalanPrefix,
                onValueChange = { chalanPrefix = it },
                label = { Text("Chalan Prefix (e.g. CH)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
              )
              OutlinedTextField(
                value = receiptPrefix,
                onValueChange = { receiptPrefix = it },
                label = { Text("Receipt Prefix (e.g. REC)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
              )
            }
          }
        }
      }

      // WhatsApp Templates
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("WhatsApp Message Templates", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(
              "Placeholders like {student}, {month}, {amount}, {dueDate}, {teacher} will be replaced automatically.",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
              value = chalanTemplate,
              onValueChange = { chalanTemplate = it },
              label = { Text("Fee Chalan Template") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
              value = reminderTemplate,
              onValueChange = { reminderTemplate = it },
              label = { Text("Fee Reminder Template") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
              value = receiptTemplate,
              onValueChange = { receiptTemplate = it },
              label = { Text("Payment Receipt Template") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
              value = absenceTemplate,
              onValueChange = { absenceTemplate = it },
              label = { Text("Absence Notification Template") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
              value = homeworkTemplate,
              onValueChange = { homeworkTemplate = it },
              label = { Text("Homework Template") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp)
            )
          }
        }
      }

      // Save Button
      item {
        Button(
          onClick = {
            val updated = currentSettings.copy(
              tuitionName = tuitionName.trim(),
              teacherName = teacherName.trim(),
              whatsAppNumber = whatsAppNumber.trim(),
              easypaisaTitle = easypaisaTitle.trim(),
              easypaisaNumber = easypaisaNumber.trim(),
              address = address.trim(),
              defaultFee = defaultFeeStr.toDoubleOrNull() ?: 3500.0,
              feeDueDateDay = dueDateDayStr.toIntOrNull() ?: 10,
              chalanReminderDay = reminderDayStr.toIntOrNull() ?: 28,
              chalanPrefix = chalanPrefix.trim(),
              receiptPrefix = receiptPrefix.trim(),
              googleAccountEmail = googleEmail.trim(),
              chalanTemplate = chalanTemplate,
              reminderTemplate = reminderTemplate,
              receiptTemplate = receiptTemplate,
              absenceTemplate = absenceTemplate,
              homeworkTemplate = homeworkTemplate
            )
            viewModel.updateSettings(updated)
          },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Save All Settings")
        }
        Spacer(modifier = Modifier.height(70.dp))
      }
    }
  }
}
