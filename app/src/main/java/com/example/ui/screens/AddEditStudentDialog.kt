package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddEditStudentDialog(
  student: Student?,
  defaultFee: Double,
  onDismiss: () -> Unit,
  onSave: (Student) -> Unit
) {
  var studentId by remember {
    mutableStateOf(student?.studentId ?: "STU-${(System.currentTimeMillis() % 10000)}")
  }
  var name by remember { mutableStateOf(student?.name ?: "") }
  var fatherName by remember { mutableStateOf(student?.fatherGuardianName ?: "") }
  var whatsApp by remember { mutableStateOf(student?.parentWhatsApp ?: "+923") }
  var dob by remember { mutableStateOf(student?.dob ?: "") }
  var gender by remember { mutableStateOf(student?.gender ?: "Male") }
  var school by remember { mutableStateOf(student?.school ?: "") }
  var className by remember { mutableStateOf(student?.className ?: "Class 10") }
  var subjects by remember { mutableStateOf(student?.subjects ?: "Mathematics, Physics") }
  var address by remember { mutableStateOf(student?.address ?: "") }
  var admissionDate by remember {
    mutableStateOf(student?.admissionDate ?: SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()))
  }
  var monthlyFeeStr by remember {
    mutableStateOf((student?.monthlyFee ?: defaultFee).toInt().toString())
  }
  var discountStr by remember {
    mutableStateOf((student?.discount ?: 0.0).toInt().toString())
  }
  var notes by remember { mutableStateOf(student?.notes ?: "") }

  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (student == null) "Add Tuition Student" else "Edit Student",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (errorMessage != null) {
          Text(
            text = errorMessage ?: "",
            color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Student Name *") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          shape = RoundedCornerShape(10.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = studentId,
            onValueChange = { studentId = it },
            label = { Text("Roll / ID") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
          )
          OutlinedTextField(
            value = className,
            onValueChange = { className = it },
            label = { Text("Class *") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
          )
        }

        OutlinedTextField(
          value = fatherName,
          onValueChange = { fatherName = it },
          label = { Text("Father / Guardian Name *") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
          value = whatsApp,
          onValueChange = { whatsApp = it },
          label = { Text("Parent WhatsApp Number *") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
          shape = RoundedCornerShape(10.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FilterChip(
            selected = gender == "Male",
            onClick = { gender = "Male" },
            label = { Text("Male") },
            modifier = Modifier.weight(1f)
          )
          FilterChip(
            selected = gender == "Female",
            onClick = { gender = "Female" },
            label = { Text("Female") },
            modifier = Modifier.weight(1f)
          )
        }

        OutlinedTextField(
          value = school,
          onValueChange = { school = it },
          label = { Text("School Name") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
          value = subjects,
          onValueChange = { subjects = it },
          label = { Text("Enrolled Subjects") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          shape = RoundedCornerShape(10.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = monthlyFeeStr,
            onValueChange = { monthlyFeeStr = it },
            label = { Text("Monthly Fee (PKR) *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
          )
          OutlinedTextField(
            value = discountStr,
            onValueChange = { discountStr = it },
            label = { Text("Discount (PKR)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
          )
        }

        OutlinedTextField(
          value = address,
          onValueChange = { address = it },
          label = { Text("Home Address") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Teacher Notes / Remarks") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isBlank() || fatherName.isBlank() || whatsApp.isBlank()) {
            errorMessage = "Please enter student name, father name, and parent WhatsApp."
            return@Button
          }
          val fee = monthlyFeeStr.toDoubleOrNull() ?: defaultFee
          val disc = discountStr.toDoubleOrNull() ?: 0.0

          val updated = (student ?: Student()).copy(
            studentId = studentId.ifBlank { "STU-${(System.currentTimeMillis() % 1000)}" },
            name = name.trim(),
            fatherGuardianName = fatherName.trim(),
            parentWhatsApp = whatsApp.trim(),
            dob = dob,
            gender = gender,
            school = school.trim(),
            className = className.trim(),
            subjects = subjects.trim(),
            address = address.trim(),
            admissionDate = admissionDate,
            monthlyFee = fee,
            discount = disc,
            notes = notes.trim()
          )
          onSave(updated)
          onDismiss()
        },
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("Save Student")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
