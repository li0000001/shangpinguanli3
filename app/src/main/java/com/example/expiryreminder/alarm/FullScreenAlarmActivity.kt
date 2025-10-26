package com.example.expiryreminder.alarm

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.expiryreminder.R
import com.example.expiryreminder.alarm.ReminderReceiver.Companion.EXTRA_DAYS_BEFORE
import com.example.expiryreminder.alarm.ReminderReceiver.Companion.EXTRA_PRODUCT_NAME
import com.example.expiryreminder.alarm.ReminderReceiver.Companion.EXTRA_REMINDER_TIME
import com.example.expiryreminder.ui.theme.ExpiryReminderTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FullScreenAlarmActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val productName = intent.getStringExtra(EXTRA_PRODUCT_NAME) ?: ""
        val reminderTime = intent.getStringExtra(EXTRA_REMINDER_TIME) ?: ""
        val daysBefore = intent.getIntExtra(EXTRA_DAYS_BEFORE, 0)
        val expirationDate = intent.getLongExtra(ReminderReceiver.EXTRA_EXPIRATION_DATE, 0L)

        playAlarmSound()

        setContent {
            ExpiryReminderTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.alarm_title),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = stringResource(R.string.alarm_message, productName, daysBefore),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        val formattedDate = remember(expirationDate) {
                            SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA).format(Date(expirationDate))
                        }
                        Text(
                            text = stringResource(R.string.alarm_expiration_date, formattedDate),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = stringResource(R.string.alarm_reminder_time, reminderTime),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Button(
                            onClick = {
                                stopAlarmSound()
                                finish()
                            },
                            modifier = Modifier.padding(top = 24.dp)
                        ) {
                            Text(stringResource(R.string.dismiss))
                        }
                    }
                }
            }
        }
    }

    private fun playAlarmSound() {
        stopAlarmSound()
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        mediaPlayer = MediaPlayer().apply {
            setDataSource(this@FullScreenAlarmActivity, alarmUri)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            isLooping = true
            prepare()
            start()
        }
    }

    private fun stopAlarmSound() {
        mediaPlayer?.run {
            stop()
            release()
        }
        mediaPlayer = null
    }

    override fun onDestroy() {
        stopAlarmSound()
        super.onDestroy()
    }
}
