# 🎓 Guía de Demostración y Evaluación en Clase

Este documento es tu **guion paso a paso** para la defensa y corrección del proyecto frente al profesor. Cubre en orden exacto cada uno de los 14 puntos exigidos en la rúbrica de evaluación.

---

## 📋 Resumen del Flujo de Demostración

| # | Requisito a Demostrar | Dónde se muestra |
|---|---|---|
| **1 y 2** | Ejecutar Backend y levantar Contenedores Docker | Terminal (PowerShell) / Docker Desktop |
| **3** | Registrar un usuario | Swagger UI / App Móvil |
| **4 y 5** | Realizar Login y Obtener JWT | Swagger UI / App Móvil |
| **6** | Consumir un endpoint protegido con Bearer Token | Swagger UI (`/dashboard/profile` o `/users`) |
| **7** | Ejecutar CRUD completo de usuarios | App Móvil (Pantalla "Gestión de Usuarios") |
| **8 y 9** | Subir imagen/archivo y mostrar referencia física/BD | App Móvil / Swagger (`POST /upload` y carpeta `uploads/`) |
| **10, 11 y 12** | Proceso Concurrente, Secuencial y Medición de Tiempos | App Móvil (Pantalla "Benchmark Concurrencia") |
| **13** | Explicar por qué puede ejecutarse concurrentemente | Sustentación verbal (Guion incluido abajo) |
| **14** | Explicar cuándo NO sería conveniente el paralelismo | Sustentación verbal (Guion incluido abajo) |

---

## 🚀 Paso a Paso Detallado para la Demostración

---

### Paso 1 y 2: Ejecutar el Backend y Levantar Contenedores Docker

**Acción en la terminal:**
1. Abre PowerShell en la raíz del proyecto (`E:\algoritmos paralelos\tarea 1`).
2. Muestra al profesor cómo levantas todo el ecosistema con un solo comando:
   ```powershell
   docker compose up -d
   ```
3. Muestra el estado saludable de los contenedores:
   ```powershell
   docker compose ps
   ```

**Qué mostrarle al profesor:**
- Verá los dos contenedores corriendo:
  - `paralelo_movil_db` (PostgreSQL 16 en puerto `5432` con estado `healthy`).
  - `paralelo_movil_api` (FastAPI en puerto `8000`).
- Abre en el navegador: **[http://localhost:8000/health](http://localhost:8000/health)** y verás:
  `{"status": "healthy", "service": "Paralelo Móvil API"}`.

---

### Paso 3: Registrar un Usuario

**Dónde mostrarlo:** En la App Móvil o en **[http://localhost:8000/docs](http://localhost:8000/docs)**.

**Acción:**
- En Swagger, despliega el endpoint **`POST /register`**, presiona **Try it out** y envía:
  ```json
  {
    "nombre": "Estudiante",
    "apellido": "Evaluacion",
    "email": "estudiante@utesa.edu",
    "password": "Password123",
    "foto": null
  }
  ```
- **Resultado visible:** Código `201 Created` con el ID generado y la fecha de creación. *(Menciona que la contraseña se encriptó con salt en `bcrypt` y no se guarda en texto plano)*.

---

### Paso 4 y 5: Realizar Login y Obtener el JWT

**Acción:**
- En Swagger, ve a **`POST /login`**, presiona **Try it out** y envía:
  ```json
  {
    "email": "estudiante@utesa.edu",
    "password": "Password123"
  }
  ```
- **Resultado visible:** Código `200 OK` con la respuesta:
  ```json
  {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "token_type": "bearer",
    "user": { ... }
  }
  ```
- **Qué explicar:** *"Aquí el servidor valida la contraseña contra el hash de la BD y genera un token JWT firmado criptográficamente con el algoritmo HS256 y nuestra SECRET_KEY"*.

---

### Paso 6: Consumir un Endpoint Protegido con el JWT

**Acción:**
1. En Swagger, sube al botón superior verde **Authorize 🔓** y pega el token JWT obtenido en el paso anterior. Haz clic en **Authorize** y luego **Close**.
2. Despliega **`GET /dashboard/profile`** y haz clic en **Execute**.
3. **Resultado visible:** Código `200 OK` retornando el perfil del usuario autenticado.
4. *(Opcional para impresionar al profesor)*: Desautoriza el token o prueba sin cabecera y muestra que la API rechaza la petición con código `401 Unauthorized` (`detail: "Not authenticated"`).

---

### Paso 7: Ejecutar el CRUD de Usuarios

**Dónde mostrarlo:** En la **Aplicación Móvil Android**.

**Acción:**
1. Abre la app en el emulador o celular y haz login con `admin@paralelo.com` y `admin123`.
2. En el Dashboard, presiona el botón **"Gestión de Usuarios (CRUD)"**.
3. **Listar (Read):** Muestra la lista de usuarios cargada desde la base de datos PostgreSQL.
4. **Crear (Create):** Toca el botón flotante **`+`**, llena los campos (observa que la contraseña está oculta con el ícono del ojo) y presiona **Crear**.
5. **Actualizar (Update):** Toca el ícono del lápiz ✏️ en cualquier usuario, cambia su nombre y presiona **Guardar**.
6. **Eliminar (Delete):** Toca el ícono de la papelera 🗑️ en un usuario y confirma la eliminación.

---

### Paso 8 y 9: Subir una Imagen/Documento y Mostrar su Referencia

**Acción:**
1. En la misma pantalla de **Gestión de Usuarios**, toca el botón **`+`** o el lápiz ✏️ de editar.
2. Toca el círculo del avatar con el ícono de la cámara 📷.
3. Se abrirá el **selector nativo de la galería de Android**. Elige una imagen.
4. Presiona **Crear** o **Guardar**.

**Qué mostrarle al profesor:**
1. **En la App:** La foto seleccionada se subió por *multipart* a `POST /upload` y ahora aparece recortada en círculo como foto de perfil del usuario.
2. **En la Base de Datos / Swagger:** Ejecuta `GET /files` en Swagger para mostrar el registro:
   ```json
   {
     "id": 1,
     "filename": "7a9f1234_perfil.jpg",
     "url": "/uploads/7a9f1234_perfil.jpg"
   }
   ```
3. **En el disco físico:** Abre en tu explorador de Windows la carpeta:
   `E:\algoritmos paralelos\tarea 1\uploads\`
   y muéstrale que el archivo físico está guardado ahí con su nombre único UUID.

---

### Paso 10, 11 y 12: Demostración de Concurrencia vs. Secuencial y Tiempos

**Dónde mostrarlo:** En la App Móvil, pantalla **"Benchmark Concurrencia"**.

**Acción:**
1. En la app móvil, ve a la pantalla **"Demostración Concurrencia (Secuencial vs Paralelo)"**.
2. Verás el lote de tareas/archivos independientes listos para subirse a la API.

3. **Demostración Secuencial (Paso 11):**
   - Presiona el botón amarillo **"Secuencial"**.
   - Observa cómo las tareas se van ejecutando **una por una en fila** (la tarea 2 espera a que termine la 1, la 3 espera a la 2, etc.).
   - Muestra el tiempo final medido (ejemplo: **~6.8 a 8.5 segundos**).

4. **Demostración Concurrente / Paralela (Paso 10):**
   - Presiona el botón verde **"Concurrente"**.
   - Observa cómo **todas las tareas cambian a "En ejecución..." al mismo tiempo**.
   - Muestra el tiempo final medido (ejemplo: **~1.8 a 2.5 segundos**).

5. **Mostrar los Tiempos y Aceleración (Paso 12):**
   - Señala la tarjeta comparativa central en la pantalla donde se muestra:
     - ⏱ **Tiempo Secuencial:** `~7.6 s`
     - ⚡ **Tiempo Concurrente:** `~1.9 s`
     - 🚀 **Aceleración (Speedup):** `⚡ ~3.9x Más Rápido`.

---

### Paso 13: Explicar por qué el proceso puede ejecutarse concurrentemente

**Qué decirle al profesor (Tu respuesta técnica):**

> *"Profesor, este proceso puede paralelizarse eficientemente por tres razones técnicas fundamentales:*
>
> 1. * **Desacoplamiento de Datos (No hay dependencia):** Cada archivo o endpoint es completamente independiente; la subida del archivo 2 no necesita ningún dato de retorno del archivo 1, por lo que no existen condiciones de carrera (*race conditions*).
> 2. * **Naturaleza I/O-Bound (Entrada y Salida de Red):** En llamadas de red, la mayor parte del tiempo la CPU está ociosa esperando los paquetes TCP/HTTP. Al usar concurrencia, en lugar de esperar en serie, la aplicación despacha todas las peticiones a la capa de red del sistema operativo a la vez.
> 3. * **Mecanismo en Kotlin:** Usamos **Kotlin Coroutines** con el despachador **`Dispatchers.IO`**, utilizando `async` y `awaitAll()`. `Dispatchers.IO` gestiona un pool dinámico y elástico de hilos (hasta 64 hilos) que libera los recursos inmediatamente mientras espera la respuesta del servidor."*

---

### Paso 14: Explicar cuándo NO sería conveniente utilizar paralelismo

**Qué decirle al profesor (Tu respuesta técnica):**

> *"El paralelismo no siempre es conveniente y puede perjudicar el rendimiento en las siguientes situaciones:*
>
> 1. * **Dependencias Secuenciales Estrictas:** Si una operación requiere el resultado de la anterior como precondición obligatoria. Por ejemplo, en el login: no podemos paralelizar la petición de login con la de obtener el perfil, porque sin el JWT del login el perfil devolverá `401 Unauthorized`.
> 2. * **Tareas Extremadamente Pequeñas (Overhead de Sincronización):** Crear, planificar y sincronizar hilos o corrutinas tiene un costo computacional (*context switching*). Si una tarea toma microsegundos, el tiempo que toma crear el hilo supera al tiempo de ejecutarla en serie.
> 3. * **Saturación del Ancho de Banda o Hardware:** En conexiones móviles lentas (3G/2G) o dispositivos de gama baja, disparar demasiadas peticiones simultáneas genera congestión de red, paquetes perdidos y retransmisiones, haciendo que el tiempo total sea peor que el secuencial.
> 4. * **Límites de Conexión del Servidor:** Saturar el servidor con demasiadas peticiones paralelas concurrentes puede agotar el pool de conexiones de la base de datos o provocar errores `429 Too Many Requests` o `503 Service Unavailable`."*

---

## 🏁 Fin de la Demostración
Con estos pasos habrás cumplido y demostrado cada uno de los 14 puntos de la rúbrica de evaluación de Algoritmos Paralelos.
