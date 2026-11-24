# 💡 LuzAlert - App Android para Detección de Cortes de Luz

Esta aplicación convierte un smartphone Android viejo en un **sensor de corte eléctrico en tiempo real** conectado a tu **Telegram**.

---

## ⚡ ¿Cómo funciona?
1. Dejas el teléfono viejo enchufado a la toma de corriente en casa, conectado al cargador y con **datos móviles (SIM)** activos.
2. La app corre un servicio en segundo plano vigilando el estado del cargador.
3. **En el segundo exacto que se va la luz:**
   - La toma de corriente se apaga.
   - El teléfono despierta instantáneamente su procesador con batería.
   - Envía un mensaje de emergencia a tu Telegram:
     `🚨 ¡ALERTA DE CORTE DE LUZ! - Se fue la electricidad a las HH:mm:ss`.
4. **Cuando regresa la electricidad:**
   - Detecta la entrada de energía nuevamente.
   - Envía a tu Telegram:
     `✅ ¡ELECTRICIDAD RESTABLECIDA! - Duración del corte: X min Y seg`.

---

## 🚀 ¿Cómo compilar y descargar el APK gratis en GitHub?

No necesitas instalar Java ni Android SDK en tu computadora. GitHub Actions compilará el archivo `.apk` automáticamente en sus servidores en la nube:

### Paso 1: Subir este proyecto a GitHub
1. Entra a tu cuenta en [GitHub.com](https://github.com) y crea un nuevo repositorio (por ejemplo: `LuzAlertApp`).
2. Sube los archivos de esta carpeta a tu repositorio. Puedes hacerlo de dos formas:
   * **Vía Web:** Arrastra los archivos y carpetas directamente en la web de GitHub.
   * **Vía Git (Consola):**
     ```bash
     cd C:\Users\thecr\.gemini\antigravity\scratch\LuzAlertApp
     git init
     git add .
     git commit -m "Initial commit LuzAlert"
     git branch -M main
     git remote add origin https://github.com/TU_USUARIO/LuzAlertApp.git
     git push -u origin main
     ```

### Paso 2: Descargar tu APK compilado
1. En tu repositorio de GitHub, entra en la pestaña **Actions**.
2. Verás que el flujo **"Build Android APK"** se inició automáticamente al subir los archivos (tarda ~1 a 2 minutos).
3. Cuando termine (aparezca con un check verde ✅), haz clic sobre él.
4. Al final de la página, en la sección **Artifacts**, verás el archivo **`LuzAlert-Debug-APK`**.
5. ¡Haz clic en él para descargarlo! Viene comprimido en un `.zip` que contiene tu archivo **`app-debug.apk`**.

---

## 📱 Instalación y Configuración en el Teléfono Viejo

1. Pasa el archivo `app-debug.apk` a tu teléfono viejo e instálalo (permite "Instalar aplicaciones de fuentes desconocidas" si te lo solicita).
2. Abre la app **Luz Alert**:
   * Escribe tu **Bot Token** de Telegram.
   * Escribe tu **Chat ID** de Telegram.
   * Presiona **"Guardar y Enviar Prueba"** para confirmar que te llegue el mensaje de prueba a Telegram.
3. Presiona el botón verde **"Iniciar Vigilancia de Luz"**.
4. Verás una notificación persistente arriba: *"Monitor de Luz Activo"*.
5. **Listo.** Conecta el teléfono al cargador y déjalo en un rincón de casa. Si desconectas el cargador, ¡recibirás la alerta de inmediato!
