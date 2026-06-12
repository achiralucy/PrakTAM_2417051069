package com.example.praktam_2417051069.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.praktam_2417051069.R
import java.text.SimpleDateFormat
import java.util.*

object NotificationHelper {

    const val CHANNEL_ID = "deadline_reminder"
    private const val CHANNEL_NAME = "Pengingat Deadline"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pengingat deadline tugas"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showDeadlineReminder(context: Context, judulTugas: String, deadline: String, id: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Pengingat Deadline!")
            .setContentText("$judulTugas - Deadline: $deadline")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun scheduleDeadlineAlarm(context: Context, judulTugas: String, deadline: String, id: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Parse deadline format dd/MM/yyyy
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val deadlineDate = try {
            sdf.parse(deadline) ?: return
        } catch (e: Exception) {
            return
        }


        val calendar = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 1)
        }


        if (calendar.timeInMillis < System.currentTimeMillis()) return

        val intent = Intent(context, DeadlineReceiver::class.java).apply {
            putExtra("judul", judulTugas)
            putExtra("deadline", deadline)
            putExtra("notif_id", id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelDeadlineAlarm(context: Context, id: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DeadlineReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}