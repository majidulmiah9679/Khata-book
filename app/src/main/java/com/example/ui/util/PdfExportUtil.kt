package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.ui.model.CurrencyFormatter
import com.example.ui.model.KhataDateUtils
import com.example.ui.model.WorkerMonthlySummary
import java.io.File
import java.io.FileOutputStream

object PdfExportUtil {

    fun generateAndShareMonthlyReport(
        context: Context,
        monthName: String,
        year: Int,
        workerSummaries: List<WorkerMonthlySummary>,
        totalEarned: Double,
        totalTaken: Double,
        balanceDue: Double
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 standard width in points
            val pageHeight = 842 // A4 standard height in points
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            // Paints
            val textPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 10f
                isAntiAlias = true
            }

            val boldPaint = Paint().apply {
                color = Color.BLACK
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val titlePaint = Paint().apply {
                color = Color.rgb(180, 83, 9) // Indian Khata Red/Amber
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.rgb(75, 85, 99)
                textSize = 11f
                isAntiAlias = true
            }

            val headerBgPaint = Paint().apply {
                color = Color.rgb(254, 243, 199) // Light ledger parchment amber
                style = Paint.Style.FILL
            }

            val rowAltBgPaint = Paint().apply {
                color = Color.rgb(249, 250, 251)
                style = Paint.Style.FILL
            }

            val borderPaint = Paint().apply {
                color = Color.rgb(209, 213, 219)
                strokeWidth = 1f
                style = Paint.Style.STROKE
            }

            var currentY = 36f

            // 1. Header Section (Ledger Banner)
            canvas.drawText("LABOR HAJIRA KHATA", 24f, currentY, titlePaint)
            currentY += 16f
            canvas.drawText("Official Worker Attendance & Payment Ledger Summary", 24f, currentY, subtitlePaint)
            currentY += 14f
            canvas.drawText("Month / Period: $monthName $year   |   Generated: ${KhataDateUtils.formatDisplayDate(KhataDateUtils.getTodayIso())}", 24f, currentY, textPaint)
            currentY += 16f

            // Top Summary Box (Balance / Taka Taken / Hajira total)
            val boxPaint = Paint().apply {
                color = Color.rgb(255, 251, 235)
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(24f, currentY, (pageWidth - 24).toFloat(), currentY + 46f, 6f, 6f, boxPaint)
            canvas.drawRoundRect(24f, currentY, (pageWidth - 24).toFloat(), currentY + 46f, 6f, 6f, borderPaint)

            boldPaint.textSize = 9f
            textPaint.textSize = 9f
            canvas.drawText("TOTAL WORKERS", 36f, currentY + 18f, textPaint)
            boldPaint.color = Color.rgb(31, 41, 55)
            canvas.drawText("${workerSummaries.size} Persons", 36f, currentY + 34f, boldPaint)

            canvas.drawText("TOTAL EARNINGS", 150f, currentY + 18f, textPaint)
            boldPaint.color = Color.rgb(22, 101, 52) // Green
            canvas.drawText(CurrencyFormatter.formatTaka(totalEarned), 150f, currentY + 34f, boldPaint)

            canvas.drawText("TAKA TAKEN (ADV)", 300f, currentY + 18f, textPaint)
            boldPaint.color = Color.rgb(180, 83, 9) // Amber/Brown
            canvas.drawText(CurrencyFormatter.formatTaka(totalTaken), 300f, currentY + 34f, boldPaint)

            canvas.drawText("NET BALANCE DUE", 440f, currentY + 18f, textPaint)
            boldPaint.color = if (balanceDue >= 0) Color.rgb(180, 83, 9) else Color.RED
            canvas.drawText(CurrencyFormatter.formatTaka(balanceDue), 440f, currentY + 34f, boldPaint)

            currentY += 60f

            // 2. Table Headers
            val colWorker = 28f
            val colRate = 180f
            val colHajira = 240f
            val colOt = 295f
            val colEarned = 345f
            val colTaken = 425f
            val colDue = 500f

            val rowHeight = 22f

            // Header row background
            canvas.drawRect(24f, currentY, (pageWidth - 24).toFloat(), currentY + rowHeight, headerBgPaint)
            canvas.drawRect(24f, currentY, (pageWidth - 24).toFloat(), currentY + rowHeight, borderPaint)

            boldPaint.color = Color.rgb(67, 56, 202)
            boldPaint.textSize = 8.5f
            val headerTextY = currentY + 14f
            canvas.drawText("WORKER NAME", colWorker, headerTextY, boldPaint)
            canvas.drawText("RATE (TK)", colRate, headerTextY, boldPaint)
            canvas.drawText("HAJIRA", colHajira, headerTextY, boldPaint)
            canvas.drawText("OT (HRS)", colOt, headerTextY, boldPaint)
            canvas.drawText("TOTAL TK", colEarned, headerTextY, boldPaint)
            canvas.drawText("TAKEN TK", colTaken, headerTextY, boldPaint)
            canvas.drawText("DUE TK", colDue, headerTextY, boldPaint)

            currentY += rowHeight

            // Table Rows
            textPaint.textSize = 8f
            boldPaint.textSize = 8f

            workerSummaries.forEachIndexed { index, item ->
                if (currentY + rowHeight > pageHeight - 36) {
                    return@forEachIndexed // Fits 20-25 workers comfortably on single page
                }

                if (index % 2 == 1) {
                    canvas.drawRect(24f, currentY, (pageWidth - 24).toFloat(), currentY + rowHeight, rowAltBgPaint)
                }
                canvas.drawRect(24f, currentY, (pageWidth - 24).toFloat(), currentY + rowHeight, borderPaint)

                val textY = currentY + 14f
                boldPaint.color = Color.BLACK
                val nameDisplay = if (item.worker.name.length > 22) item.worker.name.take(20) + ".." else item.worker.name
                canvas.drawText(nameDisplay, colWorker, textY, boldPaint)
                canvas.drawText(item.worker.dailyWage.toInt().toString(), colRate, textY, textPaint)
                canvas.drawText(CurrencyFormatter.formatDays(item.totalHajira), colHajira, textY, textPaint)
                canvas.drawText("${item.totalOvertimeHours}h", colOt, textY, textPaint)
                canvas.drawText(CurrencyFormatter.formatTaka(item.totalEarned), colEarned, textY, textPaint)
                canvas.drawText(CurrencyFormatter.formatTaka(item.totalMoneyTaken), colTaken, textY, textPaint)

                boldPaint.color = if (item.balanceDue >= 0) Color.rgb(180, 83, 9) else Color.RED
                canvas.drawText(CurrencyFormatter.formatTaka(item.balanceDue), colDue, textY, boldPaint)

                currentY += rowHeight
            }

            // Footer note
            currentY += 20f
            textPaint.color = Color.GRAY
            textPaint.textSize = 7.5f
            canvas.drawText("* Overtime calculated at (Daily Wage / 8 hours) per hour. Verified by Labor Hajira Khata Ledger.", 24f, currentY, textPaint)

            pdfDocument.finishPage(page)

            // Save to cache dir
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()

            val fileName = "Hajira_Ledger_${monthName}_$year.pdf"
            val file = File(reportsDir, fileName)
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            fos.close()
            pdfDocument.close()

            // Share Intent
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Labor Hajira Khata - $monthName $year Ledger")
                putExtra(Intent.EXTRA_TEXT, "Attached is the monthly attendance and payment report for $monthName $year.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Hajira PDF via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error generating PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
