# Índice de Documentación - Proyecto Algoritmos Paralelos

Esta carpeta contiene la documentación formal requerida en el apartado **17. Entrega** para la sustentación y evaluación en Nube UTESA:

1. 🏛️ **[1. Arquitectura del Sistema](arquitectura.md):**
   - Diagrama cliente-servidor con Mermaid.
   - Arquitectura MVVM completa en Android Kotlin (View, ViewModel, Repository, Services/Retrofit, DTOs).
   - Arquitectura modular de FastAPI y esquemas de base de datos relacional.
   - Dockerización y contenedores.

2. 🔌 **[2. Catálogo de Endpoints](endpoints.md):**
   - Endpoints de autenticación JWT (`POST /login`, `POST /register`).
   - CRUD de usuarios (`GET`, `POST`, `PUT`, `DELETE /users`).
   - Módulo de subida de archivos multipart (`POST /upload`, `DELETE /upload/{id}`).
   - Endpoints de consumo concurrente para el Dashboard (`/dashboard/profile`, `/dashboard/stats`, etc.).

3. ⚡ **[3. Explicación de Concurrencia y Paralelismo](explicacion_concurrencia.md):**
   - Procesos paralelizados y justificación técnica.
   - Mecanismos de Kotlin Coroutines (`Dispatchers.IO`, `async`, `awaitAll`).
   - Administración del pool de hilos y cuándo NO usar paralelismo.

4. 📊 **[4. Comparación de Tiempos y Rendimiento](comparacion_tiempos.md):**
   - Mediciones empíricas: Secuencial (~7.62 s) vs. Concurrente (~1.94 s).
   - Cálculo del factor de aceleración (*Speedup* $\approx 3.93\times$) y eficiencia.
   - Análisis teórico bajo la Ley de Amdahl.
