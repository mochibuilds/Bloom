package com.translabs.bloom.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.translabs.bloom.data.SessionStats
import com.translabs.bloom.ui.theme.pitchY
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun ProgressScreen(sessions: List<SessionStats>) {
    val days = sessions.map {
        Instant.ofEpochMilli(it.endedAt).atZone(ZoneId.systemDefault()).toLocalDate()
    }.toSet()
    var d = LocalDate.now()
    if (d !in days) d = d.minusDays(1)
    var streak = 0
    while (d in days) { streak++; d = d.minusDays(1) }

    // 🌸 FIX: Extract colors outside of the Canvas block!
    // Canvas runs in a DrawScope (not a @Composable scope), so we can't call MaterialTheme inside it.
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLow

    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("your beautiful progress 📈", style = MaterialTheme.typography.headlineMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("sessions", "${sessions.size}", Modifier.weight(1f))
            StatCard("voiced min", "${sessions.sumOf { it.seconds } / 60}", Modifier.weight(1f))
            StatCard("in bloom 🌸", "${sessions.lastOrNull()?.inTargetPercent ?: 0}%", Modifier.weight(1f))
        }
        if (streak > 0) {
            Text("🔥 $streak-day active streak — keep it up! 💗",
                style = MaterialTheme.typography.titleMedium)
        }
        val recent = sessions.takeLast(20)
        if (recent.isEmpty()) {
            Text("no sessions yet — go say hi to the mic 🎤",
                style = MaterialTheme.typography.bodyLarge)
        } else {
            Canvas(
                Modifier.fillMaxWidth().height(260.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(surfaceColor) // Using extracted color
            ) {
                val w = size.width; val h = size.height
                val top = pitchY(220f, h); val bottom = pitchY(165f, h)
                drawRoundRect(
                    primaryColor.copy(alpha = 0.15f), // Using extracted color
                    Offset(0f, top), Size(w, bottom - top), CornerRadius(20f)
                )
                val step = if (recent.size > 1) w / (recent.size - 1) else w

                if (recent.size > 1) {
                    val path = Path()
                    recent.forEachIndexed { i, s ->
                        val p = Offset(i * step, pitchY(s.medianHz, h))
                        if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
                    }
                    drawPath(path, primaryColor, // Using extracted color
                        style = Stroke(6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
                recent.forEachIndexed { i, s ->
                    val x = if (recent.size == 1) w / 2f else i * step
                    drawCircle(primaryColor, 8f, // Using extracted color
                        Offset(x, pitchY(s.medianHz, h)))
                }
            }
            Text("median pitch of your last ${recent.size} sessions — dots in the pink band = blooming 🌸",
                style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(14.dp)
    ) {
        Text(value, style = MaterialTheme.typography.headlineSmall)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}