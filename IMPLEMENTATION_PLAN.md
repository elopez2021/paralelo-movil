# Plan de Implementación: Backend FastAPI + App Móvil Android (Kotlin)

Proyecto para la asignatura de **Algoritmos Paralelos**. Consiste en un sistema cliente-servidor compuesto por un Backend en **FastAPI (Dockerizado)** con base de datos, autenticación JWT, CRUD de usuarios y subida de archivos, y una **Aplicación Móvil Nativa en Android con Kotlin** (Arquitectura MVVM), enfocada en la implementación, medición y demostración de **programación concurrente vs. secuencial**.

---

## 1. Estructura del Proyecto y Carpetas

El repositorio se organizará separando claramente el backend de la aplicación móvil:

```text
paralelo-movil/
├── backend/                             # API REST en FastAPI
│   ├── app/
│   │   ├── core/
│   │   │   ├── config.py                # Variables de entorno y configuraciones
│   │   │   └── security.py              # Hashing (bcrypt) y gestión de JWT
│   │   ├── database/
│   │   │   ├── session.py               # Conexión SQLAlchemy / Engine
│   │   │   └── base.py                  # Base declarativa
│   │   ├── models/
│   │   │   ├── user.py                  # Modelo SQLAlchemy de Usuario
│   │   │   └── file_record.py           # Modelo SQLAlchemy de Archivo
│   │   ├── schemas/
│   │   │   ├── user.py                  # Esquemas Pydantic (UserCreate, UserResponse, UserUpdate)
│   │   │   ├── token.py                 # Esquema de Token JWT
│   │   │   ├── file_record.py           # Esquema de Archivo subido
│   │   │   └── dashboard.py             # Esquemas de Stats / Profile / Notifications
│   │   ├── routers/
│   │   │   ├── auth.py                  # POST /login, POST /register
│   │   │   ├── users.py                 # CRUD usuarios (/users, /users/{id})
│   │   │   ├── files.py                 # POST /upload, DELETE /upload/{id}
│   │   │   └── dashboard.py             # Endpoints para consumo simultáneo
│   │   ├── services/
│   │   │   └── computation.py           # Procesamiento pesado / divisores
│   │   └── main.py                      # Punto de entrada de la aplicación FastAPI
│   ├── uploads/                         # Directorio local de almacenamiento de archivos
│   ├── Dockerfile                       # Contenedor para la API
│   ├── docker-compose.yml               # Orquestación de API + Base de Datos (PostgreSQL o SQLite)
│   └── requirements.txt                 # Dependencias Python
│
├── app_movil/                           # Aplicación Android Nativa en Kotlin
│   ├── app/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/com/movil/paralelo/
│   │       ├── data/
│   │       │   ├── model/               # Modelos de datos / DTOs
│   │       │   │   ├── User.kt
│   │       │   │   ├── AuthModels.kt
│   │       │   │   ├── FileUploadResult.kt
│   │       │   │   └── DashboardData.kt
│   │       │   ├── network/             # Servicios HTTP (Retrofit)
│   │       │   │   ├── ApiService.kt
│   │       │   │   ├── AuthInterceptor.kt
│   │       │   │   └── RetrofitClient.kt
│   │       │   └── repository/          # Abstracción de datos
│   │       │       ├── AuthRepository.kt
│   │       │       ├── UserRepository.kt
│   │       │       ├── FileRepository.kt
│   │       │       └── DashboardRepository.kt
│   │       ├── ui/
│   │       │   ├── viewmodel/           # ViewModels (Gestión de estado y lógica)
│   │       │   │   ├── AuthViewModel.kt
│   │       │   │   ├── UsersViewModel.kt
│   │       │   │   ├── DashboardViewModel.kt
│   │       │   │   └── ConcurrencyBenchmarkViewModel.kt
│   │       │   └── views/               # Pantallas (Activities/Fragments o Jetpack Compose)
│   │       │       ├── LoginActivity.kt / LoginScreen.kt
│   │       │       ├── DashboardActivity.kt / DashboardScreen.kt
│   │       │       ├── UsersActivity.kt / UsersScreen.kt
│   │       │       └── BenchmarkActivity.kt / BenchmarkScreen.kt
│   │       └── utils/
│   │           ├── SessionManager.kt    # Almacenamiento seguro del JWT (EncryptedSharedPreferences / DataStore)
│   │           └── Constants.kt
│   └── build.gradle.kts
│
├── README.md
├── IMPLEMENTATION_PLAN.md
└── .gitignore
```

---

## 2. Fase Backend: FastAPI + Docker

### 2.1 Base de Datos y Persistencia
- Configurar base de datos relacional con **SQLAlchemy**.
- **Tabla `users`:**
  - `id`: Entero autoincremental / UUID.
  - `nombre`: String.
  - `apellido`: String.
  - `email`: String (único, indexado).
  - `password`: String (almacenado con hash `bcrypt`/`argon2`, nunca en texto plano).
  - `foto`: String (URL o nombre de archivo).
  - `createdAt`: Timestamp.
- **Tabla `files`:**
  - `id`: Entero autoincremental.
  - `filename`: Nombre asignado.
  - `original_name`: Nombre original.
  - `url`: Ruta accesible para consultar el archivo.
  - `mime_type`: Tipo de archivo.
  - `size`: Tamaño en bytes.
  - `createdAt`: Timestamp.

### 2.2 Autenticación y Seguridad JWT
- `POST /login`: Valida email y password; genera y retorna token JWT con expiración.
- `POST /register`: Registra un nuevo usuario con hashing seguro de password.
- Dependencia `get_current_user`: Extrae el token de la cabecera `Authorization: Bearer <TOKEN>`, valida firma y tiempo de expiración, e inyecta el usuario a los endpoints protegidos.

### 2.3 CRUD de Usuarios
- `GET /users`: Listado de todos los usuarios (requiere JWT).
- `GET /users/{id}`: Obtener usuario específico por ID.
- `POST /users`: Crear usuario desde panel administrativo.
- `PUT /users/{id}`: Actualizar datos de usuario.
- `DELETE /users/{id}`: Eliminar usuario.

### 2.4 Módulo de Subida de Archivos
- `POST /upload`: Recibe archivo por `multipart/form-data`, valida extensiones (imágenes, PDF, etc.), lo almacena en disco dentro de `uploads/`, registra la referencia en la BD y retorna:
  ```json
  {
    "id": 15,
    "filename": "foto_usuario_15.jpg",
    "url": "/uploads/foto_usuario_15.jpg"
  }
  ```
- `DELETE /upload/{id}`: Elimina el archivo del almacenamiento físico y su registro en la BD.
- Servir archivos estáticos mediante `app.mount("/uploads", StaticFiles(directory="uploads"), name="uploads")`.

### 2.5 Endpoints para el Dashboard Concurrente
Para demostrar el consumo simultáneo de endpoints independientes:
- `GET /dashboard/profile`: Datos del usuario autenticado.
- `GET /dashboard/stats`: Estadísticas del sistema (total usuarios, total archivos, etc.).
- `GET /dashboard/notifications`: Notificaciones o avisos recientes.

### 2.6 Dockerización
- **`Dockerfile`:**
  - Base Python 3.11-slim.
  - Copia de dependencias e instalación sin caché.
  - Exposición de puerto 8000.
  - Comando de ejecución con `uvicorn app.main:app --host 0.0.0.0 --port 8000`.
- **`docker-compose.yml`:**
  - Servicio `api`: Construye el Dockerfile, mapea puertos (`8000:8000`) y volumen para `/uploads`.
  - Servicio `db`: Base de datos PostgreSQL (con credenciales configuradas vía variables de entorno).

---

## 3. Fase Móvil: Android Nativo en Kotlin (Arquitectura MVVM)

La aplicación móvil se desarrollará en el directorio `app_movil/`.

### 3.1 Componentes de la Arquitectura MVVM
1. **View (UI):**
   - Interfaz gráfica limpia y moderna (en Jetpack Compose o XML/Material3).
   - Formulario de Login, Lista de Usuarios con FloatingActionButton para crear/editar, Pantalla de Perfil/Detalle, Pantalla de Benchmark.
   - Observa estados del ViewModel (`StateFlow` o `LiveData`) sin contener lógica de negocio.
2. **ViewModel:**
   - Mantiene el estado de la UI (`UiState`: Idle, Loading, Success, Error).
   - Expone métodos para disparar acciones (login, fetch usuarios, ejecutar prueba concurrente).
   - Usa `viewModelScope` para lanzar coroutines en hilos adecuados.
3. **Repository:**
   - Unifica el acceso a datos.
   - Comunica el ViewModel con la capa de red (`ApiService`).
4. **Services / Network:**
   - Configuración de `Retrofit` y `OkHttpClient`.
   - `AuthInterceptor`: Inyecta el header `Authorization: Bearer <TOKEN>` guardado en sesión de forma transparente.
5. **Models / DTOs:**
   - Data classes de Kotlin serializables con Gson o Kotlinx Serialization.

### 3.2 Almacenamiento Seguro de Sesión
- `SessionManager`: Almacena el JWT en `EncryptedSharedPreferences` (o `DataStore Preferences`).
- Se asegura de no quemar tokens ni credenciales en código fuente.

---

## 4. Requisito Central de Concurrencia y Paralelismo (Evaluación)

### 4.1 Consumo Simultáneo de Servicios al Entrar al Dashboard
Al abrir la pantalla principal (Dashboard), la app móvil no realiza llamadas secuenciales esperando una tras otra. Se ejecutan simultáneamente con Coroutines:

```kotlin
// En DashboardViewModel.kt
fun loadDashboardData() {
    viewModelScope.launch {
        _uiState.value = DashboardUiState.Loading
        try {
            // Se lanzan concurrentemente en Dispatchers.IO
            val profileDeferred = async(Dispatchers.IO) { repository.getProfile() }
            val statsDeferred = async(Dispatchers.IO) { repository.getStats() }
            val usersDeferred = async(Dispatchers.IO) { repository.getUsers() }

            // Se esperan todos en paralelo
            val profile = profileDeferred.await()
            val stats = statsDeferred.await()
            val users = usersDeferred.await()

            _uiState.value = DashboardUiState.Success(profile, stats, users)
        } catch (e: Exception) {
            _uiState.value = DashboardUiState.Error(e.localizedMessage)
        }
    }
}
```

### 4.2 Proceso Práctico de Comparación: Secuencial vs. Concurrente / Paralelo
Se implementará una pantalla dedicada de **Benchmark y Procesamiento**:
- El usuario selecciona un conjunto de archivos/imágenes (ej. 5 fotos) o un lote de cálculos/datos pesados.
- Dispone de dos opciones para ejecutar:

#### Versión Secuencial (Paso a paso)
```kotlin
fun runSequentialUpload(files: List<File>) {
    viewModelScope.launch {
        val totalTime = measureTimeMillis {
            for (file in files) {
                // Se espera a que termine una para iniciar la siguiente
                repository.uploadFileSync(file)
            }
        }
        _sequentialTimeState.value = totalTime / 1000.0 // segundos
    }
}
```

#### Versión Concurrente / Paralela
```kotlin
fun runConcurrentUpload(files: List<File>) {
    viewModelScope.launch {
        val totalTime = measureTimeMillis {
            // Se disparan todas concurrentemente en el pool de Dispatchers.IO
            files.map { file ->
                async(Dispatchers.IO) {
                    repository.uploadFileSync(file)
                }
            }.awaitAll()
        }
        _concurrentTimeState.value = totalTime / 1000.0 // segundos
    }
}
```

### 4.3 Visualización de Resultados en la App Móvil
La pantalla mostrará un panel interactivo con:
- ⏱ **Tiempo Secuencial:** e.g. `8.42 s`
- ⚡ **Tiempo Concurrente:** e.g. `2.15 s`
- 📊 **Aceleración (Speedup):** e.g. `3.91x`
- Barra de progreso por cada tarea individual para ver visualmente cómo todas las tareas avanzan al mismo tiempo en el modo concurrente versus una por una en el secuencial.

---

## 5. Guía para la Explicación / Defensa en Clase

Para responder a las preguntas que el profesor evaluará:

1. **¿Qué proceso se paralelizó?**
   - El consumo simultáneo de endpoints del Dashboard (`/profile`, `/stats`, `/users`) y la subida/procesamiento concurrente de múltiples archivos en lote.
2. **¿Por qué puede ejecutarse concurrentemente?**
   - Porque las operaciones son independientes entre sí: la subida de una imagen no depende de los datos o resultado de la otra; no existen condiciones de carrera ni dependencias de datos mutuas.
3. **¿Qué mecanismo se utilizó?**
   - Kotlin Coroutines (`async`, `awaitAll`, `launch`) sobre `Dispatchers.IO`.
4. **¿Cuántas tareas / hilos se utilizaron?**
   - `Dispatchers.IO` utiliza un pool dinámico de hilos elástico que se ajusta según la carga de I/O (hasta 64 hilos o número de cores), permitiendo que múltiples peticiones HTTP ocurran en paralelo en la pila de red.
5. **Diferencia de rendimiento:**
   - La versión concurrente reduce el tiempo total prácticamente al tiempo de la tarea individual más lenta, en lugar de la suma acumulativa de todas las tareas.
6. **¿Cuándo NO es conveniente usar paralelismo?**
   - Cuando hay dependencias secuenciales estrictas (ej. no se puede pedir el perfil sin antes recibir el JWT del login).
   - Cuando el overhead de crear y sincronizar hilos o tareas supera el tiempo de ejecución del proceso.
   - En dispositivos o servidores con cuellos de botella severos de ancho de banda o límites de conexiones simultáneas.

---

## 6. Fases de Ejecución

- [ ] **Paso 1:** Reestructurar el backend actual a la carpeta `backend/` con base de datos SQLAlchemy, modelos `User` y `FileRecord`, y hashing con bcrypt.
- [ ] **Paso 2:** Implementar autenticación JWT y middleware de protección en FastAPI.
- [ ] **Paso 3:** Implementar endpoints CRUD de usuarios, `/upload`, `/dashboard/stats` y `/dashboard/profile`.
- [ ] **Paso 4:** Crear `Dockerfile` y `docker-compose.yml`, probando el despliegue con `docker compose up -d`.
- [ ] **Paso 5:** Inicializar el proyecto Android Kotlin en la carpeta `app_movil/` con estructura MVVM y dependencias (Retrofit, Coroutines, ViewModel).
- [ ] **Paso 6:** Implementar flujo de Login, almacenamiento seguro de JWT y CRUD de usuarios en la app móvil.
- [ ] **Paso 7:** Implementar la pantalla de Dashboard con consumo concurrente (`async`/`awaitAll`).
- [ ] **Paso 8:** Implementar la pantalla de Benchmark comparativo (Secuencial vs Concurrente) con medición de tiempos y visualización interactiva.
