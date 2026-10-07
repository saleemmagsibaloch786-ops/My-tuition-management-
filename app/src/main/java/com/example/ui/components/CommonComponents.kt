package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TeacherSettings
import com.example.service.PdfGenerator
import com.example.service.WhatsAppHelper
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.TertiaryEmerald
import com.example.ui.viewmodel.WhatsAppDialogData

@Composable
fun SyncStatusPill(
  syncStatus: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {}
) {
  val (bgColor, textColor, icon) = when (syncStatus) {
    "Syncing" -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), Icons.Default.CloudSync)
    "Synced" -> Triple(Color(0xFFD1FAE5), Color(0xFF065F46), Icons.Default.CloudDone)
    "Sync Failed" -> Triple(Color(0xFFFEE2E2), Color(0xFF991B1B), Icons.Default.Error)
    else -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), Icons.Default.CloudOff)
  }

  Surface(
    shape = RoundedCornerShape(20.dp),
    color = bgColor,
    modifier = modifier
      .clip(RoundedCornerShape(20.dp))
      .clickable { onClick() }
      .testTag("sync_status_pill")
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = syncStatus,
        tint = textColor,
        modifier = Modifier.size(15.dp)
      )
      Spacer(modifier = Modifier.width(5.dp))
      Text(
        text = syncStatus,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = textColor
      )
    }
  }
}

@Composable
fun StatCard(
  title: String,
  value: String,
  icon: ImageVector,
  accentColor: Color,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  onClick: (() -> Unit)? = null
) {
  Card(
    modifier = modifier
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
      .testTag("stat_card_${title.lowercase().replace(" ", "_")}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .background(accentColor.copy(alpha = 0.15f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(20.dp)
          )
        }
      }
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = value,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        text = title,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      if (subtitle != null) {
        Text(
          text = subtitle,
          fontSize = 10.sp,
          color = accentColor,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}

@Composable
fun StatusBadge(status: String) {
  val (bgColor, textColor) = when (status) {
    "Present", "Paid", "Completed", "Active" -> Color(0xFFD1FAE5) to Color(0xFF065F46)
    "Absent", "Overdue" -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
    "Late", "Partial", "Assigned" -> Color(0xFFFEF3C7) to Color(0xFF92400E)
    "Leave" -> Color(0xFFE0E7FF) to Color(0xFF3730A3)
    else -> Color(0xFFF1F5F9) to Color(0xFF475569)
  }

  Surface(
    shape = RoundedCornerShape(8.dp),
    color = bgColor
  ) {
    Text(
      text = status,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = textColor
    )
  }
}

@Composable
fun EmptyStateView(
  icon: ImageVector,
  title: String,
  message: String,
  modifier: Modifier = Modifier,
  actionButtonText: String? = null,
  onActionClick: () -> Unit = {}
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(64.dp)
        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(32.dp)
      )
    }
    Spacer(modifier = Modifier.height(14.dp))
    Text(
      text = title,
      fontSize = 17.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = message,
      fontSize = 13.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      lineHeight = 18.sp
    )
    if (actionButtonText != null) {
      Spacer(modifier = Modifier.height(18.dp))
      Button(
        onClick = onActionClick,
        shape = RoundedCornerShape(12.dp)
      ) {
        Text(actionButtonText)
      }
    }
  }
}

@Composable
fun WhatsAppComposerDialog(
  data: WhatsAppDialogData,
  settings: TeacherSettings,
  onDismiss: () -> Unit
) {
  var editedMessage by remember(data.message) { mutableStateOf(data.message) }
  var recipientPhone by remember(data.phone) { mutableStateOf(data.phone) }
  val context = LocalContext.current

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = data.title,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Recipient: ${data.recipientName}",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = recipientPhone,
          onValueChange = { recipientPhone = it },
          label = { Text("Parent WhatsApp Number") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          shape = RoundedCornerShape(10.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "Message (You can edit before sending):",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = editedMessage,
          onValueChange = { editedMessage = it },
          modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
          shape = RoundedCornerShape(10.dp),
          textStyle = MaterialTheme.typography.bodyMedium
        )
        if (data.chalan != null || data.receipt != null) {
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Tip: You can send text directly or generate and share the official PDF.",
            fontSize = 11.sp,
            color = Color(0xFF059669),
            fontWeight = FontWeight.Medium
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          WhatsAppHelper.sendTextMessage(context, recipientPhone, editedMessage)
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
        shape = RoundedCornerShape(10.dp)
      ) {
        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Send on WhatsApp")
      }
    },
    dismissButton = {
      Row {
        if (data.chalan != null) {
          OutlinedButton(
            onClick = {
              val pdf = PdfGenerator.generateFeeChalanPdf(context, data.chalan, settings)
              WhatsAppHelper.sharePdf(context, pdf, editedMessage)
              onDismiss()
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.padding(end = 4.dp)
          ) {
            Text("Share PDF")
          }
        } else if (data.receipt != null) {
          OutlinedButton(
            onClick = {
              val pdf = PdfGenerator.generatePaymentReceiptPdf(context, data.receipt, settings)
              WhatsAppHelper.sharePdf(context, pdf, editedMessage)
              onDismiss()
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.padding(end = 4.dp)
          ) {
            Text("Share PDF")
          }
        }
        TextButton(onClick = onDismiss) {
          Text("Cancel")
        }
      }
    }
  )
}

@Composable
fun BulkChalanProgressDialog(
  current: Int,
  total: Int
) {
  val progress = if (total > 0) current.toFloat() / total.toFloat() else 0f

  AlertDialog(
    onDismissRequest = {},
    title = {
      Text("Generating Bulk Chalans", fontWeight = FontWeight.Bold)
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Generating $current/$total...",
          fontSize = 16.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(14.dp))
        LinearProgressIndicator(
          progress = { progress },
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "Please wait while professional fee chalans are created for all active students.",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )
      }
    },
    confirmButton = {}
  )
}
