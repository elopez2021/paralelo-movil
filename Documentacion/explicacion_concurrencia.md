# Explicación Técnica de Concurrencia y Paralelismo

Este documento contiene la fundamentación teórica y técnica requerida para la sustentación y defensa del proyecto en la materia de **Algoritmos Paralelos**.

---

## 1. ¿Qué procesos se paralelizaron en la aplicación?

Se implementaron **dos niveles clave de concurrencia** en la aplicación móvil Android:

1. **Consumo Simultáneo del Dashboard (Nivel de Servicio):**
   Al ingresar al Dashboard, en lugar de encadenar llamadas síncronas que bloqueen la pantalla, se ejecutan de manera simultánea 4 peticiones HTTP independientes:
   - `GET /dashboard/profile` (Perfil del usuario)
   - `GET /dashboard/stats` (Estadísticas de la BD y memoria)
   - `GET /dashboard/notifications` (Notificaciones del sistema)
   - `GET /users` (Lista inicial de usuarios)

2. **Procesamiento y Subida en Lote de Archivos (Nivel de Benchmark):**
   Un lote de 5 archivos independientes (datasets, reportes, imágenes, logs) que se procesan y suben a la API REST. Se implementó tanto la versión **secuencial** (uno tras otro en fila) como la versión **concurrente/paralela** (todos disparados a la vez).

---

## 2. ¿Por qué pueden ejecutarse concurrentemente?

Las operaciones elegidas son candidatas ideales para paralelización porque cumplen con los siguientes principios:

- **Desacoplamiento de Datos (No Data Dependency):**
  La petición para obtener las estadísticas del servidor (`/stats`) no necesita ningún dato que retorne la petición de notificaciones (`/notifications`) ni la de usuarios (`/users`). Ninguna es precondición de la otra.
- **Naturaleza I/O-Bound (Entrada/Salida de Red):**
  La mayor parte del tiempo transcurre esperando que los paquetes viajen por la red y que el servidor responda (latencia de red y procesamiento I/O). En lugar de que el procesador móvil permanezca ocioso esperando la respuesta 1 para recién enviar la 2, se envían todas las solicitudes a la capa de red del sistema operativo simultáneamente.
- **Inexistencia de Condiciones de Carrera (Race Conditions):**
  Cada tarea de subida crea un registro y un archivo independiente en el servidor con un identificador único (UUID), sin competir por recursos compartidos mutables.

---

## 3. ¿Qué mecanismo se utilizó en Android Kotlin?

Se utilizaron **Kotlin Coroutines** estructuradas sobre el despachador de entrada y salida (`Dispatchers.IO`):

### Mecanismos Clave:
- **`CoroutineScope` (`viewModelScope`):**
  Garantiza concurrencia estructurada (*structured concurrency*). Si el usuario sale de la pantalla, las corrutinas se cancelan automáticamente evitando fugas de memoria (*memory leaks*).
- **`async(Dispatchers.IO) { ... }`:**
  Inicia una corrutina asíncrona que retorna una promesa de valor futuro (`Deferred<T>`). No bloquea el hilo llamador ni la interfaz de usuario.
- **`awaitAll()`:**
  Suspende la ejecución hasta que todas las tareas en paralelo del lote hayan completado su ejecución, reuniendo todos los resultados en un solo paso.
- **`measureTimeMillis { ... }`:**
  Bloque de medición de tiempo de alta resolución en milisegundos para comparar matemáticamente el rendimiento de ambos métodos.

---

## 4. ¿Cuántas tareas e hilos se utilizaron?

- **Número de Tareas Concurrente:** 4 tareas simultáneas en el Dashboard y 5 tareas en el Benchmark de archivos.
- **Gestión de Hilos (`Dispatchers.IO`):**
  A diferencia de los hilos tradicionales pesados de Java (`Thread`), las Coroutines son hilos virtuales ultraligeros (*green threads/lightweight threads*). 
  `Dispatchers.IO` está respaldado por un **pool elástico de hilos** optimizado para operaciones de red y disco:
  - Comparte hilos con `Dispatchers.Default` pero permite crecer dinámicamente hasta **64 hilos** (o el número de núcleos de la CPU, el que sea mayor).
  - Cuando una corrutina se suspende esperando la respuesta HTTP del servidor, el hilo subyacente se libera inmediatamente para atender a otra corrutina, maximizando el uso de la CPU y reduciendo el overhead al mínimo.

---

## 5. Código Comparativo: Secuencial vs. Concurrente

### Versión Secuencial (Iterativa)
```kotlin
// Cada iteración bloquea el avance del bucle hasta que la petición anterior finalice
val tiempoSecuencial = measureTimeMillis {
    for (archivo in listaArchivos) {
        fileRepository.uploadBytes(archivo.name, archivo.bytes) // Espera respuesta
    }
}
```
*Tiempo total estimado:* $\sum_{i=1}^{n} T_i$ (Suma acumulada de todos los tiempos individuales).

### Versión Concurrente / Paralela (Coroutines)
```kotlin
// Todas las corrutinas se despachan a la vez en el pool de Dispatchers.IO
val tiempoConcurrente = measureTimeMillis {
    val tareas = listaArchivos.map { archivo ->
        async(Dispatchers.IO) {
            fileRepository.uploadBytes(archivo.name, archivo.bytes)
        }
    }
    tareas.awaitAll() // Espera que todas finalicen
}
```
*Tiempo total estimado:* $\max(T_1, T_2, \dots, T_n) + \epsilon$ (El tiempo de la tarea más lenta más un pequeño overhead de sincronización).

---

## 6. ¿En qué situaciones NO sería conveniente utilizar paralelismo?

Es indispensable comprender cuándo el paralelismo es contraproducente:

1. **Dependencia Secuencial Estricta de Datos:**
   Por ejemplo, el flujo de autenticación: no se puede consultar el perfil ni los usuarios sin haber completado primero el `POST /login` para obtener el JWT. Forzar concurrencia aquí generaría errores `401 Unauthorized`.
2. **Tareas Excesivamente Diminutas (Overhead > Cómputo):**
   Crear, planificar y sincronizar corrutinas o hilos introduce una sobrecarga (*overhead* de contexto y memoria). Si una operación toma 0.001 ms, el tiempo de crear la tarea paralela superará al tiempo de ejecutarla en serie.
3. **Cuellos de Botella de Hardware o Ancho de Banda Saturado:**
   Si la conexión de red del dispositivo móvil tiene un ancho de banda de subida muy limitado (ej. 2G/3G inestable), disparar 20 subidas a la vez provocará congestión de paquetes (*packet drops*), retransmisiones TCP y aumento en la latencia de todas las conexiones.
4. **Límites de Conexiones Simultáneas del Servidor:**
   Los servidores web o pools de bases de datos tienen un límite de conexiones concurrentes por IP. Un exceso de peticiones paralelas no controladas puede saturar el servidor o disparar errores `429 Too Many Requests` o `503 Service Unavailable`.
