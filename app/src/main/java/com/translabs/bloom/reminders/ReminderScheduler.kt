package com.translabs.bloom.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

/** a daily alarm that reschedules itself — fires even when the app is closed 💗 */
object ReminderScheduler {

    fun schedule(context: Context, hour: Int, minute: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = nextTriggerMillis(hour, minute)

        when {
            // Android 12+: exact alarms need permission — check first! 🚦
            Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms() ->
                // graceful fallback: inexact, but still wakes the device during doze
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent(context))
            Build.VERSION.SDK_INT >= 23 ->
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent(context))
            else ->
                am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent(context))
        }
    }

    fun cancel(context: Context) {
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
            .cancel(pendingIntent(context))
    }

    /** reads saved settings; called at boot and after each reminder fires */
    fun scheduleNextDay(context: Context) {
        val prefs = context.getSharedPreferences("bloom", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("reminderEnabled", true)) return
        schedule(context, prefs.getInt("hour", 17), prefs.getInt("minute", 0))
    }

    fun canScheduleExact(context: Context): Boolean {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()
    }

    private fun pendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context, 42,
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun nextTriggerMillis(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis
    }
}