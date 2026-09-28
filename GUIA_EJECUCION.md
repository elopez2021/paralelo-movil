# 📱 Guía Paso a Paso: Ejecución y Pruebas (PC y Teléfono Android)

Esta guía explica detalladamente cómo levantar el backend en tu computadora y cómo ejecutar la aplicación móvil nativa tanto en el **Emulador de Android Studio (en tu PC)** como en un **Teléfono Android Físico**.

---

## 📋 Resumen Rápido de Credenciales
- **Usuario administrador precargado:**
  - **Email:** `admin@paralelo.com`
  - **Contraseña:** `admin123`
- **Documentación Swagger de la API:** `http://localhost:8000/docs`

---

## 🖥️ Paso 1: Levantar el Backend en tu PC

Antes de abrir la app móvil, el backend debe estar corriendo en tu computadora. Puedes levantarlo de dos formas:

### Opción A: Ejecución Local con Python (Más rápida para desarrollo)
1. Abre una terminal (PowerShell o CMD) en la raíz del proyecto (`e:\algoritmos paralelos\tarea 1`).
2. Activa el entorno virtual:
   ```powershell
   .venv\Scripts\activate
   ```
3. Inicia el servidor de FastAPI:
   ```powershell
   uvicorn main:app --reload --host 0.0.0.0 --port 8000
   ```
   > **Nota importante:** El parámetro `--host 0.0.0.0` es fundamental para que el servidor acepte conexiones tanto del emulador como de tu teléfono por Wi-Fi.

### Opción B: Ejecución con Docker Compose
Si tienes Docker Desktop instalado:
```powershell
docker compose up -d
```

### ✅ Verificación del Backend
Abre tu navegador en:
- [http://localhost:8000/docs](http://localhost:8000/docs)
Deberás ver la documentación Swagger interactiva con todos los endpoints de autenticación, usuarios, archivos y dashboard.

---

## 💻 Paso 2: Ejecutar la App en el Emulador de tu PC (Android Studio)

1. **Abrir el proyecto móvil:**
   - Abre **Android Studio**.
   - Haz clic en **File > Open...** (o **Open** en la pantalla de bienvenida).
   - Selecciona la carpeta:
     ```
     e:\algoritmos paralelos\tarea 1\app_movil
     ```
2. **Sincronización:**
   - Espera a que termine la sincronización de Gradle (*Sync Project with Gradle Files*). Si te pide descargar SDKs o herramientas, dale en aceptar.
3. **Crear o Iniciar un Emulador:**
   - Ve a **Tools > Device Manager** en Android Studio.
   - Si no tienes un emulador creado, haz clic en **Create Device** (por ejemplo, *Pixel 7* o *Pixel 8* con API 34 o 35) y haz clic en el ícono de **Play (▶️)** para encenderlo.
4. **Ejecutar la App:**
   - En la barra superior de Android Studio, asegúrate de que esté seleccionado el emulador y el módulo `app`.
   - Haz clic en el botón verde **Run 'app' (▶️)** o presiona `Shift + F10`.
5. **Conexión Automática:**
   - La aplicación está preconfigurada con `http://10.0.2.2:8000/`.
   - *(En Android, `10.0.2.2` es la dirección especial que utiliza el emulador para comunicarse con el `localhost` de tu computadora).*
   - Inicia sesión directamente con `admin@paralelo.com` y `admin123`.

---

## 📲 Paso 3: Ejecutar la App en tu Teléfono Android Físico

Para conectar tu teléfono físico a la API de tu computadora, ambos dispositivos deben estar conectados a la **misma red Wi-Fi**.

### 3.1 Obtener la IP Local de tu PC
1. Abre una terminal de PowerShell o CMD en tu PC y escribe:
   ```powershell
   ipconfig
   ```
2. Busca tu adaptador Wi-Fi y copia la **Dirección IPv4**. Por ejemplo:
   ```text
   Adaptador de LAN inalámbrica Wi-Fi:
      Dirección IPv4. . . . . . . . . . . . . . : 192.168.1.45
   ```
   *(En este ejemplo, la IP de tu PC es `192.168.1.45`)*.

> **💡 Firewall de Windows:** Si tu teléfono no logra conectar, asegúrate de que el Firewall de Windows permita conexiones al puerto `8000` o selecciona red privada.

---

### 3.2 Método A: Instalar directamente desde Android Studio (Por Cable USB)

1. **Activar Opciones de Desarrollador en tu Teléfono:**
   - Ve a **Ajustes > Acerca del teléfono**.
   - Presiona 7 veces seguidas sobre **"Número de compilación"** hasta que diga *"¡Ya eres desarrollador!"*.
   - Ve a **Ajustes > Sistema > Opciones de desarrollador** (o busca "Depuración USB") y activa **Depuración por USB**.
2. **Conectar el teléfono a la PC:**
   - Conecta el cable USB a la computadora.
   - En la pantalla de tu celular aparecerá un mensaje: *"¿Permitir depuración USB?"* -> Marca *"Permitir siempre"* y presiona **Aceptar**.
3. **Ejecutar desde Android Studio:**
   - En Android Studio, en la lista desplegable de dispositivos arriba, verás el nombre y modelo de tu teléfono físico.
   - Selecciónalo y haz clic en **Run 'app' (▶️)**.
   - La aplicación se compilará e instalará automáticamente en tu celular.

---

### 3.3 Método B: Generar el archivo `.apk` para instalarlo manualmente

Si prefieres pasar el archivo instalador (`.apk`) a tu teléfono por WhatsApp, Telegram, Google Drive o USB:

1. En Android Studio, ve al menú superior:
   - **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
2. Espera a que termine la compilación (unos segundos).
3. Verás una notificación abajo a la derecha: *"APK(s) generated successfully"*. Haz clic en **locate**.
   - El archivo generado se encuentra en:
     `e:\algoritmos paralelos\tarea 1\app_movil\app\build\outputs\apk\debug\app-debug.apk`
4. Pasa ese archivo `app-debug.apk` a tu teléfono e instálalo (permite la instalación de fuentes desconocidas si tu teléfono te lo solicita).

---

### 3.4 Conectar la App en tu Teléfono con tu PC

1. Abre la app **Paralelo Móvil** en tu teléfono.
2. En la pantalla de Login, presiona el botón:
   **⚙️ Configurar Servidor / IP**
3. Cambia la URL por la IP de tu computadora obtenida en el paso 3.1:
   ```text
   http://192.168.1.45:8000/
   ```
   *(Reemplaza `192.168.1.45` por tu IP real y asegúrate de mantener `:8000/` al final)*.
4. Presiona **Guardar**.
5. ¡Listo! Ya puedes iniciar sesión con `admin@paralelo.com` y `admin123`.

---

## 🎯 Paso 4: Flujo de Demostración para la Clase

Sigue este recorrido para exponer el proyecto ante el profesor:

### 1. Inicio de Sesión (JWT)
- Muestra el formulario de login. Puedes usar las credenciales de prueba o registrar un nuevo usuario con el botón *"¿No tienes cuenta? Regístrate aquí"*.
- El backend responde con el token JWT que la app almacena de forma segura en `SessionManager`.

### 2. Dashboard y Consumo Concurrente
- Al entrar al Dashboard, observa el banner verde superior:
  - Muestra el mensaje: **"4 endpoints resueltos en paralelo con Coroutines en: X ms"**.
  - Explica al profesor que la app lanzó simultáneamente con `async(Dispatchers.IO)` y `awaitAll()` las peticiones a:
    - `/dashboard/profile`
    - `/dashboard/stats`
    - `/dashboard/notifications`
    - `/users`
  - Ninguno tuvo que esperar a que el otro terminara.

### 3. Gestión de Usuarios (CRUD)
- Presiona **"Gestión de Usuarios (CRUD)"**:
  - Muestra la lista de usuarios.
  - Presiona el botón flotante **`+`** para crear un usuario nuevo.
  - Edita el nombre o correo de un usuario existente con el ícono del lápiz.
  - Elimina un usuario con el ícono de la papelera.

### 4. Demostración Principal: Benchmark de Concurrencia (Secuencial vs Paralelo)
- Desde el Dashboard, entra a **"Demostración Concurrencia (Secuencial vs Paralelo)"**:
  - Verás un lote de 5 archivos independientes listos para subirse a la API.
  - **Prueba 1: Modo Secuencial**
    - Presiona el botón amarillo **"Secuencial"**.
    - Observa cómo las tareas cambian a *En ejecución* **una por una** en fila.
    - Observa el tiempo total obtenido (ejemplo: `~6.80 s`).
  - **Prueba 2: Modo Concurrente**
    - Presiona el botón verde **"Concurrente"**.
    - Observa cómo **todas las 5 tareas se activan simultáneamente** al mismo tiempo.
    - Observa el nuevo tiempo obtenido (ejemplo: `~1.85 s`).
  - **Comparación en Pantalla:**
    - Se calculará automáticamente el factor de aceleración:
      `⚡ 3.67x Más Rápido`.
  - Presiona **"Ver Guía de Sustentación para Clase"** al final de la pantalla para repasar los conceptos teóricos frente al docente.

---

## 🛠️ Preguntas Frecuentes y Solución de Problemas

- **¿La app dice "Error de conexión con el servidor"?**
  - Verifica que el backend esté corriendo (`uvicorn main:app --reload --host 0.0.0.0 --port 8000`).
  - Si estás en un teléfono físico, asegúrate de que tanto el teléfono como la PC estén en la misma red Wi-Fi y de haber configurado la IP correcta en el botón *Configurar Servidor / IP*.
- **¿Cómo reiniciar la base de datos a cero?**
  - Solo elimina el archivo [app.db](file:///e:/algoritmos%20paralelos/tarea%201/app.db) y vuelve a iniciar el backend; se creará automáticamente con el usuario demo.
