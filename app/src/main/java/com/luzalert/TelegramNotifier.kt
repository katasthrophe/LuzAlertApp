package com.luzalert

import android.os.Handler
import android.os.Looper
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors

object TelegramNotifier {
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun sendMessage(
        botToken: String,
        chatId: String,
        message: String,
        onResult: ((Boolean, String?) -> Unit)? = null
    ) {
        if (botToken.isBlank() || chatId.isBlank()) {
            onResult?.invoke(false, "Token o Chat ID vacíos")
            return
        }

        executor.execute {
            var connection: HttpURLConnection? = null
            try {
                val encodedMsg = URLEncoder.encode(message, "UTF-8")
                val urlString = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encodedMsg&parse_mode=Markdown"
                val url = URL(urlString)

                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                }

                val responseCode = connection.responseCode
                val success = (responseCode in 200..299)
                
                mainHandler.post {
                    onResult?.invoke(success, if (success) "Mensaje enviado (HTTP $responseCode)" else "Error HTTP $responseCode")
                }
            } catch (e: Exception) {
                mainHandler.post {
                    onResult?.invoke(false, e.localizedMessage ?: "Error de red desconocido")
                }
            } finally {
                connection?.disconnect()
            }
        }
    }
}
