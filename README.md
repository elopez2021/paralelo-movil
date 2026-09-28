# Paralelo Móvil - Backend & Concurrencia

Proyecto para la asignatura de **Algoritmos Paralelos**. Contiene una API REST desarrollada en **FastAPI** (Python) orientada al procesamiento concurrente y secuencial, integrando autenticación, CRUD y servicios móviles.

## Características
- **FastAPI**: Framework web asíncrono y de alto rendimiento.
- **Procesamiento Concurrente vs Secuencial**: Comparativa de tiempos de ejecución mediante subprocesos y tareas asíncronas.
- **Preparado para Aplicación Móvil**: Conexión con frontend móvil nativo/multiplataforma con arquitectura MVVM.

## Requisitos y Ejecución
1. Crear y activar entorno virtual:
   ```bash
   python -m venv .venv
   .venv\Scripts\activate   # En Windows
   ```
2. Instalar dependencias:
   ```bash
   pip install -r requirements.txt
   ```
3. Ejecutar la API:
   ```bash
   uvicorn main:app --reload
   ```
4. Documentación interactiva Swagger:
   - `http://localhost:8000/docs`
