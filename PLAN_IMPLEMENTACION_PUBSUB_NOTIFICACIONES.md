# 📋 Plan de Implementación: Mensajería Asíncrona con GCP Cloud Run y Pub/Sub

> **Objetivo:** Implementar un flujo de mensajería desacoplado y reactivo utilizando **Google Cloud Pub/Sub** y un nuevo microservicio **Cloud Run (`notification-cloud-run`)** para el envío de correos electrónicos disparados desde la aplicación móvil Android.

---

## 🏛️ 1. Arquitectura y Flujo de Datos

```mermaid
flowchart TD
    subgraph Cliente["📱 Cliente Android"]
        A["App Móvil (Jetpack Compose)"]
    end

    subgraph Backend_Actual["🚀 Cloud Run: Backend Actual"]
        B["FastAPI (paralelo-api)"]
        B1["POST /notifications/send"]
    end

    subgraph Mensajeria["📬 Asynchronous Broker (GCP)"]
        C["Pub/Sub Topic: notifications-topic"]
        D["Push Subscription: notifications-push-sub"]
    end

    subgraph Microservicio_Notificaciones["✉️ Cloud Run: Notificaciones"]
        E["notification-cloud-run"]
        E1["Endpoint POST / (Decodificador Base64)"]
        E2["Servicio de Envío de Correo (SMTP / Mailer)"]
    end

    subgraph Destino_Y_Monitoreo["🌐 Destino & Observabilidad"]
        F["Buzón del Usuario (Email)"]
        G["Cloud Logging (Eventos y Auditoría)"]
    end

    A -->|"1. Envía {email, subject, message}|" B1
    B1 -->|"2. Publica mensaje JSON en menos de 50ms|" C
    B1 -.->|"3. Retorna 200 OK inmediatamente|" A
    C -->|"4. Enruta a la suscripción|" D
    D -->|"5. HTTP POST Push con token OIDC|" E1
    E1 -->|"6. Procesa información|" E2
    E2 -->|"7. Envía correo|" F
    E2 -->|"8. Registra evento estructurado|" G
```

---

## 📍 FASE 1: Microservicio de Notificaciones (`notifications_service/`)

*Microservicio serverless dedicado exclusivamente a consumir eventos de Pub/Sub y gestionar el envío de correos sin sobrecargar el backend transaccional.*

### 1.1 Estructura de Archivos
```text
notifications_service/
├── Dockerfile              # Imagen ligera Python 3.11-slim
├── requirements.txt        # fastapi, uvicorn, pydantic
├── main.py                 # Receptor del Push HTTP de Pub/Sub
└── mailer.py               # Lógica de envío SMTP y Cloud Logging
```

### 1.2 Responsabilidades Técnicas
1. **Endpoint Receptor (`POST /`):**
   * Google Cloud Pub/Sub en modo **Push** envía las peticiones con este esquema JSON estándar:
     ```json
     {
       "message": {
         "data": "eyjlbWFpbCI6ICJ1c3VhcmlvQGNvcnJlby5jb20iLCAic3ViamVjdCI6IC4uLn0=",
         "messageId": "1234567890",
         "publishTime": "2026-10-13T12:00:00Z"
       },
       "subscription": "projects/.../subscriptions/notifications-push-sub"
     }
     ```
   * El servicio decodifica el campo `message.data` en Base64 para extraer el objeto original:
     ```json
     {
       "email": "destinatario@correo.com",
       "subject": "Asunto del Correo",
       "message": "Cuerpo del mensaje"
     }
     ```
2. **Envío de Correo (`mailer.py`):**
   * Envío mediante el protocolo estándar SMTP (`smtplib` + `email.mime`).
   * Configurable por variables de entorno: `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASSWORD`.
   * **Modo Resiliente (Fallback):** Si no se proporcionan credenciales SMTP de producción en el entorno académico, formatea y simula el envío registrándolo en **Cloud Logging** y retorna código HTTP `200 OK` (ACK) para confirmar la entrega a Pub/Sub y evitar reintentos infinitos.
3. **Registro en Logger:**
   * Emite logs estructurados indicando `messageId`, `destinatario`, `asunto`, `timestamp` y estado de la entrega.

---

## 📍 FASE 2: Infraestructura con Terraform (`terraform/`)

*Automatización total de la infraestructura necesaria en Google Cloud mediante código declarativo.*

### 2.1 Modificaciones en `terraform/variables.tf`
* `notification_service_name`: Nombre del nuevo Cloud Run (por defecto `"notification-cloud-run"`).
* `pubsub_topic_name`: Nombre del topic (por defecto `"notifications-topic"`).
* `smtp_host`, `smtp_port`, `smtp_user`, `smtp_password`: Parámetros opcionales para el envío de correo.

### 2.2 Declaración de Recursos en `terraform/main.tf`
1. **Topic de Pub/Sub:**
   ```hcl
   resource "google_pubsub_topic" "notifications_topic" {
     name    = var.pubsub_topic_name
     project = var.project_id
   }
   ```
2. **Permiso de Publicación para el Backend:**
   * Asignar el rol `roles/pubsub.publisher` a la Service Account del backend actual (`paralelo-run-sa`):
   ```hcl
   resource "google_pubsub_topic_iam_member" "backend_publisher" {
     project = var.project_id
     topic   = google_pubsub_topic.notifications_topic.name
     role    = "roles/pubsub.publisher"
     member  = "serviceAccount:${google_service_account.cloud_run_sa.email}"
   }
   ```
3. **Service Account para la Suscripción Pub/Sub Push:**
   * Cuenta dedicada para invocar de forma segura al nuevo Cloud Run mediante tokens OIDC:
   ```hcl
   resource "google_service_account" "pubsub_invoker_sa" {
     account_id   = "pubsub-invoker-sa"
     display_name = "Service Account para Push de Pub/Sub a Cloud Run"
     project      = var.project_id
   }
   ```
4. **Microservicio Cloud Run de Notificaciones:**
   * Creación del servicio `notification-cloud-run` con scale-to-zero y variables de entorno inyectadas.
5. **Permisos de Invocación:**
   * Conceder el rol `roles/run.invoker` a `pubsub_invoker_sa` sobre el nuevo servicio de Cloud Run.
6. **Suscripción Pub/Sub en Modo Push:**
   * Conecta el topic con la URL de `notification-cloud-run` utilizando autenticación OIDC.

### 2.3 Salidas en `terraform/outputs.tf`
* `pubsub_topic_name`: Nombre del topic creado.
* `notification_service_url`: URL del microservicio de notificaciones.

---

## 📍 FASE 3: Integración en el Backend Actual (`backend/`)

*Añadir la capacidad de publicar mensajes asíncronos en Pub/Sub sin bloquear la respuesta al usuario.*

### 3.1 Dependencias
* Agregar `google-cloud-pubsub` al archivo `requirements.txt`.

### 3.2 Esquema de Validación (`backend/app/schemas/notification.py`)
```python
from pydantic import BaseModel, EmailStr

class NotificationSendRequest(BaseModel):
    email: EmailStr
    subject: str
    message: str

class NotificationSendResponse(BaseModel):
    status: str
    message: str
    messageId: str
```

### 3.3 Servicio Publicador (`backend/app/services/pubsub_publisher.py`)
* Instancia el cliente `pubsub_v1.PublisherClient()`.
* Serializa los datos a JSON, codifica a UTF-8 y publica en el topic configurado.
* Retorna el `message_id` generado por GCP.

### 3.4 Router del Backend (`backend/app/routers/notifications.py`)
* Implementar el endpoint:
  ```http
  POST /notifications/send
  ```
* **Lógica:**
  1. Valida el formato del email, asunto y mensaje con Pydantic.
  2. Publica el mensaje en Pub/Sub.
  3. Responde inmediatamente con HTTP `200 OK`:
     ```json
     {
       "status": "success",
       "message": "Mensaje enviado a la cola de procesamiento",
       "messageId": "1234567890"
     }
     ```

### 3.5 Mapeo en API Gateway (`terraform/openapi_spec.yaml`)
* Añadir la ruta `/notifications/send` para que las peticiones que ingresen por el Gateway se redirijan correctamente hacia Cloud Run.

---

## 📍 FASE 4: Integración en la App Móvil Android (`Mobile/`)

*Creación de una interfaz gráfica intuitiva construida en Jetpack Compose siguiendo la arquitectura MVVM.*

### 4.1 Capa de Datos (Data Layer)
1. **Modelos Kotlin:**
   ```kotlin
   data class NotificationRequest(
       val email: String,
       val subject: String,
       val message: String
   )

   data class NotificationResponse(
       val status: String,
       val message: String,
       val messageId: String? = null
   )
   ```
2. **Endpoint en Retrofit (`ApiService.kt`):**
   ```kotlin
   @POST("notifications/send")
   suspend fun sendNotification(
       @Body request: NotificationRequest
   ): Response<NotificationResponse>
   ```

### 4.2 Capa de Presentación (UI & ViewModel)
1. **`NotificationViewModel.kt`:**
   * Estados observables (`StateFlow`):
     * `email`: `String`
     * `subject`: `String`
     * `message`: `String`
     * `isLoading`: `Boolean`
     * `resultMessage`: `String?`
     * `isSuccess`: `Boolean?`
   * Función `sendNotification()`: Valida que los campos no estén vacíos, ejecuta la petición en `viewModelScope` y gestiona los mensajes de feedback.
2. **Pantalla Compose (`SendNotificationScreen.kt`):**
   * Formulario con diseño moderno:
     * Campo de texto con icono para el **Correo destino**.
     * Campo de texto para el **Asunto**.
     * Campo de texto expandible para el **Mensaje**.
     * Botón interactivo de **Enviar Notificación** con indicador de carga (`CircularProgressIndicator`).
   * Componente de alerta o Toast:
     * 🟢 *"Mensaje enviado correctamente."*
     * 🔴 *"Error al enviar mensaje."*
3. **Navegación:**
   * Añadir un botón o tarjeta en el Dashboard principal que navegue hacia `SendNotificationScreen`.

---

## 📍 FASE 5: Actualización del Pipeline CI/CD (`.github/workflows/deploy.yml`)

*Automatización del despliegue en la nube tanto del backend actual como del nuevo microservicio.*

### 5.1 Nuevos Pasos del Pipeline:
1. **Compilación de Imágenes Docker en Paralelo:**
   * Compilar y publicar `paralelo-repo/api:latest` (Backend).
   * Compilar y publicar `paralelo-repo/notification-service:latest` (Microservicio de notificaciones).
2. **Terraform Apply:**
   * Inicializar, validar y aplicar Terraform con `-auto-approve` para levantar:
     * El Topic de Pub/Sub.
     * La Suscripción Push.
     * El nuevo servicio `notification-cloud-run`.
     * Los roles IAM correspondientes.
3. **Reporte de URLs:**
   * Imprimir la URL del backend, del gateway y del servicio de notificaciones.

---

## 📍 FASE 6: Rúbrica de Evidencias para la Calificación del Profesor

*Guía para demostrar cada punto obligatorio durante la corrección:*

| Requerimiento | Dónde Mostrarlo | Qué Explicar |
|---|---|---|
| **1. Código Terraform de Pub/Sub** | `terraform/main.tf` | Mostrar `google_pubsub_topic` y `google_pubsub_subscription`. |
| **2. Código Terraform de Cloud Run** | `terraform/main.tf` | Mostrar `google_cloud_run_v2_service.notification_service`. |
| **3. Recursos en Google Cloud** | Consola GCP / Cloud Shell | Mostrar el Topic en **Pub/Sub**, la suscripción y los dos servicios en **Cloud Run**. |
| **4. Flujo en Vivo desde la App** | Teléfono / Emulador Android | Abrir la pantalla de notificaciones, ingresar datos, pulsar enviar y mostrar el mensaje de confirmación. |
| **5. Cloud Logging** | Logs Explorer de GCP | Filtrar logs por `notification-cloud-run` y mostrar en tiempo real la llegada del mensaje desde Pub/Sub y el registro del correo. |
| **6. GitHub Actions** | Pestaña Actions del repositorio | Mostrar el pipeline en verde con `terraform apply -auto-approve` exitoso. |
