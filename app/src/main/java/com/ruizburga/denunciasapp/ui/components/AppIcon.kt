package com.ruizburga.denunciasapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

enum class AppIconName { Home, List, Add, Document, Search, Calendar, Location, Person, Check, ArrowRight, ChevronDown }

@Composable
fun AppIcon(
    name: AppIconName,
    modifier: Modifier = Modifier,
    tint: Color,
    contentDescription: String? = null
) {
    Canvas(
        modifier.semantics {
            if (contentDescription != null) this.contentDescription = contentDescription
        }
    ) {
        val unit = size.minDimension / 24f
        withTransform({ scale(unit, unit, pivot = Offset.Zero) }) {
            val stroke = Stroke(width = 1.8f, cap = StrokeCap.Round)
            fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
                drawLine(tint, Offset(x1, y1), Offset(x2, y2), stroke.width, cap = StrokeCap.Round)
            }
            when (name) {
                AppIconName.Home -> {
                    val path = Path().apply {
                        moveTo(3.5f, 10.5f); lineTo(12f, 3.5f); lineTo(20.5f, 10.5f)
                        moveTo(5.5f, 9f); lineTo(5.5f, 20f); lineTo(18.5f, 20f); lineTo(18.5f, 9f)
                        moveTo(9.5f, 20f); lineTo(9.5f, 14f); lineTo(14.5f, 14f); lineTo(14.5f, 20f)
                    }
                    drawPath(path, tint, style = stroke)
                }
                AppIconName.List -> {
                    listOf(6f, 12f, 18f).forEach { y ->
                        drawCircle(tint, 1.1f, Offset(4f, y))
                        line(9f, y, 20f, y)
                    }
                }
                AppIconName.Add -> {
                    line(12f, 5f, 12f, 19f)
                    line(5f, 12f, 19f, 12f)
                }
                AppIconName.Document -> {
                    val path = Path().apply {
                        moveTo(7f, 3.5f); lineTo(15f, 3.5f); lineTo(20f, 8.5f)
                        lineTo(20f, 20.5f); lineTo(7f, 20.5f); close()
                        moveTo(15f, 3.5f); lineTo(15f, 8.5f); lineTo(20f, 8.5f)
                        moveTo(10f, 13f); lineTo(17f, 13f)
                        moveTo(10f, 16.5f); lineTo(17f, 16.5f)
                    }
                    drawPath(path, tint, style = stroke)
                }
                AppIconName.Search -> {
                    drawCircle(tint, 6.3f, Offset(10.3f, 10.3f), style = stroke)
                    line(15f, 15f, 20.5f, 20.5f)
                }
                AppIconName.Calendar -> {
                    drawRoundRect(tint, Offset(3.5f, 5.5f), androidx.compose.ui.geometry.Size(17f, 15f), androidx.compose.ui.geometry.CornerRadius(2.5f), style = stroke)
                    line(3.8f, 9.5f, 20.2f, 9.5f)
                    line(8f, 3.5f, 8f, 7f)
                    line(16f, 3.5f, 16f, 7f)
                }
                AppIconName.Location -> {
                    val path = Path().apply {
                        moveTo(12f, 21f); cubicTo(10f, 18f, 5f, 13.5f, 5f, 9.8f)
                        cubicTo(5f, 6f, 8f, 3.5f, 12f, 3.5f); cubicTo(16f, 3.5f, 19f, 6f, 19f, 9.8f)
                        cubicTo(19f, 13.5f, 14f, 18f, 12f, 21f); close()
                    }
                    drawPath(path, tint, style = stroke)
                    drawCircle(tint, 2f, Offset(12f, 9.5f), style = stroke)
                }
                AppIconName.Person -> {
                    drawCircle(tint, 3f, Offset(12f, 7.5f), style = stroke)
                    val path = Path().apply {
                        moveTo(5f, 20f); cubicTo(5f, 16f, 8f, 14f, 12f, 14f)
                        cubicTo(16f, 14f, 19f, 16f, 19f, 20f)
                    }
                    drawPath(path, tint, style = stroke)
                }
                AppIconName.Check -> {
                    val path = Path().apply { moveTo(4.5f, 12.5f); lineTo(9.5f, 17.5f); lineTo(19.5f, 6.5f) }
                    drawPath(path, tint, style = stroke)
                }
                AppIconName.ArrowRight -> {
                    line(4f, 12f, 19f, 12f)
                    line(13f, 6f, 19f, 12f)
                    line(13f, 18f, 19f, 12f)
                }
                AppIconName.ChevronDown -> {
                    line(6f, 9f, 12f, 15f)
                    line(12f, 15f, 18f, 9f)
                }
            }
        }
    }
}
