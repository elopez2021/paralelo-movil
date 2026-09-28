# Backend REST API - FastAPI (Algoritmos Paralelos)

API REST desarrollada en **FastAPI** (Python 3.11/3.12) para dar soporte al cliente móvil Android, integrando autenticación JWT, CRUD de usuarios, gestión de archivos y consumo concurrente.

---

## 🛠️ Tecnologías Utilizadas
- **Lenguaje:** Python
- **Framework Web:** FastAPI (Asíncrono, OpenAPI / Swagger automático)
- **Servidor ASGI:** Uvicorn
- **Base de Datos & ORM:** SQLAlchemy (Soporte dual: SQLite y PostgreSQL)
- **Seguridad:** Tokens JWT (`PyJWT`), Hashing de contraseñas con sal (`bcrypt`)
- **Gestión de Archivos:** `python-multipart` con almacenamiento en disco
- **Concurrencia Backend:** `ThreadPoolExecutor`, `as_completed`, `run_in_threadpool`
- **Contenedores:** Docker y Docker Compose

---

## 🚀 Instrucciones de Ejecución

### Opción 1: Con Docker Compose (Recomendado para evaluación)
Desde la raíz del proyecto o desde esta carpeta:
```bash
docker compose up -d
```
- La API estará disponible en `http://localhost:8000`.
- Documentación Swagger: `http://localhost:8000/docs`.

### Opción 2: Ejecución Local en Python
```bash
# 1. Crear entorno virtual (si no existe)
python -m venv .venv

# 2. Activar entorno virtual
.venv\Scripts\activate   # Windows

# 3. Instalar dependencias
pip install -r requirements.txt

# 4. Iniciar servidor (0.0.0.0 permite acceso desde móvil en la misma red)
uvicorn backend.app.main:app --reload --host 0.0.0.0 --port 8000
```

---

## 🔐 Credenciales Precargadas
Al iniciar por primera vez, el sistema genera automáticamente un usuario inicial:
- **Email:** `admin@paralelo.com`
- **Contraseña:** `admin123`

---

## 📌 Endpoints Principales

| Método | Endpoint | Descripción | Requiere JWT |
|---|---|---|---|
| `POST` | `/login` | Autentica usuario y retorna Token JWT | No |
| `POST` | `/register` | Registra un nuevo usuario | No |
| `GET` | `/users` | Lista todos los usuarios | Sí |
| `GET` | `/users/{id}` | Detalle de un usuario | Sí |
| `POST` | `/users` | Crear usuario | Sí |
| `PUT` | `/users/{id}` | Actualizar datos de usuario | Sí |
| `DELETE` | `/users/{id}` | Eliminar usuario | Sí |
| `POST` | `/upload` | Subir archivo multipart (imágenes, PDF) | Sí |
| `DELETE` | `/upload/{id}` | Eliminar archivo | Sí |
| `GET` | `/dashboard/profile` | Perfil del usuario autenticado | Sí |
| `GET` | `/dashboard/stats` | Estadísticas del sistema y BD | Sí |
| `GET` | `/dashboard/notifications` | Notificaciones del sistema | Sí |
| `GET` | `/api/proceso/secuencial` | Cálculo pesado secuencial (divisores) | No |
| `GET` | `/api/proceso/concurrente` | Cálculo pesado concurrente particionado | No |
