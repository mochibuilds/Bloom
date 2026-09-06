package com.translabs.bloom

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.translabs.bloom.reminders.ReminderScheduler
import com.translabs.bloom.ui.CareScreen
import com.translabs.bloom.ui.CreditsScreen
import com.translabs.bloom.ui.GuideScreen
import com.translabs.bloom.ui.LivePitchScreen
import com.translabs.bloom.ui.PitchViewModel
import com.translabs.bloom.ui.ProgressScreen
import com.translabs.bloom.ui.StoryScreen
import com.translabs.bloom.ui.theme.BloomTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ReminderScheduler.scheduleNextDay(this)
        setContent {
            BloomTheme {
                val context = LocalContext.current

                // 🌸 FIRST BOOT: ask for everything we need in one gentle go,
                // then remember we asked so we never nag again
                val multiLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()) { }
                LaunchedEffect(Unit) {
                    val prefs = context.getSharedPreferences("bloom", Context.MODE_PRIVATE)
                    if (!prefs.getBoolean("permissionsAsked", false)) {
                        prefs.edit().putBoolean("permissionsAsked", true).apply()
                        val wanted = mutableListOf(Manifest.permission.RECORD_AUDIO)
                        if (Build.VERSION.SDK_INT >= 33)
                            wanted += Manifest.permission.POST_NOTIFICATIONS
                        multiLauncher.launch(wanted.toTypedArray())
                    }
                }

                var tab by remember { mutableIntStateOf(0) }
                val vm: PitchViewModel = viewModel()

                // 🌸 FIX: the mic now lives at the ACTIVITY level instead of the
                // practice screen — hopping over to stories/guide/care no longer
                // stops the listening! we only pause when the whole app hides 💗
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> {
                                val granted = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                vm.setMicGranted(granted)
                            }
                            Lifecycle.Event.ON_STOP -> vm.stop()
                            else -> {}
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                Scaffold(bottomBar = {
                    // 6 tabs now → labels only on selection to keep it tidy 💅
                    NavigationBar {
                        NavigationBarItem(tab == 0, { tab = 0 },
                            icon = { Text("🎤") }, label = { Text("practice") },
                            alwaysShowLabel = false)
                        NavigationBarItem(tab == 1, { tab = 1 },
                            icon = { Text("📈") }, label = { Text("progress") },
                            alwaysShowLabel = false)
                        NavigationBarItem(tab == 2, { tab = 2 },
                            icon = { Text("📖") }, label = { Text("stories") },
                            alwaysShowLabel = false)
                        NavigationBarItem(tab == 3, { tab = 3 },
                            icon = { Text("🎓") }, label = { Text("guide") },
                            alwaysShowLabel = false)
                        NavigationBarItem(tab == 4, { tab = 4 },
                            icon = { Text("💗") }, label = { Text("care") },
                            alwaysShowLabel = false)
                        NavigationBarItem(tab == 5, { tab = 5 },
                            icon = { Text("💐") }, label = { Text("thanks") },
                            alwaysShowLabel = false)
                    }
                }) { padding ->
                    Box(Modifier.padding(padding)) {
                        when (tab) {
                            0 -> LivePitchScreen(vm)
                            1 -> ProgressScreen(vm.sessions.collectAsState().value)
                            2 -> StoryScreen()
                            3 -> GuideScreen()
                            4 -> CareScreen()
                            else -> CreditsScreen()
                        }
                    }
                }
            }
        }
    }
}