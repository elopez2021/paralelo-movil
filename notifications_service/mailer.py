import os
import smtplib
import logging
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from datetime import datetime, timezone

# Configuracion de Logging para Google Cloud Logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s"
)
logger = logging.getLogger("notification-cloud-run")

SMTP_HOST = os.getenv("SMTP_HOST", "")
SMTP_PORT = int(os.getenv("SMTP_PORT", "587"))
SMTP_USER = os.getenv("SMTP_USER", "")
SMTP_PASSWORD = os.getenv("SMTP_PASSWORD", "")
SMTP_FROM = os.getenv("SMTP_FROM", SMTP_USER or "notificaciones@paralelo-movil.com")


def send_email_notification(email: str, subject: str, message: str, message_id: str = "N/A") -> dict:
    """
    Procesa y envia el correo electronico hacia el destinatario.
    Si se proporcionan credenciales SMTP (ej: Gmail / SendGrid), envia el correo real.
    Si no hay credenciales, registra el evento completo en Cloud Logging para la evidencia academica.
    """
    timestamp = datetime.now(timezone.utc).isoformat()

    logger.info("============================================================")
    logger.info("[PUB/SUB NOTIFICACION RECIBIDA EN CLOUD RUN]")
    logger.info(f"   -> Message ID GCP: {message_id}")
    logger.info(f"   -> Destinatario:   {email}")
    logger.info(f"   -> Asunto:         {subject}")
    logger.info(f"   -> Mensaje:        {message}")
    logger.info(f"   -> Timestamp UTC:  {timestamp}")
    logger.info("============================================================")

    # Si hay credenciales SMTP configuradas, intentamos el envio real
    if SMTP_HOST and SMTP_USER and SMTP_PASSWORD:
        try:
            msg = MIMEMultipart("alternative")
            msg["Subject"] = subject
            msg["From"] = SMTP_FROM
            msg["To"] = email

            html_body = f"""
            <html>
            <body style="font-family: Arial, sans-serif; background-color: #f4f6f8; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 8px; padding: 25px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                    <h2 style="color: #1a73e8; margin-top: 0;">Paralelo Movil - Notificacion</h2>
                    <p style="font-size: 16px; color: #333333;">{message}</p>
                    <hr style="border: none; border-top: 1px solid #e0e0e0; margin: 20px 0;" />
                    <p style="font-size: 12px; color: #888888;">
                        Enviado automaticamente por <strong>notification-cloud-run</strong> via Google Cloud Pub/Sub.<br />
                        Message ID: <code>{message_id}</code>
                    </p>
                </div>
            </body>
            </html>
            """
            msg.attach(MIMEText(message, "plain"))
            msg.attach(MIMEText(html_body, "html"))

            with smtplib.SMTP(SMTP_HOST, SMTP_PORT, timeout=10) as server:
                server.starttls()
                server.login(SMTP_USER, SMTP_PASSWORD)
                server.sendmail(SMTP_FROM, [email], msg.as_string())

            logger.info(f"[EXITO] Correo electronico enviado exitosamente via SMTP a {email}")
            return {
                "status": "DELIVERED",
                "mode": "REAL_SMTP",
                "recipient": email,
                "subject": subject,
                "messageId": message_id,
            }
        except Exception as e:
            logger.error(f"[SMTP ERROR] Error enviando correo via SMTP ({str(e)}). Registrado en logs.")
            return {
                "status": "LOGGED_FALLBACK",
                "mode": "FALLBACK_LOG",
                "error": str(e),
                "recipient": email,
                "subject": subject,
                "messageId": message_id,
            }

    # Modo simulacion / registro en Cloud Logging (cuando no hay SMTP configurado)
    logger.info("[MODO CLOUD LOGGING] No se configuro SMTP externo. Evento registrado como evidencia en Cloud Logging.")
    return {
        "status": "DELIVERED",
        "mode": "CLOUD_LOGGING",
        "recipient": email,
        "subject": subject,
        "messageId": message_id,
        "timestamp": timestamp,
    }
