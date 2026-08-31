package mb28.crystalTimer

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import mb28.crystalTimer.ui.theme.CrystalTimerTheme

class TimerIsFinishedActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightNavigationBars = true
        controller.isAppearanceLightStatusBars = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        super.onCreate(savedInstanceState)
        setContent {
            CrystalTimerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Timer is finished!",
                            fontSize = 36.sp,
                            fontFamily = FontFamily(Font(R.font.mali_bold)),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.height(200.dp))
                        Button(
                            {
                                ringtone.stop()
                                finish()
                            }
                        ) { Text(
                            "Stop",
                            fontSize = 22.sp,
                            modifier = Modifier.padding(15.dp)
                        ) }
                    }
                }
            }
        }
    }
}
