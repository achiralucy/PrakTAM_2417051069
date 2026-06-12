package com.example.praktam_2417051069.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.praktam_2417051069.R

class DeadlineReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val judulTugas = intent.getStringExtra("judul") ?: return
        val deadline = intent.getStringExtra("deadline") ?: return
        val id = intent.getIntExtra("notif_id", 0)

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏰ Pengingat Deadline!")
            .setContentText("\"$judulTugas\" deadline hari ini: $deadline")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Jangan lupa! Tugas \"$judulTugas\" harus selesai hari ini ($deadline).")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}