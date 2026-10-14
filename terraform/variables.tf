variable "project_id" {
  description = "El ID del proyecto de Google Cloud Platform (GCP)"
  type        = string
}

variable "region" {
  description = "Región de GCP donde se desplegarán los recursos"
  type        = string
  default     = "us-central1"
}

variable "environment" {
  description = "Entorno de despliegue (prod, dev, staging)"
  type        = string
  default     = "prod"
}

variable "service_name" {
  description = "Nombre base para el servicio de Cloud Run"
  type        = string
  default     = "notificacion"
}

variable "artifact_repo_name" {
  description = "Nombre del repositorio en Artifact Registry"
  type        = string
  default     = "paralelo-repo"
}

variable "container_image" {
  description = "URL completa de la imagen Docker en Artifact Registry"
  type        = string
  default     = ""
}

variable "jwt_secret" {
  description = "Clave secreta para firmar los tokens JWT"
  type        = string
  sensitive   = true
  default     = "paralelo_movil_super_secret_key_2026_jwt_token"
}

variable "database_url" {
  description = "URL de conexión PostgreSQL (si se usa Neon, Supabase o externa). Si está vacía y enable_cloud_sql es true, se usará Cloud SQL."
  type        = string
  sensitive   = true
  default     = ""
}

variable "enable_cloud_sql" {
  description = "Si es true, crea una instancia de Cloud SQL PostgreSQL gestionada en GCP"
  type        = bool
  default     = true
}

variable "db_instance_tier" {
  description = "Tipo de máquina para la instancia de Cloud SQL (db-f1-micro es la más económica)"
  type        = string
  default     = "db-f1-micro"
}

variable "db_name" {
  description = "Nombre de la base de datos PostgreSQL"
  type        = string
  default     = "paralelo_db"
}

variable "db_user" {
  description = "Usuario administrador de la base de datos PostgreSQL"
  type        = string
  default     = "postgres"
}

variable "db_password" {
  description = "Contraseña para el usuario de PostgreSQL en Cloud SQL"
  type        = string
  sensitive   = true
  default     = "ParaleloDb2026Secure!"
}

variable "notification_service_name" {
  description = "Nombre del microservicio Cloud Run de Notificaciones"
  type        = string
  default     = "notification-cloud-run"
}

variable "pubsub_topic_name" {
  description = "Nombre del Topic de Google Cloud Pub/Sub para notificaciones"
  type        = string
  default     = "notifications-topic"
}

variable "notification_container_image" {
  description = "URL de la imagen Docker para el servicio de notificaciones en Artifact Registry"
  type        = string
  default     = ""
}

variable "smtp_host" {
  description = "Host del servidor SMTP para envío de correos (opcional)"
  type        = string
  default     = ""
}

variable "smtp_port" {
  description = "Puerto del servidor SMTP"
  type        = number
  default     = 587
}

variable "smtp_user" {
  description = "Usuario o correo del servidor SMTP"
  type        = string
  default     = ""
}

variable "smtp_password" {
  description = "Contraseña o App Password del servidor SMTP"
  type        = string
  sensitive   = true
  default     = ""
}

variable "smtp_from" {
  description = "Dirección remitente para los correos enviados"
  type        = string
  default     = ""
}

