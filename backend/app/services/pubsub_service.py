import os
import json
import uuid
import logging
from typing import Dict, Any

logger = logging.getLogger("backend-pubsub")

GCP_PROJECT_ID = os.getenv("GCP_PROJECT_ID", os.getenv("GOOGLE_CLOUD_PROJECT", "paralelo-movil-prod"))
PUBSUB_TOPIC_NAME = os.getenv("PUBSUB_TOPIC_NAME", "notifications-topic")

_publisher_client = None


def get_publisher_client():
    global _publisher_client
    if _publisher_client is None:
        try:
            from google.cloud import pubsub_v1
            _publisher_client = pubsub_v1.PublisherClient()
        except Exception as e:
            logger.warning(f"[PUBSUB CLIENT] No se pudo inicializar cliente Pub/Sub oficial ({e}). Se usará modo simulado.")
            return None
    return _publisher_client


def publish_notification_to_pubsub(email: str, subject: str, message: str) -> str:
    """
    Publica un mensaje asíncrono en el Topic de Google Cloud Pub/Sub.
    Si corre en Cloud Run con credenciales GCP, publica al Topic real.
    Si corre en entorno local sin credenciales, emite log y retorna un ID simulado.
    """
    payload: Dict[str, Any] = {
        "email": email,
        "subject": subject,
        "message": message
    }
    encoded_data = json.dumps(payload).encode("utf-8")

    client = get_publisher_client()

    if client and GCP_PROJECT_ID and PUBSUB_TOPIC_NAME:
        try:
            topic_path = client.topic_path(GCP_PROJECT_ID, PUBSUB_TOPIC_NAME)
            logger.info(f"[PUBSUB] Publicando mensaje en {topic_path} para {email}...")
            
            future = client.publish(topic_path, data=encoded_data)
            message_id = future.result(timeout=10)
            
            logger.info(f"[PUBSUB EXITO] Mensaje publicado exitosamente con ID: {message_id}")
            return str(message_id)
        except Exception as err:
            logger.error(f"[PUBSUB ERROR] Error publicando en GCP Pub/Sub ({err}). Aplicando fallback local...")

    # Fallback local / simulación si no hay credenciales GCP activas
    mock_id = f"mock-msg-{uuid.uuid4().hex[:8]}"
    logger.info(f"[PUBSUB SIMULADO LOCAL] Destinatario: {email} | Asunto: {subject} | ID: {mock_id}")
    return mock_id
