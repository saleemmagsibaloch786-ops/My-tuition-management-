package com.example.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.net.URLEncoder

object WhatsAppHelper {

  fun formatPhoneNumber(rawNumber: String): String {
    var cleaned = rawNumber.replace(" ", "").replace("-", "").replace("(", "").replace(")", "").trim()
    if (cleaned.startsWith("03")) {
      cleaned = "+92" + cleaned.substring(1)
    } else if (cleaned.startsWith("3") && cleaned.length == 10) {
      cleaned = "+92$cleaned"
    }
    // Remove plus for api.whatsapp.com deep links if needed, but whatsapp accepts clean digits
    return cleaned.replace("+", "")
  }

  fun sendTextMessage(context: Context, rawPhone: String, message: String) {
    val phone = formatPhoneNumber(rawPhone)
    try {
      val encodedMsg = URLEncoder.encode(message, "UTF-8")
      val uriString = if (phone.isNotBlank()) {
        "https://api.whatsapp.com/send?phone=$phone&text=$encodedMsg"
      } else {
        "https://api.whatsapp.com/send?text=$encodedMsg"
      }
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
      intent.setPackage("com.whatsapp")
      try {
        context.startActivity(intent)
      } catch (e: Exception) {
        // Fallback without package specification (WhatsApp Business or web browser)
        val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
        context.startActivity(fallbackIntent)
      }
    } catch (e: Exception) {
      Toast.makeText(context, "Could not open WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
    }
  }

  fun sharePdf(context: Context, file: File, caption: String = "") {
    try {
      val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
      )
      val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        if (caption.isNotBlank()) {
          putExtra(Intent.EXTRA_TEXT, caption)
        }
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      context.startActivity(Intent.createChooser(intent, "Share Document via WhatsApp or Drive"))
    } catch (e: Exception) {
      Toast.makeText(context, "Failed to share file: ${e.message}", Toast.LENGTH_SHORT).show()
    }
  }
}
