package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.data.repository.GameRepository
import com.example.R
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val repository = GameRepository(applicationContext)
        val progress = repository.progressFlow.firstOrNull() ?: return Result.success()

        val type = inputData.getString("type") ?: return Result.failure()

        when (type) {
            "hearts_full" -> {
                if (progress.hearts >= 5) {
                    showNotification(
                        "Canların Doldu!",
                        "Maceraya kaldığın yerden devam et!",
                        101
                    )
                    repository.addNotification("Bildirim", "Canların doldu bildirimi gönderildi.")
                }
            }
            "streak_reminder" -> {
                // Check if user active today
                val today = Calendar.getInstance()
                // Simple logic: if last active was yesterday, show reminder
                showNotification(
                    "Serini Koru!",
                    "${progress.streak} günlük serini bozmamak için bir bulmaca çöz!",
                    102
                )
                repository.addNotification("Bildirim", "Seri hatırlatma bildirimi gönderildi.")
            }
            "daily_reward" -> {
                if (progress.lastDailyRewardClaimDate != repository.getTodayDateString()) {
                    showNotification(
                        "Günlük Ödül!",
                        "Bugünkü altın yumurtalarını almayı unutma!",
                        103
                    )
                    repository.addNotification("Bildirim", "Günlük ödül hatırlatması gönderildi.")
                }
            }
        }

        return Result.success()
    }

    private fun showNotification(title: String, message: String, id: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Hatırlatıcılar", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }
}
