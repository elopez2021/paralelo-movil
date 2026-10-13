import os
import json
import base64
import logging
from typing import Optional, Dict, Any
from fastapi import FastAPI, Request, Response, status
from pydantic import BaseModel

from mailer import send_email_notification, logger

app = FastAPI(
    title="Notification Cloud Run",
    description="Microservicio receptor de eventos de Google Cloud Pub/Sub para el envío de notificaciones por correo.",
    version="1.0.0"
)


class PubSubMessage(BaseModel):
    data: Optional[str] = None
    messageId: Optional[str] = "unknown"
    publishTime: Optional[str] = None


class PubSubPushPayload(BaseModel):
    message: Optional[PubSubMessage] = None
    subscription: Optional[str] = None


@app.get("/")
@app.get("/health")
def health_check():
    """Health check para Cloud Run."""
    return {
        "status": "healthy",
        "service": "notification-cloud-run",
        "version": "1.0.0"
    }


@app.post("/")
async def handle_pubsub_push(request: Request):
    """
    Endpoint invocado automáticamente por la Suscripción Push de Google Cloud Pub/Sub.
    También soporta invocación directa con payload plano para pruebas manuales.
    """
    try:
        body_bytes = await request.body()
        if not body_bytes:
            logger.warning("Petición vacía recibida en Cloud Run.")
            return Response(status_code=status.HTTP_200_OK, content="Empty body acknowledged")

        raw_json: Dict[str, Any] = json.loads(body_bytes.decode("utf-8"))

        email = ""
        subject = ""
        message = ""
        message_id = "direct-call"

        # CASO 1: Formato estándar de Google Cloud Pub/Sub Push
        if "message" in raw_json and isinstance(raw_json["message"], dict):
            pubsub_msg = raw_json["message"]
            message_id = pubsub_msg.get("messageId", "pubsub-id-unknown")
            b64_data = pubsub_msg.get("data", "")

            if b64_data:
                try:
                    decoded_text = base64.b64decode(b64_data).decode("utf-8")
                    data_obj = json.loads(decoded_text)
                    email = data_obj.get("email", "")
                    subject = data_obj.get("subject", "")
                    message = data_obj.get("message", "")
                except Exception as decode_err:
                    logger.error(f"Error decodificando payload base64 de Pub/Sub: {decode_err}")
            else:
                logger.warning("Mensaje de Pub/Sub sin datos en el campo 'data'.")

        # CASO 2: Llamada directa JSON (ej: prueba manual o Swagger)
        elif "email" in raw_json:
            email = raw_json.get("email", "")
            subject = raw_json.get("subject", "Notificación")
            message = raw_json.get("message", "")

        # Validar campos mínimos
        if not email or not message:
            logger.warning(f"Datos insuficientes para enviar notificación. Email: '{email}', Mensaje: '{message}'.")
            # Respondemos 200 para dar ACK a Pub/Sub y evitar reintentos con datos inválidos
            return {
                "status": "SKIPPED",
                "detail": "Email o mensaje vacíos.",
                "messageId": message_id
            }

        # Procesar y enviar el correo / registrar en Cloud Logging
        result = send_email_notification(
            email=email,
            subject=subject or "Notificación de Paralelo Móvil",
            message=message,
            message_id=message_id
        )

        return {
            "status": "SUCCESS",
            "messageId": message_id,
            "result": result
        }

    except Exception as e:
        logger.error(f"Error inesperado procesando la notificación: {str(e)}", exc_info=True)
        # Se retorna 200 para que Pub/Sub no entre en bucle infinito si hay un mensaje corrupto
        return Response(
            status_code=status.HTTP_200_OK,
            content=json.dumps({"status": "ERROR_ACKNOWLEDGED", "detail": str(e)}),
            media_type="application/json"
        )


if __name__ == "__main__":
    import uvicorn
    port = int(os.getenv("PORT", "8080"))
    uvicorn.run("main:app", host="0.0.0.0", port=port, reload=True)
