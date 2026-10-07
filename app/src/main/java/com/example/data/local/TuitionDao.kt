package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface TuitionDao {

  // --- Students ---
  @Query("SELECT * FROM students ORDER BY name ASC")
  fun getAllStudents(): Flow<List<Student>>

  @Query("SELECT * FROM students WHERE status = 'Active' ORDER BY name ASC")
  fun getActiveStudents(): Flow<List<Student>>

  @Query("SELECT * FROM students WHERE status = 'Archived' ORDER BY name ASC")
  fun getArchivedStudents(): Flow<List<Student>>

  @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
  fun getStudentById(id: Long): Flow<Student?>

  @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
  suspend fun getStudentByIdDirect(id: Long): Student?

  @Query("SELECT COUNT(*) FROM students WHERE status = 'Active'")
  fun getActiveStudentsCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStudent(student: Student): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStudents(students: List<Student>)

  @Update
  suspend fun updateStudent(student: Student)

  @Delete
  suspend fun deleteStudent(student: Student)

  // --- Classes ---
  @Query("SELECT * FROM classes ORDER BY startTime ASC")
  fun getAllClasses(): Flow<List<ClassSchedule>>

  @Query("SELECT * FROM classes WHERE dayOfWeek = :day OR dayOfWeek = 'Daily' ORDER BY startTime ASC")
  fun getClassesForDay(day: String): Flow<List<ClassSchedule>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertClass(schedule: ClassSchedule): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertClasses(schedules: List<ClassSchedule>)

  @Update
  suspend fun updateClass(schedule: ClassSchedule)

  @Delete
  suspend fun deleteClass(schedule: ClassSchedule)

  // --- Attendance ---
  @Query("SELECT * FROM attendance WHERE date = :date")
  fun getAttendanceByDate(date: String): Flow<List<AttendanceRecord>>

  @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
  fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceRecord>>

  @Query("SELECT * FROM attendance ORDER BY date DESC")
  fun getAllAttendance(): Flow<List<AttendanceRecord>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAttendance(record: AttendanceRecord): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAttendanceList(records: List<AttendanceRecord>)

  @Update
  suspend fun updateAttendance(record: AttendanceRecord)

  // --- Fee Chalans ---
  @Query("SELECT * FROM fee_chalans ORDER BY id DESC")
  fun getAllChalans(): Flow<List<FeeChalan>>

  @Query("SELECT * FROM fee_chalans WHERE studentId = :studentId ORDER BY id DESC")
  fun getChalansForStudent(studentId: Long): Flow<List<FeeChalan>>

  @Query("SELECT * FROM fee_chalans WHERE monthYear = :monthYear ORDER BY studentName ASC")
  fun getChalansByMonth(monthYear: String): Flow<List<FeeChalan>>

  @Query("SELECT * FROM fee_chalans WHERE id = :id LIMIT 1")
  suspend fun getChalanById(id: Long): FeeChalan?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertChalan(chalan: FeeChalan): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertChalans(chalans: List<FeeChalan>)

  @Update
  suspend fun updateChalan(chalan: FeeChalan)

  @Delete
  suspend fun deleteChalan(chalan: FeeChalan)

  // --- Payment Receipts ---
  @Query("SELECT * FROM payment_receipts ORDER BY id DESC")
  fun getAllReceipts(): Flow<List<PaymentReceipt>>

  @Query("SELECT * FROM payment_receipts WHERE studentId = :studentId ORDER BY id DESC")
  fun getReceiptsForStudent(studentId: Long): Flow<List<PaymentReceipt>>

  @Query("SELECT * FROM payment_receipts WHERE id = :id LIMIT 1")
  suspend fun getReceiptById(id: Long): PaymentReceipt?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReceipt(receipt: PaymentReceipt): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReceipts(receipts: List<PaymentReceipt>)

  @Update
  suspend fun updateReceipt(receipt: PaymentReceipt)

  // --- Tests ---
  @Query("SELECT * FROM tests ORDER BY testDate DESC")
  fun getAllTests(): Flow<List<TestRecord>>

  @Query("SELECT * FROM tests WHERE id = :id LIMIT 1")
  suspend fun getTestById(id: Long): TestRecord?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTest(test: TestRecord): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTests(tests: List<TestRecord>)

  @Delete
  suspend fun deleteTest(test: TestRecord)

  // --- Test Marks ---
  @Query("SELECT * FROM test_marks WHERE testId = :testId")
  fun getMarksForTest(testId: Long): Flow<List<TestMark>>

  @Query("SELECT * FROM test_marks WHERE studentId = :studentId ORDER BY id DESC")
  fun getMarksForStudent(studentId: Long): Flow<List<TestMark>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTestMark(mark: TestMark): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTestMarks(marks: List<TestMark>)

  // --- Homework ---
  @Query("SELECT * FROM homework ORDER BY id DESC")
  fun getAllHomework(): Flow<List<HomeworkRecord>>

  @Query("SELECT * FROM homework WHERE className = :className ORDER BY id DESC")
  fun getHomeworkForClass(className: String): Flow<List<HomeworkRecord>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertHomework(hw: HomeworkRecord): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertHomeworkList(list: List<HomeworkRecord>)

  @Update
  suspend fun updateHomework(hw: HomeworkRecord)

  @Delete
  suspend fun deleteHomework(hw: HomeworkRecord)

  // --- Performance ---
  @Query("SELECT * FROM performance WHERE studentId = :studentId ORDER BY monthYear DESC")
  fun getPerformanceForStudent(studentId: Long): Flow<List<StudentPerformance>>

  @Query("SELECT * FROM performance ORDER BY id DESC")
  fun getAllPerformance(): Flow<List<StudentPerformance>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPerformance(perf: StudentPerformance): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPerformanceList(list: List<StudentPerformance>)

  // --- Settings ---
  @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
  fun getSettings(): Flow<TeacherSettings?>

  @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
  suspend fun getSettingsDirect(): TeacherSettings?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveSettings(settings: TeacherSettings)
}
