package mb28.crystalTimer

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import kotlin.time.Duration

fun showTimerNotification(nm: NotificationManagerCompat, context: Activity, title: String, subtitle: String,
                          shortCriticalText: String, color: androidx.compose.ui.graphics.Color, progress: Duration,
                          duration: Duration, id: Int = 0) {
    val d = duration.inWholeSeconds.toInt()
    val style: NotificationCompat.ProgressStyle = NotificationCompat.ProgressStyle()
        .setStyledByProgress(false)
        .setProgress(d - progress.inWholeSeconds.toInt())
        .setProgressTrackerIcon(IconCompat.createWithResource(context, R.drawable.timer_progress_icon))
        .setProgressSegments(
            listOf(
                NotificationCompat.ProgressStyle.Segment(d).setColor(color.toArgb())
            )
        )
        .setProgressPoints(
            listOf(
                NotificationCompat.ProgressStyle.Point((d * 0.33).toInt()).setColor(color.copy(red = color.red * 0.7f).toArgb()),
                NotificationCompat.ProgressStyle.Point((d * 0.66).toInt()).setColor(color.copy(red = color.red * 1.3f).toArgb())
            )
        )

    val n = NotificationCompat.Builder(context, CHANNEL_LIVE_TIMER)
        .setColor(color.toArgb())
        .setContentTitle(title)
        .setShortCriticalText(shortCriticalText)
        .setOngoing(true)
        .setRequestPromotedOngoing(true)
        .setSmallIcon(R.drawable.notif_icon)
        .setStyle(style)
        .build()


    if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
        nm.notify(id, n)
    }
}
