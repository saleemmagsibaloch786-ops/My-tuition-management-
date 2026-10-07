package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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

@Database(
  entities = [
    Student::class,
    ClassSchedule::class,
    AttendanceRecord::class,
    FeeChalan::class,
    PaymentReceipt::class,
    TestRecord::class,
    TestMark::class,
    HomeworkRecord::class,
    StudentPerformance::class,
    TeacherSettings::class
  ],
  version = 1,
  exportSchema = false
)
abstract class TuitionDatabase : RoomDatabase() {
  abstract fun tuitionDao(): TuitionDao

  companion object {
    @Volatile
    private var INSTANCE: TuitionDatabase? = null

    fun getDatabase(context: Context): TuitionDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          TuitionDatabase::class.java,
          "tuition_manager_database"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
