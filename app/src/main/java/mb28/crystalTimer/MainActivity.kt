package mb28.crystalTimer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.core.view.WindowCompat
import mb28.crystalTimer.ui.theme.CrystalTimerTheme
import kotlin.time.Duration.Companion.milliseconds

const val CHANNEL_LIVE_TIMER = "Timer"

lateinit var ringtone: Ringtone
lateinit var vibrator: VibratorManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightNavigationBars = true
        controller.isAppearanceLightStatusBars = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
            }
        }

        val nm = NotificationManagerCompat.from(this)
        val channel = NotificationChannelCompat.Builder(CHANNEL_LIVE_TIMER, NotificationManagerCompat.IMPORTANCE_LOW)
            .setName("Live Timer")
            .setDescription("Shows timer's remaining duration as live notification")
            .build()

        nm.createNotificationChannel(channel)

        val rm = RingtoneManager(this)
        val r = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        ringtone = rm.getRingtone(rm.getRingtonePosition(r))

        vibrator = getSystemService<VibratorManager>()!!

        super.onCreate(savedInstanceState)

        setContent {
            CrystalTimerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TimerPage(
                        Modifier.padding(innerPadding),
                        nm
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NotificationManagerCompat.from(this).cancel(0)
    }
}

lateinit var timer: CountDownTimer

@Composable
fun TimerPage(modifier: Modifier = Modifier, nm: NotificationManagerCompat) {
    val context = LocalActivity.current
    val primary = MaterialTheme.colorScheme.primary
    val times = listOf("30s", "1m", "2m", "5m", "8m", "10m", "15m")
    var isRunning by rememberSaveable { mutableStateOf(false) }
    var duration by rememberSaveable { mutableLongStateOf(0) }
    var time by rememberSaveable { mutableLongStateOf(duration) }
    val animatedTime = animateFloatAsState(
        (time.toFloat() / duration).takeIf { time.toFloat() != 0f } ?: 0f,
        TweenSpec(500, easing = LinearEasing)
    )
    var pickerText by rememberSaveable { mutableStateOf("") }
    var showPicker by remember { mutableStateOf(false) }

    fun stop() {
        context?.window?.decorView?.keepScreenOn = false
        timer.cancel()
        time = 0
        isRunning = false
        nm.cancel(0)
    }
    fun start(durationMS: Long) {
        ringtone.stop()
        context?.window?.decorView?.keepScreenOn = true
        duration = durationMS
        time = durationMS
        timer = object : CountDownTimer(duration, 300) {
            override fun onFinish() {
                stop()
                ringtone.play()
                context!!.startActivity(Intent(context, TimerIsFinishedActivity::class.java))
            }
            override fun onTick(millisUntilFinished: Long) {
                time = millisUntilFinished

                val t = time.milliseconds
                val timerText = "${t.inWholeHours.toString().padStart(2, '0')}:" +
                        "${t.inWholeMinutes.rem(60).toString().padStart(2, '0')}:" +
                        t.inWholeSeconds.rem(60).toString().padStart(2, '0')
                showTimerNotification(nm, context!!, timerText,
                    primary
                )
            }
        }
        timer.start()
        isRunning = true
    }

    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val t = time.milliseconds
        Text(
            "${t.inWholeHours.toString().padStart(2, '0')}:" +
                "${t.inWholeMinutes.rem(60).toString().padStart(2, '0')}:" +
                t.inWholeSeconds.rem(60).toString().padStart(2, '0'),
            fontSize = 160.sp,
            fontFamily = FontFamily(Font(R.font.tulpen_one))
        )

        Spacer(Modifier.height(120.dp))
        CircularWavyProgressIndicator(
            progress = { animatedTime.value },
            modifier = modifier.scale(7f),
            gapSize = 1.dp,
            waveSpeed = 8.dp,
        )
        Spacer(Modifier.height(110.dp))

        if (isRunning) {
            OutlinedButton(
                { stop() }
            ) { Text(
                "Stop",
                fontSize = 22.sp,
                modifier = Modifier.padding(15.dp)
            ) }
        }
        else {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                times.forEachIndexed { index, string ->
                    OutlinedIconButton(
                        { start(
                            when (index) {
                                0 -> 30000
                                1 -> 60000
                                2 -> 120000
                                3 -> 300000
                                4 -> 480000
                                5 -> 600000
                                else -> 900000
                            }
                        ) }
                    ) {
                        Text(
                            string,
                            fontFamily = FontFamily(Font(R.font.mali_bold))
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Button(
                { showPicker = true }
            ) { Text(
                "Select Duration",
                fontSize = 22.sp,
                modifier = Modifier.padding(10.dp)
            ) }
        }
    }

    if (showPicker) {
        AlertDialog(
            {
                showPicker = false
            },
            {
                Button(
                    {
                        try {
                            val times = pickerText.split(",")
                            val duration = when(times.count()) {
                                1 -> times[0].toLong() * 60
                                2 -> times[0].toLong() * 60 + (times[1].toLong())
                                3 -> times[0].toLong() + (times[1].toLong()) + (times[2].toLong() * 60 * 60)
                                else -> throw Exception()
                            } * 1000
                            start(duration)
                        } catch (_: Exception) {
                            Toast.makeText(context!!, "Invalid time format. example: 1,30 for 1 minute & 30 seconds",
                                Toast.LENGTH_LONG).show()
                        } finally {
                            showPicker = false
                        }
                    }
                ) {
                    Text("Start")
                }
            },
            title = {
                Text("Start new alarm")
            },
            text = {
                OutlinedTextField(
                    pickerText,
                    {
                        pickerText = it
                    },
                    label = {
                        Text("Min,Sec,Hour (ex: 1,30)")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberSigned
                    ),
                    shape = RoundedCornerShape(25.dp)
                )
            }
        )
    }

}






