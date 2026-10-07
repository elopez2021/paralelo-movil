import os
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from starlette.concurrency import run_in_threadpool

from backend.app.core.config import UPLOAD_DIR
from backend.app.database.session import engine, Base, SessionLocal
from backend.app.models.user import User
from backend.app.models.file_record import FileRecord
from backend.app.core.security import get_password_hash
from backend.app.routers import auth, users, files, dashboard
from backend.app.services.computation import run_sequential, run_concurrent

# Crear tablas en la base de datos
Base.metadata.create_all(bind=engine)


# Crear usuario inicial por defecto si no existe
def seed_default_user():
    db = SessionLocal()
    try:
        existing = db.query(User).filter(User.email == "admin@paralelo.com").first()
        if not existing:
            default_user = User(
                nombre="Usuario",
                apellido="Demo",
                email="admin@paralelo.com",
                password=get_password_hash("admin123"),
                foto=None
            )
            db.add(default_user)
            db.commit()
    finally:
        db.close()


seed_default_user()

app = FastAPI(
    title="API Paralelo Móvil",
    description=(
        "API REST para Algoritmos Paralelos. "
        "Incluye autenticación JWT, CRUD de usuarios, subida de archivos "
        "y procesamiento concurrente para consumo desde Android Kotlin (MVVM)."
    ),
    version="2.0.0",
)

# Configuración de CORS para acceso desde emulador Android (10.0.2.2) y dispositivos físicos
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Montar directorio de subidas estáticas
app.mount("/uploads", StaticFiles(directory=UPLOAD_DIR), name="uploads")

# Registrar routers
app.include_router(auth.router)
app.include_router(users.router)
app.include_router(files.router)
app.include_router(dashboard.router)


@app.get("/", tags=["Info"])
def read_root():
    return {
        "message": "API REST Paralelo Móvil",
        "version": "2.0.0",
        "docs": "/docs",
        "endpoints": {
            "login": "POST /login",
            "register": "POST /register",
            "users": "GET, POST /users",
            "upload": "POST /upload",
            "dashboard_profile": "GET /dashboard/profile",
            "dashboard_stats": "GET /dashboard/stats",
            "dashboard_notifications": "GET /dashboard/notifications",
            "sequential_computation": "GET /api/proceso/secuencial",
            "concurrent_computation": "GET /api/proceso/concurrente",
        },
    }


@app.get("/health", tags=["Info"])
def health_check():
    return {"status": "healthy", "service": "Paralelo Móvil API"}


# Endpoints heredados de Tarea 1 para compatibilidad total
@app.get("/api/proceso/secuencial", tags=["Heredado Tarea 1"])
async def proceso_secuencial():
    result = await run_in_threadpool(run_sequential)
    return result


@app.get("/api/proceso/concurrente", tags=["Heredado Tarea 1"])
async def proceso_concurrente():
    result = await run_in_threadpool(run_concurrent)
    return result


if __name__ == "__main__":
    import uvicorn
    port = int(os.getenv("PORT", 8000))
    uvicorn.run("backend.app.main:app", host="0.0.0.0", port=port, reload=True)
