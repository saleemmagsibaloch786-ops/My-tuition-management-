package com.example.data.repository

import android.content.Context
import com.example.data.local.TuitionDao
import com.example.data.local.TuitionDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TuitionRepository(private val dao: TuitionDao) {

  val allStudents: Flow<List<Student>> = dao.getAllStudents()
  val activeStudents: Flow<List<Student>> = dao.getActiveStudents()
  val archivedStudents: Flow<List<Student>> = dao.getArchivedStudents()
  val activeStudentsCount: Flow<Int> = dao.getActiveStudentsCount()

  val allClasses: Flow<List<ClassSchedule>> = dao.getAllClasses()
  val allAttendance: Flow<List<AttendanceRecord>> = dao.getAllAttendance()
  val allChalans: Flow<List<FeeChalan>> = dao.getAllChalans()
  val allReceipts: Flow<List<PaymentReceipt>> = dao.getAllReceipts()
  val allTests: Flow<List<TestRecord>> = dao.getAllTests()
  val allHomework: Flow<List<HomeworkRecord>> = dao.getAllHomework()
  val allPerformance: Flow<List<StudentPerformance>> = dao.getAllPerformance()
  val settings: Flow<TeacherSettings?> = dao.getSettings()

  suspend fun getStudentById(id: Long): Student? = dao.getStudentByIdDirect(id)
  fun getStudentStream(id: Long): Flow<Student?> = dao.getStudentById(id)

  fun getAttendanceByDate(date: String): Flow<List<AttendanceRecord>> = dao.getAttendanceByDate(date)
  fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceRecord>> = dao.getAttendanceForStudent(studentId)

  fun getChalansForStudent(studentId: Long): Flow<List<FeeChalan>> = dao.getChalansForStudent(studentId)
  fun getReceiptsForStudent(studentId: Long): Flow<List<PaymentReceipt>> = dao.getReceiptsForStudent(studentId)
  fun getMarksForTest(testId: Long): Flow<List<TestMark>> = dao.getMarksForTest(testId)
  fun getMarksForStudent(studentId: Long): Flow<List<TestMark>> = dao.getMarksForStudent(studentId)
  fun getPerformanceForStudent(studentId: Long): Flow<List<StudentPerformance>> = dao.getPerformanceForStudent(studentId)

  suspend fun insertStudent(student: Student): Long = dao.insertStudent(student)
  suspend fun updateStudent(student: Student) = dao.updateStudent(student)
  suspend fun deleteStudent(student: Student) = dao.deleteStudent(student)

  suspend fun insertClass(schedule: ClassSchedule): Long = dao.insertClass(schedule)
  suspend fun updateClass(schedule: ClassSchedule) = dao.updateClass(schedule)
  suspend fun deleteClass(schedule: ClassSchedule) = dao.deleteClass(schedule)

  suspend fun recordAttendance(record: AttendanceRecord): Long = dao.insertAttendance(record)
  suspend fun updateAttendance(record: AttendanceRecord) = dao.updateAttendance(record)

  suspend fun markAllPresent(date: String, className: String? = null) {
    val students = dao.getActiveStudents().first()
    val filtered = if (className.isNullOrBlank() || className == "All") students else students.filter { it.className == className }
    val existing = dao.getAttendanceByDate(date).first().associateBy { it.studentId }
    val toSave = filtered.map { student ->
      val exist = existing[student.id]
      if (exist != null) {
        exist.copy(status = "Present")
      } else {
        AttendanceRecord(
          studentId = student.id,
          studentName = student.name,
          className = student.className,
          date = date,
          status = "Present"
        )
      }
    }
    dao.insertAttendanceList(toSave)
  }

  suspend fun insertChalan(chalan: FeeChalan): Long = dao.insertChalan(chalan)
  suspend fun updateChalan(chalan: FeeChalan) = dao.updateChalan(chalan)
  suspend fun deleteChalan(chalan: FeeChalan) = dao.deleteChalan(chalan)

  suspend fun generateChalanForStudent(student: Student, monthYear: String, dueDate: String): FeeChalan {
    val total = (student.monthlyFee - student.discount).coerceAtLeast(0.0)
    val currentSetting = dao.getSettingsDirect() ?: TeacherSettings()
    val prefix = currentSetting.chalanPrefix.ifBlank { "CH" }
    val chalanNumber = "$prefix-${System.currentTimeMillis() % 1000000}"
    val issueDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

    val chalan = FeeChalan(
      chalanNumber = chalanNumber,
      studentId = student.id,
      studentName = student.name,
      fatherGuardianName = student.fatherGuardianName,
      className = student.className,
      monthYear = monthYear,
      issueDate = issueDate,
      dueDate = dueDate,
      monthlyFee = student.monthlyFee,
      discount = student.discount,
      previousBalance = 0.0,
      fine = 0.0,
      totalPayable = total,
      paidAmount = 0.0,
      status = "Pending"
    )
    val id = dao.insertChalan(chalan)
    return chalan.copy(id = id)
  }

  suspend fun generateBulkChalans(monthYear: String, dueDate: String, onProgress: (current: Int, total: Int) -> Unit): Int {
    val students = dao.getActiveStudents().first()
    val total = students.size
    val currentSetting = dao.getSettingsDirect() ?: TeacherSettings()
    val prefix = currentSetting.chalanPrefix.ifBlank { "CH" }
    val issueDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

    students.forEachIndexed { index, student ->
      val payable = (student.monthlyFee - student.discount).coerceAtLeast(0.0)
      val chalanNumber = "$prefix-${(System.currentTimeMillis() % 100000) + index}"
      val chalan = FeeChalan(
        chalanNumber = chalanNumber,
        studentId = student.id,
        studentName = student.name,
        fatherGuardianName = student.fatherGuardianName,
        className = student.className,
        monthYear = monthYear,
        issueDate = issueDate,
        dueDate = dueDate,
        monthlyFee = student.monthlyFee,
        discount = student.discount,
        previousBalance = 0.0,
        fine = 0.0,
        totalPayable = payable,
        paidAmount = 0.0,
        status = "Pending"
      )
      dao.insertChalan(chalan)
      onProgress(index + 1, total)
      delay(40) // Smooth progress feedback
    }
    return total
  }

  suspend fun recordPayment(
    studentId: Long,
    feeMonthYear: String,
    amountPaid: Double,
    paymentMethod: String,
    transactionRef: String,
    notes: String,
    chalanId: Long? = null
  ): PaymentReceipt {
    val student = dao.getStudentByIdDirect(studentId)
    val currentSetting = dao.getSettingsDirect() ?: TeacherSettings()
    val prefix = currentSetting.receiptPrefix.ifBlank { "REC" }
    val receiptNumber = "$prefix-${System.currentTimeMillis() % 1000000}"
    val paymentDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

    // If chalan exists, update it
    var previousBalance = 0.0
    var remainingBalance = 0.0
    if (chalanId != null) {
      val chalan = dao.getChalanById(chalanId)
      if (chalan != null) {
        val newPaid = chalan.paidAmount + amountPaid
        val remaining = (chalan.totalPayable - newPaid).coerceAtLeast(0.0)
        previousBalance = chalan.totalPayable - chalan.paidAmount
        remainingBalance = remaining
        val newStatus = when {
          remaining <= 0.0 -> "Paid"
          newPaid > 0.0 -> "Partial"
          else -> "Pending"
        }
        dao.updateChalan(chalan.copy(paidAmount = newPaid, status = newStatus))
      }
    } else {
      val expectedFee = (student?.monthlyFee ?: 0.0) - (student?.discount ?: 0.0)
      previousBalance = expectedFee
      remainingBalance = (expectedFee - amountPaid).coerceAtLeast(0.0)
    }

    val receipt = PaymentReceipt(
      receiptNumber = receiptNumber,
      chalanId = chalanId,
      studentId = studentId,
      studentName = student?.name ?: "Unknown",
      fatherGuardianName = student?.fatherGuardianName ?: "",
      className = student?.className ?: "",
      feeMonthYear = feeMonthYear,
      amountPaid = amountPaid,
      previousBalance = previousBalance,
      remainingBalance = remainingBalance,
      paymentDate = paymentDate,
      paymentMethod = paymentMethod,
      transactionReference = transactionRef,
      notes = notes
    )
    val id = dao.insertReceipt(receipt)
    return receipt.copy(id = id)
  }

  suspend fun insertTest(test: TestRecord): Long = dao.insertTest(test)
  suspend fun deleteTest(test: TestRecord) = dao.deleteTest(test)

  suspend fun recordTestMark(testId: Long, studentId: Long, marks: Double, totalMarks: Double, remarks: String) {
    val student = dao.getStudentByIdDirect(studentId)
    val pct = if (totalMarks > 0) (marks / totalMarks) * 100.0 else 0.0
    val grade = when {
      pct >= 85 -> "A+"
      pct >= 75 -> "A"
      pct >= 65 -> "B"
      pct >= 55 -> "C"
      pct >= 45 -> "D"
      else -> "F"
    }
    val mark = TestMark(
      testId = testId,
      studentId = studentId,
      studentName = student?.name ?: "Student",
      marksObtained = marks,
      percentage = pct,
      grade = grade,
      remarks = remarks
    )
    dao.insertTestMark(mark)
  }

  suspend fun insertHomework(hw: HomeworkRecord): Long = dao.insertHomework(hw)
  suspend fun updateHomework(hw: HomeworkRecord) = dao.updateHomework(hw)
  suspend fun deleteHomework(hw: HomeworkRecord) = dao.deleteHomework(hw)

  suspend fun savePerformance(perf: StudentPerformance): Long = dao.insertPerformance(perf)

  suspend fun saveSettings(settings: TeacherSettings) = dao.saveSettings(settings)

  suspend fun triggerGoogleSync(): Boolean {
    val current = dao.getSettingsDirect() ?: TeacherSettings()
    dao.saveSettings(current.copy(syncStatus = "Syncing"))
    delay(1200) // Simulating fast cloud synchronization to Google Sheets and Drive
    val time = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
    dao.saveSettings(current.copy(syncStatus = "Synced", lastSyncTime = time))
    return true
  }

  suspend fun seedInitialDataIfEmpty() {
    val count = dao.getActiveStudents().firstOrNull()?.size ?: 0
    if (count > 0) return

    val currentSettings = dao.getSettingsDirect()
    if (currentSettings == null) {
      dao.saveSettings(TeacherSettings())
    }

    val students = listOf(
      Student(
        studentId = "STU-001",
        name = "Hamza Ali",
        fatherGuardianName = "Tariq Ali",
        parentWhatsApp = "+923005551234",
        dob = "12-05-2009",
        gender = "Male",
        school = "Army Public School",
        className = "Class 10",
        subjects = "Mathematics, Physics, Chemistry",
        address = "Street 12, G-10/2, Islamabad",
        admissionDate = "01-08-2025",
        monthlyFee = 4000.0,
        discount = 0.0,
        status = "Active",
        notes = "Excels in algebra, needs practice in physics numericals."
      ),
      Student(
        studentId = "STU-002",
        name = "Ayesha Khan",
        fatherGuardianName = "Rashid Khan",
        parentWhatsApp = "+923214445678",
        dob = "24-11-2010",
        gender = "Female",
        school = "Beaconhouse School",
        className = "Class 9",
        subjects = "Mathematics, English, Biology",
        address = "House 45, F-11/3, Islamabad",
        admissionDate = "15-08-2025",
        monthlyFee = 3500.0,
        discount = 500.0,
        status = "Active",
        notes = "Very attentive, homework is always on time."
      ),
      Student(
        studentId = "STU-003",
        name = "Zaid Baloch",
        fatherGuardianName = "Naseer Baloch",
        parentWhatsApp = "+923338889900",
        dob = "05-02-2009",
        gender = "Male",
        school = "Islamabad College for Boys",
        className = "Class 10",
        subjects = "Mathematics, Computer Science",
        address = "Flat 8, I-8 Markaz, Islamabad",
        admissionDate = "10-09-2025",
        monthlyFee = 3500.0,
        discount = 0.0,
        status = "Active",
        notes = "Good logic, working on geometry."
      ),
      Student(
        studentId = "STU-004",
        name = "Fatima Zahra",
        fatherGuardianName = "Dr. Zahid Iqbal",
        parentWhatsApp = "+923451112233",
        dob = "18-07-2011",
        gender = "Female",
        school = "City School",
        className = "Class 8",
        subjects = "General Science, Mathematics, English",
        address = "Street 6, E-11, Islamabad",
        admissionDate = "01-09-2025",
        monthlyFee = 3000.0,
        discount = 0.0,
        status = "Active",
        notes = "Consistent participation."
      ),
      Student(
        studentId = "STU-005",
        name = "Bilal Ahmed",
        fatherGuardianName = "Sohail Ahmed",
        parentWhatsApp = "+923129998877",
        dob = "09-03-2010",
        gender = "Male",
        school = "Froebel's International",
        className = "Class 9",
        subjects = "Mathematics, Physics",
        address = "Block C, Bahria Enclave",
        admissionDate = "20-09-2025",
        monthlyFee = 3500.0,
        discount = 0.0,
        status = "Active",
        notes = "Regular and polite."
      )
    )
    dao.insertStudents(students)

    val classes = listOf(
      ClassSchedule(
        className = "Class 10",
        subject = "Mathematics & Physics",
        dayOfWeek = "Daily",
        startTime = "04:00 PM",
        endTime = "05:30 PM",
        studentsCount = 2,
        studentNames = "Hamza Ali, Zaid Baloch",
        notes = "Chapter 4 Quadratic Equations"
      ),
      ClassSchedule(
        className = "Class 9",
        subject = "Mathematics & Science",
        dayOfWeek = "Daily",
        startTime = "05:30 PM",
        endTime = "07:00 PM",
        studentsCount = 2,
        studentNames = "Ayesha Khan, Bilal Ahmed",
        notes = "Matrices and Determinants"
      ),
      ClassSchedule(
        className = "Class 8",
        subject = "General Science & Math",
        dayOfWeek = "Mon, Wed, Fri",
        startTime = "03:00 PM",
        endTime = "04:00 PM",
        studentsCount = 1,
        studentNames = "Fatima Zahra",
        notes = "Cellular Biology"
      )
    )
    dao.insertClasses(classes)

    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val attendance = listOf(
      AttendanceRecord(studentId = 1, studentName = "Hamza Ali", className = "Class 10", date = today, status = "Present"),
      AttendanceRecord(studentId = 2, studentName = "Ayesha Khan", className = "Class 9", date = today, status = "Present"),
      AttendanceRecord(studentId = 3, studentName = "Zaid Baloch", className = "Class 10", date = today, status = "Present"),
      AttendanceRecord(studentId = 4, studentName = "Fatima Zahra", className = "Class 8", date = today, status = "Late"),
      AttendanceRecord(studentId = 5, studentName = "Bilal Ahmed", className = "Class 9", date = today, status = "Absent")
    )
    dao.insertAttendanceList(attendance)

    val test = TestRecord(
      title = "Monthly Math Assessment - Algebra",
      className = "Class 10",
      subject = "Mathematics",
      totalMarks = 50.0,
      testDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    )
    val testId = dao.insertTest(test)
    val marks = listOf(
      TestMark(testId = testId, studentId = 1, studentName = "Hamza Ali", marksObtained = 46.0, percentage = 92.0, grade = "A+", remarks = "Brilliant attempt in Section B"),
      TestMark(testId = testId, studentId = 3, studentName = "Zaid Baloch", marksObtained = 40.5, percentage = 81.0, grade = "A", remarks = "Good work, review formula step 3")
    )
    dao.insertTestMarks(marks)

    val homework = listOf(
      HomeworkRecord(
        className = "Class 10",
        subject = "Mathematics",
        title = "Exercise 4.2 Questions 1 to 8",
        description = "Solve quadratic formula problems on neat tuition register.",
        assignedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
        dueDate = "Tomorrow 4 PM",
        status = "Assigned"
      ),
      HomeworkRecord(
        className = "Class 9",
        subject = "Mathematics",
        title = "Matrix Inverse Calculation",
        description = "Find determinant and adjugate matrix for given 2x2 systems.",
        assignedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
        dueDate = "Tomorrow 5:30 PM",
        status = "Assigned"
      )
    )
    dao.insertHomeworkList(homework)

    // Seed sample chalans and receipts
    val month = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
    val chalan1 = FeeChalan(
      chalanNumber = "CH-1001",
      studentId = 1,
      studentName = "Hamza Ali",
      fatherGuardianName = "Tariq Ali",
      className = "Class 10",
      monthYear = month,
      issueDate = "01 " + month,
      dueDate = "10 " + month,
      monthlyFee = 4000.0,
      discount = 0.0,
      previousBalance = 0.0,
      fine = 0.0,
      totalPayable = 4000.0,
      paidAmount = 4000.0,
      status = "Paid"
    )
    val chalan1Id = dao.insertChalan(chalan1)

    val receipt1 = PaymentReceipt(
      receiptNumber = "REC-1001",
      chalanId = chalan1Id,
      studentId = 1,
      studentName = "Hamza Ali",
      fatherGuardianName = "Tariq Ali",
      className = "Class 10",
      feeMonthYear = month,
      amountPaid = 4000.0,
      previousBalance = 4000.0,
      remainingBalance = 0.0,
      paymentDate = "05 " + month,
      paymentMethod = "Easypaisa",
      transactionReference = "EP-99887766",
      notes = "Paid on time via Easypaisa."
    )
    dao.insertReceipt(receipt1)

    val chalan2 = FeeChalan(
      chalanNumber = "CH-1002",
      studentId = 2,
      studentName = "Ayesha Khan",
      fatherGuardianName = "Rashid Khan",
      className = "Class 9",
      monthYear = month,
      issueDate = "01 " + month,
      dueDate = "10 " + month,
      monthlyFee = 3500.0,
      discount = 500.0,
      previousBalance = 0.0,
      fine = 0.0,
      totalPayable = 3000.0,
      paidAmount = 0.0,
      status = "Pending"
    )
    dao.insertChalan(chalan2)

    val chalan3 = FeeChalan(
      chalanNumber = "CH-1003",
      studentId = 3,
      studentName = "Zaid Baloch",
      fatherGuardianName = "Naseer Baloch",
      className = "Class 10",
      monthYear = month,
      issueDate = "01 " + month,
      dueDate = "10 " + month,
      monthlyFee = 3500.0,
      discount = 0.0,
      previousBalance = 0.0,
      fine = 0.0,
      totalPayable = 3500.0,
      paidAmount = 0.0,
      status = "Pending"
    )
    dao.insertChalan(chalan3)

    // Performance
    val perf = StudentPerformance(
      studentId = 1,
      studentName = "Hamza Ali",
      className = "Class 10",
      monthYear = month,
      readingRating = 5,
      writingRating = 4,
      mathRating = 5,
      englishRating = 4,
      homeworkRating = 5,
      participationRating = 5,
      behaviourRating = 5,
      teacherRemarks = "Excellent student with top motivation. Keep up the high standard!"
    )
    dao.insertPerformance(perf)
  }

  companion object {
    @Volatile
    private var INSTANCE: TuitionRepository? = null

    fun getRepository(context: Context): TuitionRepository {
      return INSTANCE ?: synchronized(this) {
        val db = TuitionDatabase.getDatabase(context)
        val instance = TuitionRepository(db.tuitionDao())
        INSTANCE = instance
        // Seed in IO coroutine
        CoroutineScope(Dispatchers.IO).launch {
          instance.seedInitialDataIfEmpty()
        }
        instance
      }
    }
  }
}
