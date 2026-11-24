package com.luzalert

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var etBotToken: EditText
    private lateinit var etChatId: EditText
    private lateinit var btnSaveAndTest: Button
    private lateinit var btnToggleService: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvPowerStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etBotToken = findViewById(R.id.etBotToken)
        etChatId = findViewById(R.id.etChatId)
        btnSaveAndTest = findViewById(R.id.btnSaveAndTest)
        btnToggleService = findViewById(R.id.btnToggleService)
        tvStatus = findViewById(R.id.tvStatus)
        tvPowerStatus = findViewById(R.id.tvPowerStatus)

        loadSavedConfig()
        checkBatteryOptimization()
        updateServiceButtonState()
        updatePowerStatus()

        btnSaveAndTest.setOnClickListener {
            saveConfig()
            testTelegramConnection()
        }

        btnToggleService.setOnClickListener {
            toggleService()
        }
    }

    override fun onResume() {
        super.onResume()
        updateServiceButtonState()
        updatePowerStatus()
    }

    private fun loadSavedConfig() {
        val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
        etBotToken.setText(prefs.getString("bot_token", ""))
        etChatId.setText(prefs.getString("chat_id", ""))
    }

    private fun saveConfig() {
        val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("bot_token", etBotToken.text.toString().trim())
            .putString("chat_id", etChatId.text.toString().trim())
            .apply()
        Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show()
    }

    private fun testTelegramConnection() {
        val token = etBotToken.text.toString().trim()
        val chatId = etChatId.text.toString().trim()

        if (token.isEmpty() || chatId.isEmpty()) {
            Toast.makeText(this, "Ingresa tu Token y Chat ID primero", Toast.LENGTH_LONG).show()
            return
        }

        tvStatus.text = "Enviando mensaje de prueba..."
        TelegramNotifier.sendMessage(
            token,
            chatId,
            "🧪 *Prueba de conexión exitosa*\nEl teléfono sensor está listo para vigilar la luz."
        ) { success, details ->
            if (success) {
                tvStatus.text = "✅ Conexión con Telegram exitosa."
                Toast.makeText(this, "¡Mensaje recibido en Telegram!", Toast.LENGTH_SHORT).show()
            } else {
                tvStatus.text = "❌ Error al enviar: $details"
                Toast.makeText(this, "Fallo: $details", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun toggleService() {
        val serviceIntent = Intent(this, PowerMonitorService::class.java)
        if (PowerMonitorService.isServiceRunning) {
            stopService(serviceIntent)
            getSharedPreferences("config", Context.MODE_PRIVATE).edit().putBoolean("service_enabled", false).apply()
            PowerMonitorService.isServiceRunning = false
        } else {
            val token = etBotToken.text.toString().trim()
            val chatId = etChatId.text.toString().trim()
            if (token.isEmpty() || chatId.isEmpty()) {
                Toast.makeText(this, "Configura primero el Bot Token y Chat ID", Toast.LENGTH_SHORT).show()
                return
            }
            saveConfig()
            getSharedPreferences("config", Context.MODE_PRIVATE).edit().putBoolean("service_enabled", true).apply()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }
        updateServiceButtonState()
    }

    private fun updateServiceButtonState() {
        if (PowerMonitorService.isServiceRunning) {
            btnToggleService.text = "Detener Vigilancia"
            btnToggleService.setBackgroundColor(0xFFD32F2F.toInt())
            tvStatus.text = "🟢 Vigilando: El monitor está activo en segundo plano."
        } else {
            btnToggleService.text = "Iniciar Vigilancia de Luz"
            btnToggleService.setBackgroundColor(0xFF2E7D32.toInt())
            tvStatus.text = "⚪ Inactivo: Presiona 'Iniciar' para vigilar el cargador."
        }
    }

    private fun updatePowerStatus() {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            registerReceiver(null, ifilter)
        }
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        if (isCharging) {
            tvPowerStatus.text = "⚡ Toma de corriente: CONECTADA (Hay electricidad)"
            tvPowerStatus.setTextColor(0xFF2E7D32.toInt())
        } else {
            tvPowerStatus.text = "🔋 Toma de corriente: DESCONECTADA (En batería)"
            tvPowerStatus.setTextColor(0xFFC62828.toInt())
        }
    }

    private fun checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    startActivity(intent)
                } catch (ignored: Exception) {}
            }
        }
    }
}
