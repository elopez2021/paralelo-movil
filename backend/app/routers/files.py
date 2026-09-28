import os
import shutil
import uuid
from typing import List
from fastapi import APIRouter, Depends, UploadFile, File, HTTPException, status
from sqlalchemy.orm import Session

from backend.app.database.session import get_db
from backend.app.models.file_record import FileRecord
from backend.app.schemas.file_record import FileUploadResponse
from backend.app.core.config import UPLOAD_DIR
from backend.app.routers.auth import get_current_user
from backend.app.models.user import User

router = APIRouter(prefix="", tags=["Archivos"])

ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp", ".pdf", ".txt", ".csv"}
MAX_FILE_SIZE = 15 * 1024 * 1024  # 15 MB


@router.post("/upload", response_model=FileUploadResponse, status_code=status.HTTP_201_CREATED)
async def upload_file(
    file: UploadFile = File(...),
    db: Session = Depends(get_db),
    # Optional JWT or require JWT: Let's allow authenticated users, or accept token if provided
    # To ease batch testing from mobile we can accept current_user
    current_user: User = Depends(get_current_user),
):
    if not file.filename:
        raise HTTPException(status_code=400, detail="Nombre de archivo inválido")

    ext = os.path.splitext(file.filename)[1].lower()
    if ext not in ALLOWED_EXTENSIONS:
        raise HTTPException(
            status_code=400,
            detail=f"Extensión '{ext}' no permitida. Permitidas: {', '.join(ALLOWED_EXTENSIONS)}"
        )

    # Generate unique filename to avoid collision
    unique_filename = f"{uuid.uuid4().hex[:10]}_{file.filename}"
    file_path = UPLOAD_DIR / unique_filename

    # Read content & validate size
    contents = await file.read()
    if len(contents) > MAX_FILE_SIZE:
        raise HTTPException(status_code=400, detail="El archivo excede el tamaño máximo de 15MB")

    with open(file_path, "wb") as f:
        f.write(contents)

    file_record = FileRecord(
        filename=unique_filename,
        original_name=file.filename,
        url=f"/uploads/{unique_filename}",
        mime_type=file.content_type,
        size=len(contents),
    )
    db.add(file_record)
    db.commit()
    db.refresh(file_record)

    return {
        "id": file_record.id,
        "filename": file_record.filename,
        "url": file_record.url,
        "original_name": file_record.original_name,
        "size": file_record.size,
        "createdAt": file_record.createdAt,
    }


@router.get("/files", response_model=List[FileUploadResponse])
def list_files(
    skip: int = 0,
    limit: int = 50,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    files = db.query(FileRecord).order_by(FileRecord.id.desc()).offset(skip).limit(limit).all()
    return files


@router.delete("/upload/{file_id}", status_code=status.HTTP_200_OK)
def delete_file(
    file_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    record = db.query(FileRecord).filter(FileRecord.id == file_id).first()
    if not record:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Archivo con id {file_id} no encontrado"
        )
    
    file_disk_path = UPLOAD_DIR / record.filename
    if os.path.exists(file_disk_path):
        try:
            os.remove(file_disk_path)
        except OSError:
            pass

    db.delete(record)
    db.commit()
    return {"message": f"Archivo {file_id} eliminado exitosamente", "id": file_id}
