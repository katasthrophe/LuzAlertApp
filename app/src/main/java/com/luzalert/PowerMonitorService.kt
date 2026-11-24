package com.luzalert

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PowerMonitorService : Service() {

    private var powerReceiver: BroadcastReceiver? = null
    private var outageStartTime: Long = 0L

    companion object {
        const val CHANNEL_ID = "power_monitor_channel"
        const val NOTIFICATION_ID = 1001
        var isServiceRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
        createNotificationChannel()
        val notification = buildForegroundNotification("Vigilando conexión eléctrica...")
        startForeground(NOTIFICATION_ID, notification)

        registerPowerReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        powerReceiver?.let {
            unregisterReceiver(it)
            powerReceiver = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerPowerReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }

        powerReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                val wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "LuzAlert::PowerChangeWakeLock"
                )
                wakeLock.acquire(15000L) // 15 segundos para asegurar el envío de red con pantalla apagada

                val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
                val botToken = prefs.getString("bot_token", "") ?: ""
                val chatId = prefs.getString("chat_id", "") ?: ""
                val timeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                val nowFormatted = timeFormat.format(Date())

                when (intent.action) {
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        outageStartTime = System.currentTimeMillis()
                        updateNotification("⚠️ CORTE DETECTADO ($nowFormatted)")
                        
                        val msg = "🚨 *¡ALERTA DE CORTE DE LUZ!*\n\n" +
                                "⚡ *Estado:* Se fue la electricidad en casa.\n" +
                                "🕒 *Hora:* $nowFormatted\n" +
                                "📱 *Sensor:* Teléfono detector en batería."
                        
                        TelegramNotifier.sendMessage(botToken, chatId, msg)
                    }

                    Intent.ACTION_POWER_CONNECTED -> {
                        val durationText = if (outageStartTime > 0) {
                            val durationSeconds = (System.currentTimeMillis() - outageStartTime) / 1000
                            val minutes = durationSeconds / 60
                            val seconds = durationSeconds % 60
                            "$minutes min $seconds seg"
                        } else {
                            "Desconocida"
                        }
                        outageStartTime = 0L
                        updateNotification("✅ Luz conectada ($nowFormatted)")

                        val msg = "✅ *¡ELECTRICIDAD RESTABLECIDA!*\n\n" +
                                "⚡ *Estado:* La luz ha vuelto a casa.\n" +
                                "🕒 *Hora:* $nowFormatted\n" +
                                "⏳ *Duración del corte:* $durationText"

                        TelegramNotifier.sendMessage(botToken, chatId, msg)
                    }
                }
            }
        }

        registerReceiver(powerReceiver, filter)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Monitor de Corte de Luz",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mantiene el servicio activo para detectar cortes de luz"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(statusText: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Monitor de Luz Activo")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(statusText: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildForegroundNotification(statusText))
    }
}
