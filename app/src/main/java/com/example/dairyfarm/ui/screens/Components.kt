package com.example.dairyfarm.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dairyfarm.ui.theme.*
import com.example.dairyfarm.ui.viewmodels.MonthlyTrendData

@Composable
fun IncomeExpenseCard(title: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceGray),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, fontSize = 14.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "₹${"%.2f".format(amount)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun PieChartNative(data: Map<String, Double>) {
    val total = data.values.sum()
    val colors = listOf(ChartFodder, ChartMedical, ChartLabor, ChartOther)

    Row(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(150.dp).padding(16.dp)) {
            var startAngle = 0f
            data.values.forEachIndexed { index, value ->
                val sweepAngle = (value / total).toFloat() * 360f
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )
                startAngle += sweepAngle
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            data.keys.forEachIndexed { index, category ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Box(modifier = Modifier.size(12.dp).background(colors[index % colors.size], CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = category, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun MonthlyTrendChart(trendData: List<MonthlyTrendData>) {

    // ── Empty state ──────────────────────────────────────────────────────────
    if (trendData.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("📊", fontSize = 36.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add income & expenses to\nsee your monthly trends",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    // ── Scale helpers ─────────────────────────────────────────────────────────
    val maxVal = trendData.maxOf { maxOf(it.income, it.expense, it.profit.coerceAtLeast(0.0)) }
        .coerceAtLeast(500.0)
    val minVal = trendData.minOf { it.profit }.coerceAtMost(0.0)
    val range  = (maxVal - minVal).coerceAtLeast(1.0)

    fun valueToY(value: Double, chartH: Float): Float =
        chartH - ((value - minVal) / range * chartH).toFloat()

    fun formatK(v: Double): String = when {
        v >= 1_000 -> "₹${"%.1f".format(v / 1000)}k"
        else       -> "₹${v.toInt()}"
    }

    Column(modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 8.dp)) {

        // ── Legend ────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            ChartLegendItem("Income",  PrimaryGreen)
            Spacer(modifier = Modifier.width(16.dp))
            ChartLegendItem("Expense", PrimaryRed)
            Spacer(modifier = Modifier.width(16.dp))
            ChartLegendItem("Profit",  ProfitGreen)
        }

        // ── Canvas ────────────────────────────────────────────────────────────
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            val W           = size.width
            val H           = size.height
            val padLeft     = 56f   // Y-axis label space
            val padBottom   = 24f   // X-axis label space
            val padTop      = 28f   // room for value labels above top points
            val chartW      = W - padLeft
            val chartH      = H - padBottom - padTop
            val chartTop    = padTop
            val chartBottom = padTop + chartH

            val labelPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize    = 28f
                color       = android.graphics.Color.argb(180, 80, 80, 80)
            }
            val valuePaint = android.graphics.Paint().apply {
                isAntiAlias  = true
                textSize     = 26f
                isFakeBoldText = true
            }

            // ── Grid lines (4 horizontal) ─────────────────────────────────────
            val gridSteps = 4
            for (i in 0..gridSteps) {
                val gy = chartTop + chartH * i / gridSteps
                drawLine(
                    color       = Color.LightGray.copy(alpha = 0.6f),
                    start       = Offset(padLeft, gy),
                    end         = Offset(W, gy),
                    strokeWidth = 1.5f
                )
                // Y-axis value label
                val gridValue = maxVal - (maxVal - minVal) * i / gridSteps
                val label     = formatK(gridValue)
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    0f,
                    gy + labelPaint.textSize / 3,
                    labelPaint
                )
            }

            // ── Axes ──────────────────────────────────────────────────────────
            // X-axis
            drawLine(Color(0xFF9E9E9E), Offset(padLeft, chartBottom), Offset(W, chartBottom), strokeWidth = 2f)
            // Y-axis
            drawLine(Color(0xFF9E9E9E), Offset(padLeft, chartTop), Offset(padLeft, chartBottom), strokeWidth = 2f)

            // Zero line (when profit can be negative)
            if (minVal < 0) {
                val zy = valueToY(0.0, chartH) + padTop
                drawLine(
                    color       = Color(0xFFBDBDBD),
                    start       = Offset(padLeft, zy),
                    end         = Offset(W, zy),
                    strokeWidth = 2f
                )
            }

            // ══ SINGLE DATA POINT → Grouped bar chart ═════════════════════════
            if (trendData.size == 1) {
                val data     = trendData[0]
                val cx       = padLeft + chartW / 2f
                val barW     = chartW / 6f
                val gap      = barW * 0.4f

                // Income bar
                val incH  = ((data.income - minVal) / range * chartH).toFloat()
                val incTop = chartBottom - incH
                drawRect(PrimaryGreen, topLeft = Offset(cx - barW - gap, incTop), size = Size(barW, incH))

                // Expense bar
                val expH  = ((data.expense - minVal) / range * chartH).toFloat()
                val expTop = chartBottom - expH
                drawRect(PrimaryRed, topLeft = Offset(cx + gap, expTop), size = Size(barW, expH))

                // Profit bar (may be negative)
                val profRaw = data.profit
                if (profRaw >= 0) {
                    val profH   = ((profRaw - minVal.coerceAtMost(0.0)) / range * chartH).toFloat()
                    val profTop = chartBottom - ((profRaw - minVal) / range * chartH).toFloat()
                    drawRect(ProfitGreen.copy(alpha = 0.7f), topLeft = Offset(cx - barW / 2f, profTop), size = Size(barW, profH))
                }

                // Value labels above bars
                valuePaint.color = android.graphics.Color.argb(220, 46, 125, 50)
                drawContext.canvas.nativeCanvas.drawText(
                    formatK(data.income),
                    cx - barW - gap,
                    incTop - 6f,
                    valuePaint
                )
                valuePaint.color = android.graphics.Color.argb(220, 198, 40, 40)
                drawContext.canvas.nativeCanvas.drawText(
                    formatK(data.expense),
                    cx + gap,
                    expTop - 6f,
                    valuePaint
                )

            } else {
                // ══ MULTI-POINT → Line chart ═══════════════════════════════════
                val n       = trendData.size
                val spacing = chartW / (n - 1).toFloat()

                val incomePath  = Path()
                val expensePath = Path()
                val profitPath  = Path()

                trendData.forEachIndexed { index, data ->
                    val x    = padLeft + spacing * index
                    val yInc = valueToY(data.income,  chartH) + padTop
                    val yExp = valueToY(data.expense, chartH) + padTop
                    val yPro = valueToY(data.profit,  chartH) + padTop

                    if (index == 0) {
                        incomePath.moveTo(x, yInc);  expensePath.moveTo(x, yExp);  profitPath.moveTo(x, yPro)
                    } else {
                        incomePath.lineTo(x, yInc);  expensePath.lineTo(x, yExp);  profitPath.lineTo(x, yPro)
                    }

                    // ── Dots: white border + colored fill ──────────────────────
                    drawCircle(Color.White,   9.dp.toPx(), Offset(x, yInc))
                    drawCircle(PrimaryGreen,  6.dp.toPx(), Offset(x, yInc))
                    drawCircle(Color.White,   9.dp.toPx(), Offset(x, yExp))
                    drawCircle(PrimaryRed,    6.dp.toPx(), Offset(x, yExp))
                    drawCircle(Color.White,   7.dp.toPx(), Offset(x, yPro))
                    drawCircle(ProfitGreen,   5.dp.toPx(), Offset(x, yPro))

                    // ── Value labels above income dot (top series only) ────────
                    valuePaint.color = android.graphics.Color.argb(200, 46, 125, 50)
                    drawContext.canvas.nativeCanvas.drawText(
                        formatK(data.income),
                        x - 20f,
                        yInc - 14f,
                        valuePaint
                    )
                }

                val lineStroke = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                drawPath(incomePath,  PrimaryGreen, style = lineStroke)
                drawPath(expensePath, PrimaryRed,   style = lineStroke)
                drawPath(profitPath,  ProfitGreen,  style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }

        // ── Month labels below chart ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 48.dp, top = 2.dp),
            horizontalArrangement = if (trendData.size == 1) Arrangement.Center else Arrangement.SpaceBetween
        ) {
            trendData.forEach {
                Text(
                    text       = it.month,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextSecondary
                )
            }
        }
    }
}

@Composable
fun ChartLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
    }
}
