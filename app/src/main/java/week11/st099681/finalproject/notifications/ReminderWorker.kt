package week11.st099681.finalproject.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import kotlin.math.max
import week11.st099681.finalproject.R

const val REMINDER_CHANNEL_ID = "maintenance_reminders"

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Maintenance Reminder"
        val text = inputData.getString("text") ?: "A service is due for your vehicle."

        ensureChannel(applicationContext)

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission revoked after scheduling; nothing we can do.
            return Result.success()
        }

        val notification = NotificationCompat.Builder(applicationContext, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(System.currentTimeMillis().toInt(), notification)
        return Result.success()
    }
}

fun ensureChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= 26) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Maintenance Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }
}

/**
 * Schedules a one-time reminder notification. If [triggerAtMillis] is in the
 * past (service already due), the notification fires shortly after scheduling.
 */
fun scheduleReminder(context: Context, title: String, text: String, triggerAtMillis: Long) {
    val delay = max(triggerAtMillis - System.currentTimeMillis(), 5_000L)
    val request = OneTimeWorkRequestBuilder<ReminderWorker>()
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(workDataOf("title" to title, "text" to text))
        .build()
    WorkManager.getInstance(context).enqueue(request)
}
