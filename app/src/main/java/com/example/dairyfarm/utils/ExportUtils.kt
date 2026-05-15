package com.example.dairyfarm.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.example.dairyfarm.data.entities.ExpenseEntry
import com.example.dairyfarm.data.entities.IncomeEntry
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExportUtils {

    fun exportToPdfAndShare(
        context: Context,
        totalIncome: Double,
        totalExpense: Double,
        netProfit: Double,
        incomeList: List<IncomeEntry>,
        expenseList: List<ExpenseEntry>
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        
        var pageCount = 1
        var currentPage = document.startPage(pageInfo)
        var canvas = currentPage.canvas
        val paint = Paint()
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val tableDateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

        val primaryGreen = Color.rgb(46, 125, 50)
        val primaryRed = Color.rgb(198, 40, 40)
        val lightGray = Color.rgb(245, 245, 245)
        val textPrimary = Color.rgb(33, 33, 33)
        val textSecondary = Color.rgb(117, 117, 117)

        val margin = 45f
        var yPos = 0f

        fun drawHeader(pageNum: Int) {
            paint.textAlign = Paint.Align.LEFT
            paint.isFakeBoldText = false
            
            if (pageNum == 1) {
                // Page 1: Professional Green Header with Logo/Title
                paint.color = primaryGreen
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), 115f, paint)

                paint.color = Color.WHITE
                paint.textSize = 24f
                paint.isFakeBoldText = true
                canvas.drawText("KSHEERA SAGARA", margin, 60f, paint)

                paint.textSize = 12f
                paint.isFakeBoldText = false
                canvas.drawText("Dairy Farm Management - Financial Report", margin, 82f, paint)

                paint.textAlign = Paint.Align.RIGHT
                paint.textSize = 10f
                canvas.drawText("Page $pageNum", pageWidth - margin, 50f, paint)
                paint.textSize = 12f
                canvas.drawText("Report Date: ${dateFormat.format(Date())}", pageWidth - margin, 72f, paint)
            } else {
                // Page 2+: Minimalist Text Header (No Green Logo Box)
                paint.color = textSecondary
                paint.textSize = 10f
                paint.textAlign = Paint.Align.LEFT
                canvas.drawText("KSHEERA SAGARA - Financial Statement", margin, 40f, paint)
                
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText("Page $pageNum | Report Date: ${dateFormat.format(Date())}", pageWidth - margin, 40f, paint)
                
                // Thin separation line
                paint.color = Color.LTGRAY
                paint.strokeWidth = 1f
                canvas.drawLine(margin, 50f, pageWidth - margin, 50f, paint)
            }
            paint.textAlign = Paint.Align.LEFT
            paint.color = textPrimary
        }

        fun checkNewPage(neededSpace: Float) {
            // Check if we need to start a new page before drawing next content
            if (yPos + neededSpace > pageHeight - margin) {
                document.finishPage(currentPage)
                pageCount++
                currentPage = document.startPage(pageInfo)
                canvas = currentPage.canvas
                drawHeader(pageCount)
                // Set yPos below the minimalist header for page 2+
                yPos = 80f
            }
        }

        // --- Start Content Generation ---
        drawHeader(pageCount)
        yPos = 155f

        // 1. Summary Card (Only on Page 1)
        paint.color = lightGray
        canvas.drawRoundRect(margin, yPos, pageWidth - margin, yPos + 140f, 15f, 15f, paint)

        var cardY = yPos + 40f
        paint.color = textPrimary
        paint.textSize = 15f
        paint.isFakeBoldText = false
        canvas.drawText("Total Revenue", margin + 25f, cardY, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.color = primaryGreen
        paint.isFakeBoldText = true
        canvas.drawText("₹%.2f".format(totalIncome), pageWidth - margin - 25f, cardY, paint)

        cardY += 30f
        paint.textAlign = Paint.Align.LEFT
        paint.color = textPrimary
        paint.isFakeBoldText = false
        canvas.drawText("Total Expenses", margin + 25f, cardY, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.color = primaryRed
        paint.isFakeBoldText = true
        canvas.drawText("₹%.2f".format(totalExpense), pageWidth - margin - 25f, cardY, paint)

        cardY += 15f
        paint.color = Color.rgb(200, 200, 200)
        paint.strokeWidth = 1f
        canvas.drawLine(margin + 25f, cardY, pageWidth - margin - 25f, cardY, paint)

        cardY += 35f
        paint.textAlign = Paint.Align.LEFT
        paint.color = textPrimary
        paint.isFakeBoldText = true
        paint.textSize = 18f
        canvas.drawText("Net Profit", margin + 25f, cardY, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.color = if (netProfit >= 0) primaryGreen else primaryRed
        canvas.drawText("₹%.2f".format(netProfit), pageWidth - margin - 25f, cardY, paint)
        paint.textAlign = Paint.Align.LEFT

        yPos += 175f

        // 2. Strategic Advice (Only on Page 1)
        val isProfitable = netProfit >= 0
        val recTitle = if (isProfitable) "HEALTHY PERFORMANCE" else "LOSS DETECTED"
        val recText = if (isProfitable) {
            "Farm is financially healthy. Maintain current expense ratio and consider scaling production."
        } else {
            "Expenses exceed income. Audit fodder wastage and reduce non-essential operational costs."
        }
        val recColor = if (isProfitable) primaryGreen else primaryRed
        val recBg = if (isProfitable) Color.rgb(232, 245, 233) else Color.rgb(255, 235, 238)

        val internalPadding = 25f
        val wrapWidth = pageWidth - 2 * margin - 2 * internalPadding

        paint.textSize = 14f
        paint.isFakeBoldText = false
        val lines = mutableListOf<String>()
        val words = recText.split(" ")
        var currentLine = StringBuilder()
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= wrapWidth) {
                currentLine.append(if (currentLine.isEmpty()) "" else " ").append(word)
            } else {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            }
        }
        lines.add(currentLine.toString())

        val boxHeight = 45f + (lines.size * 22f) + 10f
        paint.color = recBg
        canvas.drawRoundRect(margin, yPos, pageWidth - margin, yPos + boxHeight, 12f, 12f, paint)

        paint.color = recColor
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("STRATEGIC ADVICE: $recTitle", margin + internalPadding, yPos + 32f, paint)

        paint.color = textPrimary
        paint.textSize = 14f
        paint.isFakeBoldText = false
        var lineY = yPos + 58f
        for (lineText in lines) {
            canvas.drawText(lineText, margin + internalPadding, lineY, paint)
            lineY += 22f
        }

        yPos += boxHeight + 45f

        // 3. Income Table (Can span multiple pages)
        if (incomeList.isNotEmpty()) {
            checkNewPage(100f)
            paint.color = primaryGreen
            paint.textSize = 17f
            paint.isFakeBoldText = true
            canvas.drawText("Income Transactions", margin, yPos, paint)
            yPos += 12f
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, paint)
            yPos += 28f

            paint.textSize = 12f
            paint.color = textSecondary
            canvas.drawText("DATE", margin, yPos, paint)
            canvas.drawText("QUANTITY", margin + 110f, yPos, paint)
            canvas.drawText("RATE", margin + 220f, yPos, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("AMOUNT (₹)", pageWidth - margin, yPos, paint)
            paint.textAlign = Paint.Align.LEFT
            yPos += 10f
            paint.color = Color.LTGRAY
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, paint)
            yPos += 25f

            paint.color = textPrimary
            paint.isFakeBoldText = false
            incomeList.sortedByDescending { it.date }.forEach { income ->
                checkNewPage(25f)
                canvas.drawText(tableDateFormat.format(Date(income.date)), margin, yPos, paint)
                canvas.drawText("%.1f L".format(income.liters), margin + 110f, yPos, paint)
                canvas.drawText("₹%.2f".format(income.pricePerLiter), margin + 220f, yPos, paint)
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText("%.2f".format(income.totalAmount), pageWidth - margin, yPos, paint)
                paint.textAlign = Paint.Align.LEFT
                yPos += 22f
            }
            yPos += 45f
        }

        // 4. Expense Table (Can span multiple pages)
        if (expenseList.isNotEmpty()) {
            checkNewPage(100f)
            paint.color = primaryRed
            paint.textSize = 17f
            paint.isFakeBoldText = true
            canvas.drawText("Expense Transactions", margin, yPos, paint)
            yPos += 12f
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, paint)
            yPos += 28f

            paint.textSize = 12f
            paint.color = textSecondary
            canvas.drawText("DATE", margin, yPos, paint)
            canvas.drawText("CATEGORY", margin + 110f, yPos, paint)
            canvas.drawText("NOTES", margin + 220f, yPos, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("AMOUNT (₹)", pageWidth - margin, yPos, paint)
            paint.textAlign = Paint.Align.LEFT
            yPos += 10f
            paint.color = Color.LTGRAY
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, paint)
            yPos += 25f

            paint.color = textPrimary
            paint.isFakeBoldText = false
            expenseList.sortedByDescending { it.date }.forEach { expense ->
                checkNewPage(25f)
                canvas.drawText(tableDateFormat.format(Date(expense.date)), margin, yPos, paint)
                canvas.drawText(expense.category, margin + 110f, yPos, paint)
                val note = if (expense.notes.length > 25) expense.notes.take(22) + "..." else expense.notes
                canvas.drawText(note, margin + 220f, yPos, paint)
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText("%.2f".format(expense.amount), pageWidth - margin, yPos, paint)
                paint.textAlign = Paint.Align.LEFT
                yPos += 22f
            }
        }

        document.finishPage(currentPage)

        try {
            val fileDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val fileName = "KsheeraSagara_Statement_${System.currentTimeMillis()}.pdf"
            val file = File(fileDir, fileName)
            document.writeTo(FileOutputStream(file))
            document.close()
            
            // Generate URI using FileProvider
            val uri: android.net.Uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            // Create Intent to share/view the PDF
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "Ksheera Sagara Financial Report")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            Toast.makeText(context, "Saved to Documents: $fileName", Toast.LENGTH_LONG).show()
            
            // Launch chooser
            context.startActivity(android.content.Intent.createChooser(intent, "Share or Preview Report"))
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to generate report", Toast.LENGTH_SHORT).show()
        }
    }
}
