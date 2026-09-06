package com.translabs.bloom.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.translabs.bloom.MainActivity

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(NotificationChannel(
                "practice", "Practice reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "gentle daily nudge to practice 🌸" })
        }
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        nm.notify(1, NotificationCompat.Builder(context, "practice")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("bloom time 🌸")
            .setContentText("five gentle minutes for your beautiful voice?")
            .setContentIntent(open)
            .setAutoCancel(true)
            .build())
        ReminderScheduler.scheduleNextDay(context) // tomorrow, same time 💗
    }
}