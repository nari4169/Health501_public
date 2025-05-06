package com.billcoreatech.health501.presentaion

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.billcoreatech.health501.R

@Composable
fun StepIndicator(
    steps: Int,
    startTime: Long,
    endTime: Long,
    goal: Int = 10000,
    modifier: Modifier = Modifier
) {
    val progress = remember(steps, goal) { steps.coerceAtMost(goal).div(goal.toFloat()) }
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())

    Box(
        modifier = modifier.size(250.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 20.dp.toPx()
            val size = size.minDimension
            val radius = size / 2

            // 배경 원
            drawArc(
                color = Color.LightGray.copy(alpha = 0.3f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )

            // 진행률 원
            drawArc(
                color = Color(0xFF5E82FC),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
        }

        // 텍스트 중앙 표시
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "%,d".format(steps),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontFamily = FontFamily(Font(R.font.notosanskr_bold))
            )
            Text(
                text = "걸음",
                fontSize = 18.sp,
                color = Color.Gray,
                fontFamily = FontFamily(Font(R.font.notosanskr_regular))
            )
            Text( text = sdf.format(startTime),
                fontSize = 12.sp,
                color = Color.Gray,
                fontFamily = FontFamily(Font(R.font.notosanskr_regular))
            )
            Text( text = sdf.format(endTime),
                fontSize = 12.sp,
                color = Color.Gray,
                fontFamily = FontFamily(Font(R.font.notosanskr_regular))
            )
        }
    }
}
