# Proyecto Paralelo Móvil - Backend & Android Kotlin

Proyecto completo para la asignatura de **Algoritmos Paralelos**. Integra un Backend en **FastAPI (Dockerizado)** con base de datos, autenticación **JWT**, CRUD de usuarios y subida de archivos, junto a una **Aplicación Móvil Nativa en Android con Kotlin (MVVM)** orientada a la medición y comparación en vivo de **Programación Concurrente/Paralela vs. Secuencial**.

---

## 📁 Estructura del Repositorio

- **`backend/`**: API REST en FastAPI.
  - Base de datos con SQLAlchemy (`users`, `files`).
  - Autenticación segura mediante JWT (`POST /login`, `POST /register`, hashing con `bcrypt`).
  - CRUD de Usuarios (`/users`, `/users/{id}`).
  - Subida de archivos (`POST /upload`, `DELETE /upload/{id}`).
  - Endpoints de Dashboard para consumo concurrente (`/dashboard/profile`, `/dashboard/stats`, etc.).
  - Dockerizado con [`Dockerfile`](backend/Dockerfile) y [`docker-compose.yml`](docker-compose.yml).
- **`app_movil/`**: Aplicación Android Nativa en Kotlin.
  - Arquitectura **MVVM** (View, ViewModel, Repository, Retrofit, StateFlow).
  - Consumo simultáneo de endpoints al cargar Dashboard (`async`/`awaitAll`).
  - Pantalla interactiva de **Benchmark Concurrente vs. Secuencial** con cálculo de Aceleración (*Speedup*).
  - Guía teórica en pantalla lista para la sustentación y defensa en clase.

---

## 🚀 1. Ejecución del Backend

### Opción A: Con Docker Compose (Recomendado para evaluación)
```bash
docker compose up -d
```
Esto levantará la API en el puerto `8000` y la base de datos PostgreSQL en el puerto `5432`.

### Opción B: Ejecución Local en Python
```bash
python -m venv .venv
.venv\Scripts\activate   # Windows
pip install -r requirements.txt
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

- Documentación interactiva Swagger: `http://localhost:8000/docs`
- Usuario inicial precargado:
  - **Email:** `admin@paralelo.com`
  - **Password:** `admin123`

---

## 📱 2. Ejecución de la App Móvil (Android Kotlin)

1. Abre **Android Studio**.
2. Selecciona **Open** y abre la carpeta [`app_movil/`](app_movil/).
3. Espera la sincronización de dependencias Gradle.
4. Ejecuta en el emulador de Android Studio o en un dispositivo físico.
   - En emulador, se conectará a `http://10.0.2.2:8000/`.
   - En celular físico, usa el botón "Configurar Servidor / IP" en la pantalla de inicio para ingresar la IP local de tu PC.

---

## ⚡ 3. Demostración de Concurrencia y Paralelismo

1. **Dashboard Simultáneo:** Al iniciar sesión, la app ejecuta en paralelo mediante Coroutines (`Dispatchers.IO`) los endpoints de usuarios, perfil, estadísticas y notificaciones, mostrando el tiempo exacto en milisegundos.
2. **Benchmark Comparativo:** En la pantalla de Benchmark se procesan 5 archivos independientes:
   - **Modo Secuencial:** Procesa uno a uno en serie, registrando el tiempo total.
   - **Modo Concurrente:** Despacha todas las tareas simultáneamente con `async`/`awaitAll`.
   - **Métricas:** Muestra tiempo secuencial, tiempo concurrente y el factor de aceleración (*Speedup* ej. `⚡ 3.8x más rápido`).
