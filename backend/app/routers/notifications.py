from fastapi import APIRouter, HTTPException, status
from backend.app.schemas.notification import NotificationSendRequest, NotificationSendResponse
from backend.app.services.pubsub_service import publish_notification_to_pubsub

router = APIRouter(prefix="/notifications", tags=["Notificaciones & PubSub"])


@router.post("/send", response_model=NotificationSendResponse, status_code=status.HTTP_200_OK)
def send_notification(request: NotificationSendRequest):
    """
    Endpoint para encolar el envío de una notificación por correo electrónico.
    Publica el mensaje en Google Cloud Pub/Sub de forma desacoplada y asíncrona.
    """
    try:
        message_id = publish_notification_to_pubsub(
            email=str(request.email),
            subject=request.subject,
            message=request.message
        )

        return NotificationSendResponse(
            status="success",
            message="Mensaje enviado correctamente.",
            messageId=message_id
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error al enviar mensaje a Pub/Sub: {str(e)}"
        )
