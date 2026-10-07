package com.example.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.FeeChalan
import com.example.data.model.PaymentReceipt
import com.example.data.model.Student
import com.example.data.model.StudentPerformance
import com.example.data.model.TeacherSettings
import com.example.data.model.TestMark
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

  /**
   * Generates a professional Tuition Fee Chalan PDF (Standard A4: 595 x 842 points)
   */
  fun generateFeeChalanPdf(
    context: Context,
    chalan: FeeChalan,
    settings: TeacherSettings
  ): File {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Background Canvas
    canvas.drawColor(Color.WHITE)

    // Top Header Banner
    paint.color = Color.parseColor("#1A365D")
    canvas.drawRect(0f, 0f, 595f, 110f, paint)

    // Tuition Name & Subtitle
    paint.color = Color.WHITE
    paint.textSize = 22f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText(settings.tuitionName.ifBlank { "My Tuition Manager" }, 40f, 48f, paint)

    paint.textSize = 13f
    paint.typeface = Typeface.DEFAULT
    canvas.drawText("Teacher: ${settings.teacherName}  •  WhatsApp: ${settings.whatsAppNumber}", 40f, 72f, paint)
    canvas.drawText(settings.address, 40f, 92f, paint)

    // Chalan Badge
    paint.color = Color.parseColor("#FEF3C7")
    val badgeRect = RectF(410f, 32f, 555f, 82f)
    canvas.drawRoundRect(badgeRect, 8f, 8f, paint)

    paint.color = Color.parseColor("#92400E")
    paint.textSize = 14f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("FEE CHALAN", 435f, 54f, paint)

    paint.textSize = 11f
    paint.typeface = Typeface.DEFAULT
    canvas.drawText(chalan.chalanNumber, 435f, 72f, paint)

    // Student Information Box
    paint.color = Color.parseColor("#F8FAFC")
    val infoBox = RectF(40f, 130f, 555f, 220f)
    canvas.drawRoundRect(infoBox, 8f, 8f, paint)
    paint.color = Color.parseColor("#CBD5E1")
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 1.5f
    canvas.drawRoundRect(infoBox, 8f, 8f, paint)
    paint.style = Paint.Style.FILL

    // Student Info Text
    paint.color = Color.parseColor("#1E293B")
    paint.textSize = 12f
    paint.typeface = Typeface.DEFAULT

    canvas.drawText("Student Name:", 60f, 160f, paint)
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText(chalan.studentName, 160f, 160f, paint)

    paint.typeface = Typeface.DEFAULT
    canvas.drawText("Father/Guardian:", 60f, 185f, paint)
    canvas.drawText(chalan.fatherGuardianName.ifBlank { "N/A" }, 160f, 185f, paint)

    canvas.drawText("Class:", 340f, 160f, paint)
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText(chalan.className, 410f, 160f, paint)

    paint.typeface = Typeface.DEFAULT
    canvas.drawText("Fee Month:", 340f, 185f, paint)
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.color = Color.parseColor("#1E40AF")
    canvas.drawText(chalan.monthYear, 410f, 185f, paint)

    canvas.drawText("Due Date:", 340f, 205f, paint)
    paint.color = Color.parseColor("#DC2626")
    canvas.drawText(chalan.dueDate, 410f, 205f, paint)

    // Table Header
    val tableTop = 245f
    paint.color = Color.parseColor("#1A365D")
    canvas.drawRect(40f, tableTop, 555f, tableTop + 30f, paint)

    paint.color = Color.WHITE
    paint.textSize = 12f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("Description", 60f, tableTop + 20f, paint)
    canvas.drawText("Amount (PKR)", 440f, tableTop + 20f, paint)

    // Table Rows
    var currentY = tableTop + 30f
    fun drawRow(label: String, value: String, isBold: Boolean = false, isAlt: Boolean = false) {
      if (isAlt) {
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRect(40f, currentY, 555f, currentY + 30f, paint)
      }
      paint.color = Color.parseColor("#1E293B")
      paint.textSize = 12f
      paint.typeface = if (isBold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
      canvas.drawText(label, 60f, currentY + 20f, paint)
      canvas.drawText(value, 450f, currentY + 20f, paint)

      // Divider
      paint.color = Color.parseColor("#E2E8F0")
      paint.strokeWidth = 1f
      canvas.drawLine(40f, currentY + 30f, 555f, currentY + 30f, paint)
      currentY += 30f
    }

    drawRow("Tuition Monthly Fee", "Rs. ${chalan.monthlyFee.toInt()}", isAlt = false)
    if (chalan.discount > 0) {
      drawRow("Special Discount / Concession", "- Rs. ${chalan.discount.toInt()}", isAlt = true)
    }
    if (chalan.previousBalance > 0) {
      drawRow("Previous Arrears / Balance", "Rs. ${chalan.previousBalance.toInt()}", isAlt = false)
    }
    if (chalan.fine > 0) {
      drawRow("Late Fee / Fine", "Rs. ${chalan.fine.toInt()}", isAlt = true)
    }

    // Total Payable Highlight Row
    paint.color = Color.parseColor("#DBEAFE")
    canvas.drawRect(40f, currentY, 555f, currentY + 36f, paint)

    paint.color = Color.parseColor("#1E3A8A")
    paint.textSize = 14f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("TOTAL PAYABLE", 60f, currentY + 23f, paint)
    canvas.drawText("Rs. ${chalan.totalPayable.toInt()}", 440f, currentY + 23f, paint)
    currentY += 50f

    // Payment Instructions & Easypaisa Box
    val epBox = RectF(40f, currentY, 555f, currentY + 130f)
    paint.color = Color.parseColor("#ECFDF5")
    canvas.drawRoundRect(epBox, 8f, 8f, paint)
    paint.color = Color.parseColor("#10B981")
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 1.5f
    canvas.drawRoundRect(epBox, 8f, 8f, paint)
    paint.style = Paint.Style.FILL

    paint.color = Color.parseColor("#065F46")
    paint.textSize = 13f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("EASYPAISA / ONLINE PAYMENT DETAILS", 60f, currentY + 28f, paint)

    paint.color = Color.parseColor("#1E293B")
    paint.textSize = 12f
    paint.typeface = Typeface.DEFAULT
    canvas.drawText("Account Title: ${settings.easypaisaTitle}", 60f, currentY + 54f, paint)
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("Account Number: ${settings.easypaisaNumber}", 60f, currentY + 76f, paint)

    paint.color = Color.parseColor("#047857")
    paint.textSize = 11f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
    canvas.drawText("Note: After online payment, please send the payment screenshot through WhatsApp.", 60f, currentY + 104f, paint)

    // Footer Signatures
    val footerY = 650f
    paint.color = Color.parseColor("#64748B")
    paint.strokeWidth = 1f
    paint.style = Paint.Style.STROKE
    canvas.drawLine(60f, footerY, 200f, footerY, paint)
    canvas.drawLine(390f, footerY, 530f, footerY, paint)
    paint.style = Paint.Style.FILL

    paint.textSize = 10f
    paint.typeface = Typeface.DEFAULT
    canvas.drawText("Parent's Signature", 90f, footerY + 16f, paint)
    canvas.drawText("Teacher's Signature", 420f, footerY + 16f, paint)

    // Bottom note
    paint.textSize = 9f
    paint.color = Color.parseColor("#94A3B8")
    canvas.drawText("Generated by My Tuition Manager • Official Teacher Fee Chalan", 160f, 810f, paint)

    document.finishPage(page)

    // Save PDF file
    val dir = File(context.filesDir, "chalans")
    if (!dir.exists()) dir.mkdirs()
    val file = File(dir, "${chalan.chalanNumber}.pdf")
    val fos = FileOutputStream(file)
    document.writeTo(fos)
    document.close()
    fos.close()

    return file
  }

  /**
   * Generates a professional Fee Payment Receipt PDF
   */
  fun generatePaymentReceiptPdf(
    context: Context,
    receipt: PaymentReceipt,
    settings: TeacherSettings
  ): File {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    canvas.drawColor(Color.WHITE)

    // Header
    paint.color = Color.parseColor("#065F46")
    canvas.drawRect(0f, 0f, 595f, 110f, paint)

    paint.color = Color.WHITE
    paint.textSize = 22f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText(settings.tuitionName, 40f, 48f, paint)

    paint.textSize = 13f
    paint.typeface = Typeface.DEFAULT
    canvas.drawText("Teacher: ${settings.teacherName}  •  WhatsApp: ${settings.whatsAppNumber}", 40f, 72f, paint)
    canvas.drawText(settings.address, 40f, 92f, paint)

    // Receipt Badge
    paint.color = Color.parseColor("#D1FAE5")
    val badgeRect = RectF(400f, 32f, 555f, 82f)
    canvas.drawRoundRect(badgeRect, 8f, 8f, paint)

    paint.color = Color.parseColor("#065F46")
    paint.textSize = 14f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("PAYMENT RECEIPT", 418f, 54f, paint)

    paint.textSize = 11f
    paint.typeface = Typeface.DEFAULT
    canvas.drawText(receipt.receiptNumber, 418f, 72f, paint)

    // Details Box
    paint.color = Color.parseColor("#F8FAFC")
    val infoBox = RectF(40f, 130f, 555f, 230f)
    canvas.drawRoundRect(infoBox, 8f, 8f, paint)
    paint.color = Color.parseColor("#CBD5E1")
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 1.5f
    canvas.drawRoundRect(infoBox, 8f, 8f, paint)
    paint.style = Paint.Style.FILL

    paint.color = Color.parseColor("#1E293B")
    paint.textSize = 12f
    paint.typeface = Typeface.DEFAULT

    canvas.drawText("Student Name:", 60f, 160f, paint)
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText(receipt.studentName, 170f, 160f, paint)

    paint.typeface = Typeface.DEFAULT
    canvas.drawText("Father/Guardian:", 60f, 185f, paint)
    canvas.drawText(receipt.fatherGuardianName.ifBlank { "N/A" }, 170f, 185f, paint)

    canvas.drawText("Class:", 340f, 160f, paint)
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText(receipt.className, 410f, 160f, paint)

    paint.typeface = Typeface.DEFAULT
    canvas.drawText("Fee Month:", 340f, 185f, paint)
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText(receipt.feeMonthYear, 410f, 185f, paint)

    paint.typeface = Typeface.DEFAULT
    canvas.drawText("Payment Date:", 60f, 210f, paint)
    canvas.drawText(receipt.paymentDate, 170f, 210f, paint)

    canvas.drawText("Payment Method:", 340f, 210f, paint)
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText(receipt.paymentMethod, 450f, 210f, paint)

    // Financial Breakdown
    val tableTop = 260f
    paint.color = Color.parseColor("#065F46")
    canvas.drawRect(40f, tableTop, 555f, tableTop + 30f, paint)

    paint.color = Color.WHITE
    paint.textSize = 12f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("Particulars", 60f, tableTop + 20f, paint)
    canvas.drawText("Amount (PKR)", 440f, tableTop + 20f, paint)

    var currentY = tableTop + 30f
    fun drawRow(label: String, value: String, isAlt: Boolean = false) {
      if (isAlt) {
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRect(40f, currentY, 555f, currentY + 30f, paint)
      }
      paint.color = Color.parseColor("#1E293B")
      paint.textSize = 12f
      paint.typeface = Typeface.DEFAULT
      canvas.drawText(label, 60f, currentY + 20f, paint)
      canvas.drawText(value, 450f, currentY + 20f, paint)

      paint.color = Color.parseColor("#E2E8F0")
      paint.strokeWidth = 1f
      canvas.drawLine(40f, currentY + 30f, 555f, currentY + 30f, paint)
      currentY += 30f
    }

    drawRow("Previous Total Due", "Rs. ${receipt.previousBalance.toInt()}", isAlt = false)
    drawRow("Amount Received", "Rs. ${receipt.amountPaid.toInt()}", isAlt = true)
    drawRow("Remaining Balance", "Rs. ${receipt.remainingBalance.toInt()}", isAlt = false)
    if (receipt.transactionReference.isNotBlank()) {
      drawRow("Transaction Reference", receipt.transactionReference, isAlt = true)
    }

    // PAID STAMP
    paint.color = Color.parseColor("#10B981")
    paint.textSize = 28f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    val stampBox = RectF(380f, currentY + 30f, 540f, currentY + 80f)
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 3f
    canvas.drawRoundRect(stampBox, 10f, 10f, paint)
    paint.style = Paint.Style.FILL
    canvas.drawText("PAID", 430f, currentY + 65f, paint)

    if (receipt.notes.isNotBlank()) {
      paint.color = Color.parseColor("#475569")
      paint.textSize = 11f
      paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
      canvas.drawText("Notes: ${receipt.notes}", 60f, currentY + 50f, paint)
    }

    // Signature
    val footerY = 620f
    paint.color = Color.parseColor("#64748B")
    paint.strokeWidth = 1f
    paint.style = Paint.Style.STROKE
    canvas.drawLine(390f, footerY, 530f, footerY, paint)
    paint.style = Paint.Style.FILL

    paint.textSize = 10f
    paint.typeface = Typeface.DEFAULT
    canvas.drawText("Teacher's Stamp & Signature", 395f, footerY + 16f, paint)

    paint.textSize = 9f
    paint.color = Color.parseColor("#94A3B8")
    canvas.drawText("Generated by My Tuition Manager • Official Payment Receipt", 160f, 810f, paint)

    document.finishPage(page)

    val dir = File(context.filesDir, "receipts")
    if (!dir.exists()) dir.mkdirs()
    val file = File(dir, "${receipt.receiptNumber}.pdf")
    val fos = FileOutputStream(file)
    document.writeTo(fos)
    document.close()
    fos.close()

    return file
  }

  /**
   * Generates a comprehensive Student Monthly Progress Report PDF
   */
  fun generateStudentReportPdf(
    context: Context,
    student: Student,
    attendancePct: Int,
    tests: List<TestMark>,
    performance: StudentPerformance?,
    settings: TeacherSettings
  ): File {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    canvas.drawColor(Color.WHITE)

    // Header
    paint.color = Color.parseColor("#1E3A8A")
    canvas.drawRect(0f, 0f, 595f, 100f, paint)

    paint.color = Color.WHITE
    paint.textSize = 20f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText(settings.tuitionName, 40f, 44f, paint)

    paint.textSize = 12f
    paint.typeface = Typeface.DEFAULT
    canvas.drawText("Teacher: ${settings.teacherName}  •  ${settings.address}", 40f, 68f, paint)

    // Title
    paint.color = Color.parseColor("#1E293B")
    paint.textSize = 16f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("STUDENT MONTHLY PROGRESS REPORT", 40f, 130f, paint)

    // Student Header Card
    paint.color = Color.parseColor("#F1F5F9")
    val studentCard = RectF(40f, 145f, 555f, 215f)
    canvas.drawRoundRect(studentCard, 8f, 8f, paint)

    paint.color = Color.parseColor("#1E293B")
    paint.textSize = 12f
    paint.typeface = Typeface.DEFAULT
    canvas.drawText("Name: ${student.name}", 60f, 172f, paint)
    canvas.drawText("Father: ${student.fatherGuardianName}", 60f, 195f, paint)
    canvas.drawText("Class: ${student.className}", 320f, 172f, paint)
    canvas.drawText("Roll/ID: ${student.studentId}", 320f, 195f, paint)

    // Attendance
    paint.color = Color.parseColor("#059669")
    paint.textSize = 13f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("Attendance Record: $attendancePct% Monthly Attendance", 40f, 245f, paint)

    // Performance Rubrics
    paint.color = Color.parseColor("#1E293B")
    paint.textSize = 13f
    canvas.drawText("Performance Assessment (Ratings 1 to 5):", 40f, 280f, paint)

    var curY = 305f
    val rubrics = listOf(
      "Reading Skills" to (performance?.readingRating ?: 4),
      "Writing & Neatness" to (performance?.writingRating ?: 4),
      "Mathematics" to (performance?.mathRating ?: 4),
      "English" to (performance?.englishRating ?: 4),
      "Homework Regularity" to (performance?.homeworkRating ?: 4),
      "Class Participation" to (performance?.participationRating ?: 4),
      "Discipline & Behaviour" to (performance?.behaviourRating ?: 5)
    )

    paint.textSize = 11f
    paint.typeface = Typeface.DEFAULT
    for ((name, rating) in rubrics) {
      canvas.drawText(name, 60f, curY, paint)
      val stars = "★ ".repeat(rating) + "☆ ".repeat(5 - rating)
      paint.color = Color.parseColor("#D97706")
      canvas.drawText(stars, 320f, curY, paint)
      paint.color = Color.parseColor("#1E293B")
      curY += 22f
    }

    // Teacher remarks
    curY += 15f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("Teacher Remarks:", 40f, curY, paint)
    curY += 20f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
    paint.color = Color.parseColor("#334155")
    val remarks = performance?.teacherRemarks?.ifBlank { "Good progress observed in daily lectures." }
      ?: "Consistent performance and sincere effort."
    canvas.drawText(remarks, 40f, curY, paint)

    // Footer
    paint.color = Color.parseColor("#94A3B8")
    paint.textSize = 9f
    paint.typeface = Typeface.DEFAULT
    canvas.drawText("Generated by My Tuition Manager • Official Teacher Record", 160f, 810f, paint)

    document.finishPage(page)

    val dir = File(context.filesDir, "reports")
    if (!dir.exists()) dir.mkdirs()
    val file = File(dir, "Report_${student.name.replace(" ", "_")}.pdf")
    val fos = FileOutputStream(file)
    document.writeTo(fos)
    document.close()
    fos.close()

    return file
  }

  fun getFileUri(context: Context, file: File): android.net.Uri {
    return FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider",
      file
    )
  }
}
