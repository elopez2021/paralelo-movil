import os
import psutil
from datetime import datetime, timezone
from typing import List
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from starlette.concurrency import run_in_threadpool

from backend.app.database.session import get_db
from backend.app.models.user import User
from backend.app.models.file_record import FileRecord
from backend.app.schemas.user import UserResponse
from backend.app.schemas.dashboard import SystemStats, NotificationItem
from backend.app.routers.auth import get_current_user
from backend.app.services.computation import run_sequential, run_concurrent

router = APIRouter(prefix="/dashboard", tags=["Dashboard"])


@router.get("/profile", response_model=UserResponse)
def get_profile(current_user: User = Depends(get_current_user)):
    """Retorna el perfil del usuario autenticado."""
    return current_user


@router.get("/stats", response_model=SystemStats)
def get_system_stats(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    """Retorna métricas del sistema y base de datos."""
    total_users = db.query(User).count()
    total_files = db.query(FileRecord).count()
    
    process = psutil.Process(os.getpid())
    mem_info = process.memory_info().rss / (1024 * 1024)

    return {
        "total_users": total_users,
        "total_files": total_files,
        "system_status": "OPERATIONAL",
        "active_threads": process.num_threads() if hasattr(process, "num_threads") else 1,
        "memory_usage_mb": round(mem_info, 2),
    }


@router.get("/notifications", response_model=List[NotificationItem])
def get_notifications(current_user: User = Depends(get_current_user)):
    """Retorna notificaciones de actividad para el dashboard."""
    now_str = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S")
    return [
        {
            "id": 1,
            "title": "Bienvenido a Paralelo Móvil",
            "message": f"Hola {current_user.nombre}, has iniciado sesión correctamente.",
            "timestamp": now_str,
            "type": "info"
        },
        {
            "id": 2,
            "title": "Motor Concurrente Activo",
            "message": "Los subprocesos y workers de la API están listos para procesamiento paralelo.",
            "timestamp": now_str,
            "type": "success"
        },
        {
            "id": 3,
            "title": "Almacenamiento Local",
            "message": "Directorio de archivos montado y disponible para subidas masivas.",
            "timestamp": now_str,
            "type": "info"
        }
    ]


@router.get("/computation/sequential")
async def computation_sequential():
    """Ejecuta el cálculo de divisores en modo secuencial."""
    result = await run_in_threadpool(run_sequential)
    return result


@router.get("/computation/concurrent")
async def computation_concurrent(threads: int = 4):
    """Ejecuta el cálculo de divisores particionado en subprocesos concurrentes."""
    result = await run_in_threadpool(run_concurrent, num_threads=threads)
    return result
