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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Student
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.PrimaryNavy
import com.example.ui.viewmodel.TuitionViewModel

@Composable
fun StudentsScreen(
  viewModel: TuitionViewModel,
  onStudentClick: (Long) -> Unit,
  onAddStudentClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

  var selectedTab by remember { mutableIntStateOf(0) } // 0: Active, 1: Archived, 2: All

  val filteredStudents = allStudents.filter { student ->
    val matchesTab = when (selectedTab) {
      0 -> student.status == "Active"
      1 -> student.status == "Archived"
      else -> true
    }
    val matchesQuery = searchQuery.isBlank() ||
      student.name.contains(searchQuery, ignoreCase = true) ||
      student.studentId.contains(searchQuery, ignoreCase = true) ||
      student.className.contains(searchQuery, ignoreCase = true) ||
      student.fatherGuardianName.contains(searchQuery, ignoreCase = true) ||
      student.parentWhatsApp.contains(searchQuery)

    matchesTab && matchesQuery
  }

  Scaffold(
    modifier = modifier.fillMaxSize().testTag("students_screen"),
    floatingActionButton = {
      FloatingActionButton(
        onClick = onAddStudentClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        modifier = Modifier.testTag("add_student_fab")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Student")
      }
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      // Search Bar
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.setSearchQuery(it) },
        placeholder = { Text("Search by name, roll no, class, or parent...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
          if (searchQuery.isNotBlank()) {
            IconButton(onClick = { viewModel.setSearchQuery("") }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear")
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
          .testTag("student_search_bar")
      )

      // Tabs: Active (N) / Archived (N)
      val activeCount = allStudents.count { it.status == "Active" }
      val archivedCount = allStudents.count { it.status == "Archived" }

      TabRow(
        selectedTabIndex = selectedTab,
        modifier = Modifier.fillMaxWidth()
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("Active ($activeCount)", fontWeight = FontWeight.SemiBold) }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("Archived ($archivedCount)", fontWeight = FontWeight.SemiBold) }
        )
        Tab(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          text = { Text("All (${allStudents.size})", fontWeight = FontWeight.SemiBold) }
        )
      }

      // Student List
      if (filteredStudents.isEmpty()) {
        EmptyStateView(
          icon = Icons.Default.Person,
          title = if (searchQuery.isNotBlank()) "No Matching Students" else "No Students Found",
          message = if (searchQuery.isNotBlank()) "Try searching with a different name or class." else "Tap the '+' button below to add your first tuition student.",
          actionButtonText = if (searchQuery.isBlank()) "Add Student" else null,
          onActionClick = onAddStudentClick
        )
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredStudents, key = { it.id }) { student ->
            StudentItemCard(
              student = student,
              onClick = { onStudentClick(student.id) },
              onWhatsAppClick = {
                viewModel.openCustomWhatsApp(student)
              }
            )
          }
          item {
            Spacer(modifier = Modifier.height(70.dp))
          }
        }
      }
    }
  }
}

@Composable
fun StudentItemCard(
  student: Student,
  onClick: () -> Unit,
  onWhatsAppClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("student_item_${student.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Avatar with Initials
      Box(
        modifier = Modifier
          .size(46.dp)
          .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        val initials = student.name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
        Text(
          text = initials.ifBlank { "S" },
          color = MaterialTheme.colorScheme.onPrimaryContainer,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Main Info
      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = student.name,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          StatusBadge(student.status)
        }
        Text(
          text = "${student.className} • ${student.studentId}",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "Guardian: ${student.fatherGuardianName.ifBlank { "Parent" }} • ${student.school.ifBlank { "Tuition" }}",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "Fee: Rs. ${student.monthlyFee.toInt()}" + if (student.discount > 0) " (Disc: Rs. ${student.discount.toInt()})" else "",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.primary
        )
      }

      // Quick WhatsApp Action
      IconButton(
        onClick = onWhatsAppClick,
        modifier = Modifier
          .size(40.dp)
          .background(Color(0xFF25D366).copy(alpha = 0.15f), CircleShape)
          .testTag("whatsapp_btn_${student.id}")
      ) {
        Icon(
          imageVector = Icons.Default.Chat,
          contentDescription = "WhatsApp",
          tint = Color(0xFF25D366),
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}
