package com.translabs.bloom.ui

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.translabs.bloom.audio.PitchTracker
import com.translabs.bloom.data.SessionStats
import com.translabs.bloom.data.SessionStore
import com.translabs.bloom.ui.theme.pitchY
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class PitchViewModel(app: Application) : AndroidViewModel(app) {
    private val tracker = PitchTracker()
    private val store = SessionStore(app)

    private val _pitch = MutableStateFlow<Float?>(null)
    val pitch: StateFlow<Float?> = _pitch
    val sessions: StateFlow<List<SessionStats>> = store.sessions

    private val _nudge = MutableStateFlow<String?>(null)
    val nudge: StateFlow<String?> = _nudge

    // 🌸 FIX: history now lives in the ViewModel, so your blooming line
    // survives hopping between tabs!
    private val _history = MutableStateFlow<List<Float>>(emptyList())
    val history: StateFlow<List<Float>> = _history

    private val recent = ArrayDeque<Float>()
    private val samples = mutableListOf<Float>()
    private var job: Job? = null
    private var sessionStartMs = 0L
    private var micGranted = false

    init { viewModelScope.launch { store.load() } }

    // 🌸 FIX: the single source of truth for "may we listen?" — called by the
    // activity on foreground/permission changes and by the practice screen
    // after the permission dialog. switching tabs never touches the mic!
    fun setMicGranted(granted: Boolean) {
        micGranted = granted
        if (granted) start() else stop()
    }

    fun start() {
        if (job != null || !micGranted) return
        samples.clear()
        sessionStartMs = System.currentTimeMillis()
        _nudge.value = null
        job = viewModelScope.launch {
            tracker.pitches().collect { s ->
                samples.add(s.hz)
                recent.addLast(s.hz)
                while (recent.size > 5) recent.removeFirst()
                _pitch.value = recent.sorted()[recent.size / 2]
                _history.value = (_history.value + s.hz).takeLast(160)
                if (_nudge.value == null &&
                    System.currentTimeMillis() - sessionStartMs > 10 * 60_000
                ) _nudge.value = "10 minutes already?! amazing — rest & sip water 🫖"
            }
        }
    }

    fun stop() {
        job?.cancel(); job = null
        _pitch.value = null
        recent.clear()
        if (samples.size >= 30) {
            val sorted = samples.sorted()
            fun q(f: Float) = sorted[(f * (sorted.size - 1)).toInt().coerceIn(0, sorted.size - 1)]
            val stats = SessionStats(
                endedAt = System.currentTimeMillis(),
                seconds = (samples.size * 512f / 16000f).roundToInt(),
                medianHz = q(0.5f), p10Hz = q(0.1f), p90Hz = q(0.9f),
                inTargetPercent = samples.count { it in 165f..220f } * 100 / samples.size,
            )
            viewModelScope.launch { store.add(stats) }
        }
        samples.clear()
    }
}

@Composable
fun LivePitchScreen(vm: PitchViewModel = viewModel()) {
    val context = LocalContext.current

    // 🌸 check (don't nag): refresh the mic state every time we come back
    var hasMic by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()) { hasMic = it }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasMic = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 🌸 FIX: tell the ViewModel about permission changes (starts/stops the mic),
    // but do NOT stop it when this screen leaves composition anymore!
    LaunchedEffect(hasMic) { vm.setMicGranted(hasMic) }

    if (!hasMic) {
        // friendly "we need the mic" card 🥺
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("🎤", style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(8.dp))
            Text("bloom needs your microphone to hear your beautiful voice",
                style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text("audio is analyzed live & never recorded — it stays on your phone 💗",
                style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { launcher.launch(Manifest.permission.RECORD_AUDIO) }) {
                Text("grant microphone")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = {
                context.startActivity(Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + context.packageName)))
            }) { Text("open app settings") }
        }
        return
    }

    val pitch by vm.pitch.collectAsState()
    val nudge by vm.nudge.collectAsState()
    val history by vm.history.collectAsState()

    val pink = MaterialTheme.colorScheme.primary
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = pitch?.let { "${it.roundToInt()} Hz" } ?: "-- Hz",
            style = MaterialTheme.typography.displayLarge
        )
        val p = pitch // local copy so smart-cast works — no !! needed 💗
        Text(
            text = when {
                p == null -> "say something~ 🎤"
                p < 165f -> "cozy & low 💙"
                p <= 220f -> "in the bloom zone 🌸"
                else -> "floating high ✨"
            },
            style = MaterialTheme.typography.titleMedium
        )
        nudge?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary)
        }
        Spacer(Modifier.height(16.dp))
        Canvas(
            Modifier.fillMaxWidth().height(280.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            val w = size.width; val h = size.height
            val top = pitchY(220f, h); val bottom = pitchY(165f, h)
            drawRoundRect(
                pink.copy(alpha = 0.2f), Offset(0f, top), Size(w, bottom - top),
                cornerRadius = CornerRadius(20f)
            )
            if (history.size > 1) {
                val step = w / 160f
                val path = Path()
                history.forEachIndexed { i, hz ->
                    val x = w - (history.size - 1 - i) * step
                    val y = pitchY(hz, h)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, pink,
                    style = Stroke(8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            pitch?.let { drawCircle(pink, 12f, Offset(w - 2f, pitchY(it, h))) }
        }
    }
}