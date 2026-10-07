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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TeacherSettings
import com.example.ui.components.SyncStatusPill
import com.example.ui.theme.ErrorCrimson
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.TertiaryEmerald
import com.example.ui.viewmodel.TuitionViewModel

@Composable
fun MoreHubScreen(
  viewModel: TuitionViewModel,
  onNavigateToAttendance: () -> Unit,
  onNavigateToTests: () -> Unit,
  onNavigateToHomework: () -> Unit,
  onNavigateToPerformance: () -> Unit,
  onNavigateToReports: () -> Unit,
  onNavigateToSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  val settings by viewModel.settings.collectAsStateWithLifecycle()
  val currentSettings = settings ?: TeacherSettings()

  LazyColumn(
    modifier = modifier.fillMaxSize().testTag("more_hub_screen"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("Tuition Tools & More", fontWeight = FontWeight.Bold, fontSize = 20.sp)
          Text("Digital assistant for individual tuition teachers", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        SyncStatusPill(syncStatus = currentSettings.syncStatus, onClick = { viewModel.triggerSync() })
      }
      Spacer(modifier = Modifier.height(6.dp))
    }

    item {
      HubNavigationCard(
        title = "Daily Attendance Register",
        subtitle = "One-tap attendance (Present, Absent, Late, Leave), Mark All Present, and WhatsApp absence alerts.",
        icon = Icons.Default.CheckCircle,
        iconColor = TertiaryEmerald,
        onClick = onNavigateToAttendance
      )
    }

    item {
      HubNavigationCard(
        title = "Tests & Assessment Ledger",
        subtitle = "Create tests, enter student marks, automatic grade calculation (A+, A, B, C, D, F) & history.",
        icon = Icons.Default.Assignment,
        iconColor = Color(0xFF7C3AED),
        onClick = onNavigateToTests
      )
    }

    item {
      HubNavigationCard(
        title = "Homework Tracker",
        subtitle = "Assign exercises, track completion status, and broadcast homework assignments on WhatsApp.",
        icon = Icons.Default.MenuBook,
        iconColor = SecondaryAmber,
        onClick = onNavigateToHomework
      )
    }

    item {
      HubNavigationCard(
        title = "Student Performance Rubrics",
        subtitle = "7 Assessment rubrics (Reading, Writing, Math, English, Behaviour), remarks, and monthly reports.",
        icon = Icons.Default.Star,
        iconColor = Color(0xFFEAB308),
        onClick = onNavigateToPerformance
      )
    }

    item {
      HubNavigationCard(
        title = "Reports & PDF Export",
        subtitle = "Generate comprehensive fee collection, student report cards, and attendance registers.",
        icon = Icons.Default.BarChart,
        iconColor = Color(0xFF0284C7),
        onClick = onNavigateToReports
      )
    }

    item {
      HubNavigationCard(
        title = "Settings & Google Cloud Sync",
        subtitle = "Tuition profile, Easypaisa account, reminder days, Google Sheets/Drive backup, and WhatsApp templates.",
        icon = Icons.Default.Settings,
        iconColor = PrimaryNavy,
        onClick = onNavigateToSettings
      )
    }

    item { Spacer(modifier = Modifier.height(70.dp)) }
  }
}

@Composable
fun HubNavigationCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  iconColor: Color,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("hub_card_${title.lowercase().replace(" ", "_")}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .background(iconColor.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = iconColor,
          modifier = Modifier.size(24.dp)
        )
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          lineHeight = 16.sp
        )
      }
      Spacer(modifier = Modifier.width(6.dp))
      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.outline,
        modifier = Modifier.size(16.dp)
      )
    }
  }
}
