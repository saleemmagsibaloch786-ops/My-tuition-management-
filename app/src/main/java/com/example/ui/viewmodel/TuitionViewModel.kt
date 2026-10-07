package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AttendanceRecord
import com.example.data.model.ClassSchedule
import com.example.data.model.FeeChalan
import com.example.data.model.HomeworkRecord
import com.example.data.model.PaymentReceipt
import com.example.data.model.Student
import com.example.data.model.StudentPerformance
import com.example.data.model.TeacherSettings
import com.example.data.model.TestMark
import com.example.data.model.TestRecord
import com.example.data.repository.TuitionRepository
import com.example.service.GoogleSyncManager
import com.example.service.NotificationHelper
import com.example.service.PdfGenerator
import com.example.service.WhatsAppHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TuitionViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = TuitionRepository.getRepository(application)

  val allStudents = repository.allStudents.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val activeStudents = repository.activeStudents.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val archivedStudents = repository.archivedStudents.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allClasses = repository.allClasses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allAttendance = repository.allAttendance.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allChalans = repository.allChalans.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allReceipts = repository.allReceipts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allTests = repository.allTests.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allHomework = repository.allHomework.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allPerformance = repository.allPerformance.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val settings = repository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TeacherSettings())

  // UI States
  private val _searchQuery = MutableStateFlow("")
  val searchQuery = _searchQuery.asStateFlow()

  private val _selectedClassFilter = MutableStateFlow("All")
  val selectedClassFilter = _selectedClassFilter.asStateFlow()

  private val _selectedStudentId = MutableStateFlow<Long?>(null)
  val selectedStudentId = _selectedStudentId.asStateFlow()

  // WhatsApp Dialog State
  private val _whatsAppDialogState = MutableStateFlow<WhatsAppDialogData?>(null)
  val whatsAppDialogState = _whatsAppDialogState.asStateFlow()

  // Bulk Chalan Progress
  private val _bulkProgress = MutableStateFlow<Pair<Int, Int>?>(null) // (current, total)
  val bulkProgress = _bulkProgress.asStateFlow()

  // SnackBar / Toast message
  private val _userMessage = MutableStateFlow<String?>(null)
  val userMessage = _userMessage.asStateFlow()

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setClassFilter(className: String) {
    _selectedClassFilter.value = className
  }

  fun selectStudent(id: Long?) {
    _selectedStudentId.value = id
  }

  fun clearUserMessage() {
    _userMessage.value = null
  }

  fun showMessage(msg: String) {
    _userMessage.value = msg
  }

  // --- Student operations ---
  fun saveStudent(student: Student) {
    viewModelScope.launch {
      if (student.id == 0L) {
        repository.insertStudent(student)
        _userMessage.value = "Student ${student.name} added successfully"
      } else {
        repository.updateStudent(student)
        _userMessage.value = "Student ${student.name} updated"
      }
    }
  }

  fun toggleStudentStatus(student: Student) {
    viewModelScope.launch {
      val newStatus = if (student.status == "Active") "Archived" else "Active"
      repository.updateStudent(student.copy(status = newStatus))
      _userMessage.value = "Student moved to $newStatus"
    }
  }

  fun deleteStudent(student: Student) {
    viewModelScope.launch {
      repository.deleteStudent(student)
      _userMessage.value = "Student deleted"
    }
  }

  // --- Attendance ---
  fun recordAttendance(student: Student, date: String, status: String, notes: String = "") {
    viewModelScope.launch {
      val record = AttendanceRecord(
        studentId = student.id,
        studentName = student.name,
        className = student.className,
        date = date,
        status = status,
        notes = notes
      )
      repository.recordAttendance(record)

      if (status == "Absent") {
        // Offer WhatsApp absence message
        prepareAbsenceMessage(student, date)
      }
    }
  }

  fun markAllPresent(date: String, className: String? = null) {
    viewModelScope.launch {
      repository.markAllPresent(date, className)
      _userMessage.value = "All students marked Present for $date"
    }
  }

  // --- Classes ---
  fun saveClass(schedule: ClassSchedule) {
    viewModelScope.launch {
      if (schedule.id == 0L) {
        repository.insertClass(schedule)
        _userMessage.value = "Class schedule added"
      } else {
        repository.updateClass(schedule)
        _userMessage.value = "Class schedule updated"
      }
    }
  }

  fun deleteClass(schedule: ClassSchedule) {
    viewModelScope.launch {
      repository.deleteClass(schedule)
      _userMessage.value = "Class schedule deleted"
    }
  }

  // --- Fees & Chalans ---
  fun generateChalan(student: Student, monthYear: String, dueDate: String) {
    viewModelScope.launch {
      val chalan = repository.generateChalanForStudent(student, monthYear, dueDate)
      _userMessage.value = "Fee chalan ${chalan.chalanNumber} generated"
    }
  }

  fun generateBulkChalans(monthYear: String, dueDate: String) {
    viewModelScope.launch {
      _bulkProgress.value = Pair(0, activeStudents.value.size)
      val count = repository.generateBulkChalans(monthYear, dueDate) { current, total ->
        _bulkProgress.value = Pair(current, total)
      }
      _bulkProgress.value = null
      _userMessage.value = "$count Chalans generated for $monthYear"
    }
  }

  fun recordPayment(
    studentId: Long,
    feeMonthYear: String,
    amount: Double,
    paymentMethod: String,
    txRef: String,
    notes: String,
    chalanId: Long? = null
  ) {
    viewModelScope.launch {
      val receipt = repository.recordPayment(
        studentId = studentId,
        feeMonthYear = feeMonthYear,
        amountPaid = amount,
        paymentMethod = paymentMethod,
        transactionRef = txRef,
        notes = notes,
        chalanId = chalanId
      )
      _userMessage.value = "Payment recorded! Receipt: ${receipt.receiptNumber}"
      // Prepare WhatsApp Receipt message
      val currentSetting = settings.value ?: TeacherSettings()
      val msg = currentSetting.receiptTemplate
        .replace("{student}", receipt.studentName)
        .replace("{month}", receipt.feeMonthYear)
        .replace("{amount}", receipt.amountPaid.toInt().toString())
        .replace("{method}", receipt.paymentMethod)
        .replace("{remaining}", receipt.remainingBalance.toInt().toString())
        .replace("{tuition}", currentSetting.tuitionName)

      val student = allStudents.value.find { it.id == studentId }
      _whatsAppDialogState.value = WhatsAppDialogData(
        title = "Share Payment Receipt",
        recipientName = receipt.studentName,
        phone = student?.parentWhatsApp ?: "",
        message = msg,
        receipt = receipt
      )
    }
  }

  // --- Tests ---
  fun addTest(title: String, className: String, subject: String, totalMarks: Double, testDate: String) {
    viewModelScope.launch {
      val test = TestRecord(
        title = title,
        className = className,
        subject = subject,
        totalMarks = totalMarks,
        testDate = testDate
      )
      repository.insertTest(test)
      _userMessage.value = "Test '$title' created"
    }
  }

  fun deleteTest(test: TestRecord) {
    viewModelScope.launch {
      repository.deleteTest(test)
      _userMessage.value = "Test '${test.title}' deleted"
    }
  }

  fun recordTestMark(testId: Long, studentId: Long, marks: Double, totalMarks: Double, remarks: String) {
    viewModelScope.launch {
      repository.recordTestMark(testId, studentId, marks, totalMarks, remarks)
      _userMessage.value = "Marks saved"
    }
  }

  // --- Homework ---
  fun addHomework(hw: HomeworkRecord) {
    viewModelScope.launch {
      repository.insertHomework(hw)
      _userMessage.value = "Homework assigned"
    }
  }

  fun toggleHomeworkStatus(hw: HomeworkRecord) {
    viewModelScope.launch {
      val next = when (hw.status) {
        "Assigned" -> "Completed"
        "Completed" -> "Checked"
        else -> "Assigned"
      }
      repository.updateHomework(hw.copy(status = next))
    }
  }

  fun deleteHomework(hw: HomeworkRecord) {
    viewModelScope.launch {
      repository.deleteHomework(hw)
      _userMessage.value = "Homework deleted"
    }
  }

  // --- Performance ---
  fun savePerformance(perf: StudentPerformance) {
    viewModelScope.launch {
      repository.savePerformance(perf)
      _userMessage.value = "Monthly performance saved for ${perf.studentName}"
    }
  }

  // --- Settings ---
  fun updateSettings(newSettings: TeacherSettings) {
    viewModelScope.launch {
      repository.saveSettings(newSettings)
      _userMessage.value = "Settings saved successfully"
    }
  }

  fun triggerSync() {
    viewModelScope.launch {
      repository.triggerGoogleSync()
      _userMessage.value = "Synchronized with Google Sheets and Drive!"
    }
  }

  fun testMonthlyReminder() {
    val currentSetting = settings.value ?: TeacherSettings()
    NotificationHelper.sendChalanGenerationReminder(
      getApplication(),
      currentSetting.tuitionName
    )
    _userMessage.value = "Sent monthly chalan reminder notification"
  }

  // --- WhatsApp Dialog Prep ---
  fun prepareAbsenceMessage(student: Student, date: String) {
    val currentSetting = settings.value ?: TeacherSettings()
    val msg = currentSetting.absenceTemplate
      .replace("{student}", student.name)
      .replace("{className}", student.className)
      .replace("{date}", date)
      .replace("{teacher}", currentSetting.teacherName)

    _whatsAppDialogState.value = WhatsAppDialogData(
      title = "Send Absence Message",
      recipientName = student.name,
      phone = student.parentWhatsApp,
      message = msg
    )
  }

  fun prepareChalanMessage(chalan: FeeChalan) {
    val currentSetting = settings.value ?: TeacherSettings()
    val student = allStudents.value.find { it.id == chalan.studentId }
    val msg = currentSetting.chalanTemplate
      .replace("{student}", chalan.studentName)
      .replace("{month}", chalan.monthYear)
      .replace("{amount}", chalan.totalPayable.toInt().toString())
      .replace("{dueDate}", chalan.dueDate)
      .replace("{easypaisaTitle}", currentSetting.easypaisaTitle)
      .replace("{easypaisaNumber}", currentSetting.easypaisaNumber)

    _whatsAppDialogState.value = WhatsAppDialogData(
      title = "Send Fee Chalan",
      recipientName = chalan.studentName,
      phone = student?.parentWhatsApp ?: "",
      message = msg,
      chalan = chalan
    )
  }

  fun prepareFeeReminderMessage(chalan: FeeChalan) {
    val currentSetting = settings.value ?: TeacherSettings()
    val student = allStudents.value.find { it.id == chalan.studentId }
    val msg = currentSetting.reminderTemplate
      .replace("{student}", chalan.studentName)
      .replace("{month}", chalan.monthYear)
      .replace("{amount}", (chalan.totalPayable - chalan.paidAmount).toInt().toString())
      .replace("{dueDate}", chalan.dueDate)
      .replace("{teacher}", currentSetting.teacherName)

    _whatsAppDialogState.value = WhatsAppDialogData(
      title = "Send Fee Reminder",
      recipientName = chalan.studentName,
      phone = student?.parentWhatsApp ?: "",
      message = msg
    )
  }

  fun prepareHomeworkMessage(hw: HomeworkRecord) {
    val currentSetting = settings.value ?: TeacherSettings()
    val msg = currentSetting.homeworkTemplate
      .replace("{className}", hw.className)
      .replace("{subject}", hw.subject)
      .replace("{title}", hw.title)
      .replace("{description}", hw.description)
      .replace("{dueDate}", hw.dueDate)
      .replace("{teacher}", currentSetting.teacherName)

    _whatsAppDialogState.value = WhatsAppDialogData(
      title = "Share Homework via WhatsApp",
      recipientName = "All Parents (${hw.className})",
      phone = "",
      message = msg
    )
  }

  fun prepareTestResultMessage(mark: TestMark, test: TestRecord) {
    val currentSetting = settings.value ?: TeacherSettings()
    val student = allStudents.value.find { it.id == mark.studentId }
    val msg = currentSetting.testResultTemplate
      .replace("{student}", mark.studentName)
      .replace("{marks}", mark.marksObtained.toString())
      .replace("{total}", test.totalMarks.toString())
      .replace("{percentage}", mark.percentage.toInt().toString())
      .replace("{grade}", mark.grade)
      .replace("{subject}", test.subject)
      .replace("{title}", test.title)
      .replace("{remarks}", mark.remarks.ifBlank { "Keep working hard" })
      .replace("{teacher}", currentSetting.teacherName)

    _whatsAppDialogState.value = WhatsAppDialogData(
      title = "Send Test Result",
      recipientName = mark.studentName,
      phone = student?.parentWhatsApp ?: "",
      message = msg
    )
  }

  fun preparePerformanceMessage(perf: StudentPerformance) {
    val currentSetting = settings.value ?: TeacherSettings()
    val student = allStudents.value.find { it.id == perf.studentId }
    val avgRating = (perf.readingRating + perf.writingRating + perf.mathRating + perf.englishRating) / 4
    val msg = currentSetting.performanceTemplate
      .replace("{student}", perf.studentName)
      .replace("{month}", perf.monthYear)
      .replace("{behaviour}", perf.behaviourRating.toString())
      .replace("{homework}", perf.homeworkRating.toString())
      .replace("{academics}", avgRating.toString())
      .replace("{remarks}", perf.teacherRemarks.ifBlank { "Consistent progress" })
      .replace("{teacher}", currentSetting.teacherName)

    _whatsAppDialogState.value = WhatsAppDialogData(
      title = "Send Monthly Performance",
      recipientName = perf.studentName,
      phone = student?.parentWhatsApp ?: "",
      message = msg
    )
  }

  fun openCustomWhatsApp(student: Student? = null) {
    _whatsAppDialogState.value = WhatsAppDialogData(
      title = "Compose WhatsApp Message",
      recipientName = student?.name ?: "Parent",
      phone = student?.parentWhatsApp ?: "",
      message = "Assalam-o-Alaikum, this is regarding tuition classes."
    )
  }

  fun dismissWhatsAppDialog() {
    _whatsAppDialogState.value = null
  }
}

data class WhatsAppDialogData(
  val title: String,
  val recipientName: String,
  val phone: String,
  val message: String,
  val chalan: FeeChalan? = null,
  val receipt: PaymentReceipt? = null
)
