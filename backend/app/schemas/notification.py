from pydantic import BaseModel, EmailStr, Field
from typing import Optional


class NotificationSendRequest(BaseModel):
    email: EmailStr = Field(..., description="Correo electrónico del destinatario", example="usuario@correo.com")
    subject: str = Field(..., min_length=1, max_length=200, description="Asunto del correo", example="Prueba PUB/SUB")
    message: str = Field(..., min_length=1, description="Cuerpo del mensaje", example="Hola desde GCP Serverless")


class NotificationSendResponse(BaseModel):
    status: str = Field(default="success", description="Estado de la publicación")
    message: str = Field(..., description="Mensaje informativo para el cliente")
    messageId: Optional[str] = Field(default=None, description="ID del mensaje asignado por Google Cloud Pub/Sub")
