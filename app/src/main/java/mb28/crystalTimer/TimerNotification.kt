package mb28.crystalTimer

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

fun showTimerNotification(nm: NotificationManagerCompat, context: Activity, shortCriticalText: String,
                          color: androidx.compose.ui.graphics.Color, id: Int = 0) {
    val n = NotificationCompat.Builder(context, CHANNEL_LIVE_TIMER)
        .setColor(color.toArgb())
        .setSmallIcon(R.drawable.notif_icon)
        .setOngoing(true)
        .setRequestPromotedOngoing(true)
        .setContentTitle(shortCriticalText)
        .setShortCriticalText(shortCriticalText)

    if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
        nm.notify(id, n.build())
    }
}
