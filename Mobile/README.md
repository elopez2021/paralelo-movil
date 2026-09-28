# Aplicación Móvil - Paralelo Móvil (Android Kotlin)

Aplicación móvil nativa en **Android con Kotlin** para la asignatura de **Algoritmos Paralelos**. Implementa la arquitectura **MVVM**, autenticación **JWT**, **CRUD completo de usuarios**, consumo simultáneo de endpoints y una **pantalla de benchmark para comparar el tiempo de ejecución secuencial vs concurrente/paralelo** mediante **Kotlin Coroutines**.

---

## 🛠️ Tecnologías y Arquitectura

- **Lenguaje:** Kotlin
- **Plataforma:** Android Nativo (minSdk 24, targetSdk 35)
- **UI:** Jetpack Compose con Material 3 (Tema moderno oscuro Slate/Indigo/Cyan)
- **Arquitectura:** **MVVM (Model - View - ViewModel)**
  - `View`: [MainActivity.kt](src/main/java/com/movil/paralelo/MainActivity.kt), Screens (`LoginScreen`, `DashboardScreen`, `UsersScreen`, `BenchmarkScreen`).
  - `ViewModel`: `AuthViewModel`, `UsersViewModel`, `DashboardViewModel`, `ConcurrencyBenchmarkViewModel`.
  - `Repository`: `AuthRepository`, `UserRepository`, `FileRepository`, `DashboardRepository`.
  - `Network / Service`: `Retrofit 2`, `OkHttp 3`, `AuthInterceptor` (inyección automática del header `Authorization: Bearer <TOKEN>`).
- **Concurrencia y Paralelismo:**
  - `Kotlin Coroutines`
  - `Dispatchers.IO`
  - `async` / `awaitAll`
  - `measureTimeMillis` para benchmarking de alta precisión

---

## 🚀 Cómo abrir y ejecutar en Android Studio

1. **Abrir Android Studio.**
2. Selecciona **Open** y navega a la carpeta:
   `e:\algoritmos paralelos\tarea 1\app_movil`
3. Espera a que termine la sincronización de Gradle (**Sync Project with Gradle Files**).
4. Inicia un Emulador Android (o conecta tu celular físico en modo Depuración USB).
5. Presiona **Run 'app'** (ícono verde de Play ▶️).

---

## 🌐 Conexión con el Backend (FastAPI)

1. **Si usas Emulador de Android Studio:**
   - La app se conecta por defecto a `http://10.0.2.2:8000/` (que es el alias de `localhost` de tu computadora dentro del emulador).
2. **Si usas un Celular Físico:**
   - En la pantalla de Login, presiona el botón **"Configurar Servidor / IP"**.
   - Coloca la IP local de tu PC, por ejemplo: `http://192.168.1.50:8000/`.

---

## 🔑 Credenciales de Prueba por Defecto

El backend viene precargado con un usuario inicial:
- **Email:** `admin@paralelo.com`
- **Contraseña:** `admin123`

*(También puedes registrar un usuario nuevo directamente desde el botón "Regístrate aquí" en la app).*

---

## 📊 Demostración de Concurrencia y Paralelismo en Clase

Para la corrección del profesor, el proyecto demuestra el paralelismo en dos pantallas clave:

### 1. Dashboard (Consumo Simultáneo de 4 Endpoints)
Al entrar al Dashboard, se consumen de manera simultánea en paralelo:
- `GET /dashboard/profile`
- `GET /dashboard/stats`
- `GET /dashboard/notifications`
- `GET /users`

En lugar de esperar uno por uno, se ejecutan a la vez con `async(Dispatchers.IO)` y `awaitAll()`, mostrando en la parte superior el tiempo total en milisegundos.

### 2. Pantalla de Benchmark (Secuencial vs Concurrente)
Desde el Dashboard, haz clic en **"Demostración Concurrencia (Secuencial vs Paralelo)"**:
1. Presiona **"Secuencial"**: Verás cómo las tareas se procesan una tras otra y se mide el tiempo total (ej. ~6.5 s).
2. Presiona **"Concurrente"**: Todas las tareas cambian a estado "En ejecución..." simultáneamente y terminan casi al mismo tiempo (ej. ~1.8 s).
3. Verás en vivo la tarjeta comparativa con:
   - **Tiempo Secuencial**
   - **Tiempo Concurrente**
   - **Aceleración (Speedup):** `⚡ 3.6x Más Rápido`
4. Presiona **"Ver Guía de Sustentación para Clase"** en la parte inferior para ver los argumentos teóricos que evaluará el profesor.
