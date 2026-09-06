package com.translabs.bloom.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.translabs.bloom.reminders.ReminderScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareScreen() {
    val ctx = LocalContext.current
    val prefs = ctx.getSharedPreferences("bloom", Context.MODE_PRIVATE)
    var enabled by remember { mutableStateOf(prefs.getBoolean("reminderEnabled", true)) }
    var canExact by remember { mutableStateOf(ReminderScheduler.canScheduleExact(ctx)) }
    val time = rememberTimePickerState(
        initialHour = prefs.getInt("hour", 17),
        initialMinute = prefs.getInt("minute", 0),
        is24Hour = false
    )

    // re-check the exact-alarm permission when coming back from settings
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                canExact = ReminderScheduler.canScheduleExact(ctx)
                if (enabled) ReminderScheduler.schedule(ctx, time.hour, time.minute)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("self-care 💗", style = MaterialTheme.typography.headlineMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("daily reminder", Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium)
            Switch(checked = enabled, onCheckedChange = {
                enabled = it
                prefs.edit().putBoolean("reminderEnabled", it).apply()
                if (it) ReminderScheduler.schedule(ctx, time.hour, time.minute)
                else ReminderScheduler.cancel(ctx)
            })
        }
        if (enabled && !canExact) {
            Text("exact alarms are off — reminders might arrive a little late 🥺",
                style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = {
                ctx.startActivity(Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:" + ctx.packageName)))
            }) { Text("allow exact alarms ⏰") }
        }
        TimePicker(state = time)
        Button(onClick = {
            prefs.edit().putInt("hour", time.hour).putInt("minute", time.minute).apply()
            if (enabled) ReminderScheduler.schedule(ctx, time.hour, time.minute)
        }) { Text("save reminder time ⏰") }
        Text("voice health rules 🫖", style = MaterialTheme.typography.titleLarge)
        Text(
            "• practice should never hurt — scratchy or painful = stop & rest\n" +
                    "• short & daily beats long & rare\n" +
                    "• sip water, warm tea is a treat\n" +
                    "• rest days count too 💗",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}