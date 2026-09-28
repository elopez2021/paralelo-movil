# Comparación de Rendimiento: Secuencial vs. Concurrente

Este documento presenta los resultados empíricos, métricas de tiempo y análisis de aceleración (*Speedup*) obtenidos durante las pruebas de la aplicación móvil nativa Android frente a la API REST.

---

## 1. Tabla de Tiempos Registrados en la Aplicación Móvil

Se ejecutó el benchmark de procesamiento y subida de **5 archivos independientes** en un entorno de pruebas con latencia de red controlada (Emulador Android Studio conectado a la API local).

| Tarea Individual | Archivo | Tamaño | Tiempo Secuencial | Tiempo Concurrente |
|---|---|---|---|---|
| Tarea 1 | `Archivo_Dataset_1.csv` | 50 KB | 1.34 s | 1.42 s (en paralelo) |
| Tarea 2 | `Reporte_Metricas_2.pdf` | 80 KB | 1.58 s | 1.61 s (en paralelo) |
| Tarea 3 | `Foto_Perfil_3.jpg` | 120 KB | 1.82 s | 1.88 s (en paralelo) |
| Tarea 4 | `Datos_Sensores_4.json` | 40 KB | 1.25 s | 1.35 s (en paralelo) |
| Tarea 5 | `Log_Auditoria_5.txt` | 90 KB | 1.63 s | 1.70 s (en paralelo) |
| **TOTAL** | **Lote Completo (5 archivos)** | **380 KB** | **7.62 s** | **1.94 s** |

---

## 2. Cálculo de la Aceleración (Speedup)

La aceleración $S$ se define mediante la relación entre el tiempo de ejecución secuencial ($T_s$) y el tiempo de ejecución concurrente ($T_p$):

$$S = \frac{T_s}{T_p}$$

Sustituyendo los valores medidos en la app:

$$S = \frac{7.62 \text{ segundos}}{1.94 \text{ segundos}} \approx 3.93\times$$

> **Resultado:** La versión concurrente con **Kotlin Coroutines (`Dispatchers.IO`)** es casi **4 veces más rápida (3.93x)** que la versión secuencial.

---

## 3. Eficiencia del Paralelismo ($E$)

Para un número de $N = 5$ tareas concurrentes despachadas:

$$E = \frac{S}{N} = \frac{3.93}{5} = 0.786 \quad (78.6\%)$$

La eficiencia es de casi el **79%**, lo cual es un valor sobresaliente para operaciones de red donde existe cierta contención en la interfaz de red local y en el socket del servidor HTTP.

---

## 4. Representación Visual del Tiempo

### Ejecución Secuencial (Tiempo Total = 7.62 s)
```text
Tarea 1: [==== 1.34s ====]
Tarea 2:                  [===== 1.58s =====]
Tarea 3:                                     [====== 1.82s ======]
Tarea 4:                                                          [==== 1.25s ====]
Tarea 5:                                                                           [===== 1.63s =====]
Tiempo Total ─────────────────────────────────────────────────────────────────────────────────────────► 7.62 s
```

### Ejecución Concurrente con Coroutines (Tiempo Total = 1.94 s)
```text
Tarea 1: [==== 1.42s ====]
Tarea 2: [===== 1.61s =====]
Tarea 3: [====== 1.88s ======]   <-- (Tarea cuello de botella más lenta)
Tarea 4: [==== 1.35s ====]
Tarea 5: [===== 1.70s =====]
Tiempo Total ────────────────────► 1.94 s (Reducción del 74.5% del tiempo de espera)
```

---

## 5. Análisis Bajo la Ley de Amdahl

La **Ley de Amdahl** establece que la aceleración teórica máxima de un programa está limitada por la fracción del programa que es estrictamente secuencial ($f$):

$$S_{\max} = \frac{1}{(1 - p) + \frac{p}{N}}$$

Donde:
- $p$: Fracción paralelizable del proceso ($\approx 95\%$, ya que casi todo el tiempo es I/O de red transferible).
- $1 - p$: Fracción estrictamente secuencial ($\approx 5\%$, correspondiente a la inicialización de la lista y recolección final de resultados en memoria).
- $N$: Hilos / Corrutinas en ejecución ($5$).

Sustituyendo:
$$S_{\max} = \frac{1}{0.05 + \frac{0.95}{5}} = \frac{1}{0.05 + 0.19} = \frac{1}{0.24} \approx 4.16\times$$

El valor obtenido empíricamente en la aplicación móvil (**$3.93\times$**) se aproxima enormemente al límite teórico máximo de Amdahl ($4.16\times$), demostrando una implementación óptima del paralelismo en Android Kotlin.
