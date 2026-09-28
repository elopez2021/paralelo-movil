from datetime import datetime
from typing import Optional
from pydantic import BaseModel


class FileUploadResponse(BaseModel):
    id: int
    filename: str
    url: str
    original_name: Optional[str] = None
    size: Optional[int] = None
    createdAt: Optional[datetime] = None

    class Config:
        from_attributes = True
