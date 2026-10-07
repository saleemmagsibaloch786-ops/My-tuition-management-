package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val studentId: String = "",
  val name: String = "",
  val photoUri: String? = null,
  val fatherGuardianName: String = "",
  val parentWhatsApp: String = "",
  val dob: String = "",
  val gender: String = "Male",
  val school: String = "",
  val className: String = "",
  val subjects: String = "",
  val address: String = "",
  val admissionDate: String = "",
  val monthlyFee: Double = 0.0,
  val discount: Double = 0.0,
  val status: String = "Active", // "Active" | "Archived"
  val notes: String = ""
)

@Entity(tableName = "classes")
data class ClassSchedule(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val className: String = "",
  val subject: String = "",
  val dayOfWeek: String = "Monday", // Monday to Sunday or "Daily"
  val startTime: String = "04:00 PM",
  val endTime: String = "05:00 PM",
  val studentsCount: Int = 0,
  val studentNames: String = "", // Comma-separated or summary
  val notes: String = ""
)

@Entity(tableName = "attendance")
data class AttendanceRecord(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val studentId: Long = 0,
  val studentName: String = "",
  val className: String = "",
  val date: String = "", // YYYY-MM-DD
  val status: String = "Present", // "Present", "Absent", "Late", "Leave"
  val notes: String = "",
  val sentWhatsApp: Boolean = false
)

@Entity(tableName = "fee_chalans")
data class FeeChalan(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val chalanNumber: String = "",
  val studentId: Long = 0,
  val studentName: String = "",
  val fatherGuardianName: String = "",
  val className: String = "",
  val monthYear: String = "", // e.g. "October 2026"
  val issueDate: String = "",
  val dueDate: String = "",
  val monthlyFee: Double = 0.0,
  val discount: Double = 0.0,
  val previousBalance: Double = 0.0,
  val fine: Double = 0.0,
  val totalPayable: Double = 0.0,
  val paidAmount: Double = 0.0,
  val status: String = "Pending", // "Paid", "Partial", "Pending", "Overdue"
  val pdfPath: String = "",
  val driveUrl: String = "",
  val sentWhatsApp: Boolean = false
)

@Entity(tableName = "payment_receipts")
data class PaymentReceipt(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val receiptNumber: String = "",
  val chalanId: Long? = null,
  val studentId: Long = 0,
  val studentName: String = "",
  val fatherGuardianName: String = "",
  val className: String = "",
  val feeMonthYear: String = "",
  val amountPaid: Double = 0.0,
  val previousBalance: Double = 0.0,
  val remainingBalance: Double = 0.0,
  val paymentDate: String = "",
  val paymentMethod: String = "Cash", // "Cash", "Easypaisa", "Other"
  val transactionReference: String = "",
  val notes: String = "",
  val pdfPath: String = "",
  val driveUrl: String = "",
  val sentWhatsApp: Boolean = false
)

@Entity(tableName = "tests")
data class TestRecord(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String = "",
  val className: String = "",
  val subject: String = "",
  val totalMarks: Double = 100.0,
  val testDate: String = ""
)

@Entity(tableName = "test_marks")
data class TestMark(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val testId: Long = 0,
  val studentId: Long = 0,
  val studentName: String = "",
  val marksObtained: Double = 0.0,
  val percentage: Double = 0.0,
  val grade: String = "F", // A+, A, B, C, D, F
  val remarks: String = ""
)

@Entity(tableName = "homework")
data class HomeworkRecord(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val className: String = "",
  val subject: String = "",
  val title: String = "",
  val description: String = "",
  val assignedDate: String = "",
  val dueDate: String = "",
  val status: String = "Assigned" // "Assigned", "Completed", "Checked"
)

@Entity(tableName = "performance")
data class StudentPerformance(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val studentId: Long = 0,
  val studentName: String = "",
  val className: String = "",
  val monthYear: String = "", // e.g. "October 2026"
  val readingRating: Int = 4, // 1..5
  val writingRating: Int = 4,
  val mathRating: Int = 4,
  val englishRating: Int = 4,
  val homeworkRating: Int = 4,
  val participationRating: Int = 4,
  val behaviourRating: Int = 5,
  val teacherRemarks: String = ""
)

@Entity(tableName = "settings")
data class TeacherSettings(
  @PrimaryKey val id: Int = 1,
  val tuitionName: String = "Al-Ilm Tuition Centre",
  val teacherName: String = "Sir Saleem Baloch",
  val whatsAppNumber: String = "+923001234567",
  val easypaisaNumber: String = "03001234567",
  val easypaisaTitle: String = "Saleem Magsi",
  val address: String = "Street 4, Sector G-9, Islamabad",
  val defaultFee: Double = 3500.0,
  val feeDueDateDay: Int = 10,
  val chalanReminderDay: Int = 28,
  val receiptPrefix: String = "REC",
  val chalanPrefix: String = "CH",
  val googleAccountEmail: String = "Saleemmagsibaloch786@gmail.com",
  val isGoogleConnected: Boolean = true,
  val lastSyncTime: String = "Just now",
  val syncStatus: String = "Synced", // "Offline", "Syncing", "Synced", "Sync Failed"
  // WhatsApp templates
  val chalanTemplate: String = "Dear Parent, Fee chalan for {student} for {month} has been generated. Total payable: Rs. {amount}. Due date: {dueDate}. Pay via Easypaisa {easypaisaTitle} ({easypaisaNumber}). After payment, please share receipt screenshot here.",
  val reminderTemplate: String = "Reminder: Tuition fee for {student} ({month}) is pending. Amount: Rs. {amount}. Please clear before due date: {dueDate}. Thank you, {teacher}.",
  val receiptTemplate: String = "Payment Received! Received Rs. {amount} for {student} ({month}) via {method}. Remaining balance: Rs. {remaining}. Thank you, {tuition}.",
  val absenceTemplate: String = "Dear Parent, {student} was marked ABSENT today ({date}) in {className} tuition class. Please inform reason. Regards, {teacher}.",
  val testResultTemplate: String = "Test Result: {student} obtained {marks}/{total} ({percentage}%, Grade: {grade}) in {subject} test ({title}). Remarks: {remarks}. Regards, {teacher}.",
  val homeworkTemplate: String = "Tuition Homework for {className} ({subject}): \"{title}\". Description: {description}. Due date: {dueDate}. Please ensure completion. - {teacher}.",
  val performanceTemplate: String = "Monthly Performance for {student} ({month}): Behaviour: {behaviour}/5, Homework: {homework}/5, Academics: {academics}/5. Remarks: {remarks}. - {teacher}."
)
