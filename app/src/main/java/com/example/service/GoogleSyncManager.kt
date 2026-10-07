package com.example.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.local.TuitionDao
import com.example.data.model.TeacherSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GoogleSyncManager {

  const val DRIVE_ROOT_FOLDER = "My Tuition Manager"
  const val DRIVE_FOLDER_CHALANS = "My Tuition Manager/Fee Chalans/"
  const val DRIVE_FOLDER_RECEIPTS = "My Tuition Manager/Payment Receipts/"
  const val DRIVE_FOLDER_REPORTS = "My Tuition Manager/Student Reports/"
  const val DRIVE_FOLDER_TESTS = "My Tuition Manager/Test Results/"

  fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val activeNetwork = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }

  /**
   * Serializes all 11 tables into Google Sheets compatible structured JSON/CSV format
   */
  suspend fun exportStructuredSheetData(dao: TuitionDao): JSONObject = withContext(Dispatchers.IO) {
    val root = JSONObject()
    val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
    root.put("timestamp", timestamp)
    root.put("spreadsheet_name", "My Tuition Manager - Cloud Backup")

    // 1. Students
    val studentsArray = JSONArray()
    dao.getAllStudents().first().forEach { s ->
      val obj = JSONObject().apply {
        put("student_id", s.studentId)
        put("name", s.name)
        put("father_guardian", s.fatherGuardianName)
        put("whatsapp", s.parentWhatsApp)
        put("dob", s.dob)
        put("gender", s.gender)
        put("school", s.school)
        put("class", s.className)
        put("subjects", s.subjects)
        put("address", s.address)
        put("monthly_fee", s.monthlyFee)
        put("discount", s.discount)
        put("status", s.status)
        put("notes", s.notes)
      }
      studentsArray.put(obj)
    }
    root.put("Students", studentsArray)

    // 2. Classes
    val classesArray = JSONArray()
    dao.getAllClasses().first().forEach { c ->
      classesArray.put(JSONObject().apply {
        put("class", c.className)
        put("subject", c.subject)
        put("day", c.dayOfWeek)
        put("start_time", c.startTime)
        put("end_time", c.endTime)
        put("students_count", c.studentsCount)
        put("student_names", c.studentNames)
      })
    }
    root.put("Classes", classesArray)

    // 3. Attendance
    val attendanceArray = JSONArray()
    dao.getAllAttendance().first().forEach { a ->
      attendanceArray.put(JSONObject().apply {
        put("student_id", a.studentId)
        put("student_name", a.studentName)
        put("class", a.className)
        put("date", a.date)
        put("status", a.status)
      })
    }
    root.put("Attendance", attendanceArray)

    // 4. Fee Chalans
    val chalansArray = JSONArray()
    dao.getAllChalans().first().forEach { ch ->
      chalansArray.put(JSONObject().apply {
        put("chalan_no", ch.chalanNumber)
        put("student_id", ch.studentId)
        put("student_name", ch.studentName)
        put("month", ch.monthYear)
        put("due_date", ch.dueDate)
        put("total_payable", ch.totalPayable)
        put("paid_amount", ch.paidAmount)
        put("status", ch.status)
        put("drive_path", "$DRIVE_FOLDER_CHALANS${ch.chalanNumber}.pdf")
      })
    }
    root.put("Fee_Chalans", chalansArray)

    // 5. Payment Receipts
    val receiptsArray = JSONArray()
    dao.getAllReceipts().first().forEach { r ->
      receiptsArray.put(JSONObject().apply {
        put("receipt_no", r.receiptNumber)
        put("student_id", r.studentId)
        put("student_name", r.studentName)
        put("fee_month", r.feeMonthYear)
        put("amount_paid", r.amountPaid)
        put("remaining_balance", r.remainingBalance)
        put("payment_date", r.paymentDate)
        put("payment_method", r.paymentMethod)
        put("tx_reference", r.transactionReference)
        put("drive_path", "$DRIVE_FOLDER_RECEIPTS${r.receiptNumber}.pdf")
      })
    }
    root.put("Payment_Receipts", receiptsArray)

    // 6. Tests
    val testsArray = JSONArray()
    dao.getAllTests().first().forEach { t ->
      testsArray.put(JSONObject().apply {
        put("id", t.id)
        put("title", t.title)
        put("class", t.className)
        put("subject", t.subject)
        put("total_marks", t.totalMarks)
        put("test_date", t.testDate)
      })
    }
    root.put("Tests", testsArray)

    // 7. Homework
    val hwArray = JSONArray()
    dao.getAllHomework().first().forEach { h ->
      hwArray.put(JSONObject().apply {
        put("class", h.className)
        put("subject", h.subject)
        put("title", h.title)
        put("description", h.description)
        put("due_date", h.dueDate)
        put("status", h.status)
      })
    }
    root.put("Homework", hwArray)

    // 8. Performance
    val perfArray = JSONArray()
    dao.getAllPerformance().first().forEach { p ->
      perfArray.put(JSONObject().apply {
        put("student_id", p.studentId)
        put("student_name", p.studentName)
        put("month", p.monthYear)
        put("reading", p.readingRating)
        put("writing", p.writingRating)
        put("math", p.mathRating)
        put("english", p.englishRating)
        put("behaviour", p.behaviourRating)
        put("remarks", p.teacherRemarks)
      })
    }
    root.put("Performance", perfArray)

    // 9. Settings
    val settings = dao.getSettingsDirect() ?: TeacherSettings()
    root.put("Settings", JSONObject().apply {
      put("tuition_name", settings.tuitionName)
      put("teacher_name", settings.teacherName)
      put("whatsapp", settings.whatsAppNumber)
      put("easypaisa_number", settings.easypaisaNumber)
      put("easypaisa_title", settings.easypaisaTitle)
      put("google_account", settings.googleAccountEmail)
    })

    root
  }

  suspend fun saveBackupFile(context: Context, dao: TuitionDao): File = withContext(Dispatchers.IO) {
    val data = exportStructuredSheetData(dao)
    val dir = File(context.filesDir, "backups")
    if (!dir.exists()) dir.mkdirs()
    val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val file = File(dir, "TuitionBackup_$dateStr.json")
    FileOutputStream(file).use {
      it.write(data.toString(2).toByteArray())
    }
    file
  }
}
