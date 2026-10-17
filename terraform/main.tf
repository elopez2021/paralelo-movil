# 1. Habilitación de APIs necesarias en GCP
locals {
  gcp_services = [
    "cloudresourcemanager.googleapis.com",
    "run.googleapis.com",
    "artifactregistry.googleapis.com",
    "apigateway.googleapis.com",
    "servicecontrol.googleapis.com",
    "servicemanagement.googleapis.com",
    "sqladmin.googleapis.com",
    "secretmanager.googleapis.com",
    "cloudbuild.googleapis.com",
    "logging.googleapis.com",
    "storage.googleapis.com",
    "pubsub.googleapis.com"
  ]
}

resource "google_project_service" "enabled_apis" {
  for_each           = toset(local.gcp_services)
  project            = var.project_id
  service            = each.key
  disable_on_destroy = false
}

# 2. Artifact Registry (Repositorio Docker)
resource "google_artifact_registry_repository" "repo" {
  provider      = google-beta
  project       = var.project_id
  location      = var.region
  repository_id = var.artifact_repo_name
  description   = "Repositorio Docker para Paralelo Movil Backend"
  format        = "DOCKER"

  depends_on = [google_project_service.enabled_apis]
}

# 3. Service Account para Cloud Run con permisos de mínimos privilegios
resource "google_service_account" "cloud_run_sa" {
  account_id   = "paralelo-run-sa"
  display_name = "Service Account para Cloud Run API"
  project      = var.project_id
}

resource "google_project_iam_member" "sa_secret_accessor" {
  project = var.project_id
  role    = "roles/secretmanager.secretAccessor"
  member  = "serviceAccount:${google_service_account.cloud_run_sa.email}"
}

resource "google_project_iam_member" "sa_cloudsql_client" {
  project = var.project_id
  role    = "roles/cloudsql.client"
  member  = "serviceAccount:${google_service_account.cloud_run_sa.email}"
}

resource "google_project_iam_member" "sa_logging_writer" {
  project = var.project_id
  role    = "roles/logging.logWriter"
  member  = "serviceAccount:${google_service_account.cloud_run_sa.email}"
}

resource "google_project_iam_member" "sa_storage_admin" {
  project = var.project_id
  role    = "roles/storage.objectAdmin"
  member  = "serviceAccount:${google_service_account.cloud_run_sa.email}"
}

# 4. Cloud Storage Bucket (Opcional/Almacenamiento de Archivos)
resource "random_id" "bucket_suffix" {
  byte_length = 4
}

resource "google_storage_bucket" "uploads_bucket" {
  name                        = "paralelo-uploads-${var.project_id}-${random_id.bucket_suffix.hex}"
  location                    = var.region
  project                     = var.project_id
  uniform_bucket_level_access = true
  force_destroy               = true

  cors {
    origin          = ["*"]
    method          = ["GET", "POST", "PUT", "DELETE", "HEAD"]
    response_header = ["*"]
    max_age_seconds = 3600
  }

  depends_on = [google_project_service.enabled_apis]
}

# 5. Cloud SQL PostgreSQL (Instancia administrada)
resource "google_sql_database_instance" "postgres_instance" {
  count            = var.enable_cloud_sql ? 1 : 0
  name             = "paralelo-postgres-${random_id.bucket_suffix.hex}"
  database_version = "POSTGRES_16"
  region           = var.region
  project          = var.project_id

  deletion_protection = false

  settings {
    tier              = var.db_instance_tier
    availability_type = "ZONAL"
    disk_size         = 10
    disk_type         = "PD_SSD"

    ip_configuration {
      ipv4_enabled = true
      authorized_networks {
        name  = "all"
        value = "0.0.0.0/0" # Permite conexiones autorizadas
      }
    }
  }

  depends_on = [google_project_service.enabled_apis]
}

resource "google_sql_database" "database" {
  count    = var.enable_cloud_sql ? 1 : 0
  name     = var.db_name
  instance = google_sql_database_instance.postgres_instance[0].name
  project  = var.project_id
}

resource "google_sql_user" "db_user" {
  count    = var.enable_cloud_sql ? 1 : 0
  name     = var.db_user
  instance = google_sql_database_instance.postgres_instance[0].name
  password = var.db_password
  project  = var.project_id
}

# 6. Secret Manager (Variables Sensibles)
locals {
  # Si Cloud SQL está activo, construimos la URL; de lo contrario usamos la variable externa (ej. Neon o Supabase)
  resolved_db_url = var.enable_cloud_sql ? "postgresql+psycopg://${var.db_user}:${var.db_password}@${google_sql_database_instance.postgres_instance[0].public_ip_address}:5432/${var.db_name}" : var.database_url
}

resource "google_secret_manager_secret" "db_url_secret" {
  secret_id = "DATABASE_URL"
  project   = var.project_id

  replication {
    auto {}
  }

  depends_on = [google_project_service.enabled_apis]
}

resource "google_secret_manager_secret_version" "db_url_version" {
  secret      = google_secret_manager_secret.db_url_secret.id
  secret_data = local.resolved_db_url
}

resource "google_secret_manager_secret" "jwt_secret" {
  secret_id = "JWT_SECRET"
  project   = var.project_id

  replication {
    auto {}
  }

  depends_on = [google_project_service.enabled_apis]
}

resource "google_secret_manager_secret_version" "jwt_version" {
  secret      = google_secret_manager_secret.jwt_secret.id
  secret_data = var.jwt_secret
}

# 7. Cloud Run (v2 Service)
locals {
  default_image = "${var.region}-docker.pkg.dev/${var.project_id}/${var.artifact_repo_name}/api:latest"
  image_to_use  = var.container_image != "" ? var.container_image : local.default_image
}

resource "google_cloud_run_v2_service" "api" {
  name     = var.service_name
  location = var.region
  project  = var.project_id
  ingress  = "INGRESS_TRAFFIC_ALL"

  template {
    service_account = google_service_account.cloud_run_sa.email

    scaling {
      min_instance_count = 0 # Scale-to-zero (Serverless para costo mínimo)
      max_instance_count = 3
    }

    containers {
      image = local.image_to_use

      resources {
        limits = {
          cpu    = "1"
          memory = "512Mi"
        }
      }

      ports {
        container_port = 8000
      }

      env {
        name  = "STORAGE_BUCKET"
        value = google_storage_bucket.uploads_bucket.name
      }

      env {
        name = "DATABASE_URL"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.db_url_secret.secret_id
            version = "latest"
          }
        }
      }

      env {
        name = "SECRET_KEY"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.jwt_secret.secret_id
            version = "latest"
          }
        }
      }

      env {
        name  = "PUBSUB_TOPIC_NAME"
        value = var.pubsub_topic_name
      }

      env {
        name  = "GCP_PROJECT_ID"
        value = var.project_id
      }
    }
  }

  depends_on = [
    google_project_service.enabled_apis,
    google_project_iam_member.sa_secret_accessor,
    google_secret_manager_secret_version.db_url_version,
    google_secret_manager_secret_version.jwt_version
  ]
}

# Permitir acceso público directo a Cloud Run
resource "google_cloud_run_v2_service_iam_member" "public_invoker" {
  project  = var.project_id
  location = var.region
  name     = google_cloud_run_v2_service.api.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}

# 8. API Gateway
resource "google_api_gateway_api" "api_gw" {
  provider     = google-beta
  project      = var.project_id
  api_id       = "paralelo-api-gateway"
  display_name = "Gateway para Paralelo Movil"

  depends_on = [google_project_service.enabled_apis]
}

resource "google_api_gateway_api_config" "api_cfg" {
  provider      = google-beta
  project       = var.project_id
  api           = google_api_gateway_api.api_gw.api_id
  api_config_id = "v1-config-${random_id.bucket_suffix.hex}"
  display_name  = "API Config v1"

  openapi_documents {
    document {
      path = "spec.yaml"
      contents = base64encode(templatefile("${path.module}/openapi_spec.yaml", {
        cloud_run_url = google_cloud_run_v2_service.api.uri
      }))
    }
  }

  lifecycle {
    create_before_destroy = true
  }

  depends_on = [google_cloud_run_v2_service.api]
}

resource "google_api_gateway_gateway" "gateway" {
  provider   = google-beta
  project    = var.project_id
  region     = var.region
  gateway_id = "paralelo-gateway"
  api_config = google_api_gateway_api_config.api_cfg.id

  depends_on = [google_api_gateway_api_config.api_cfg]
}

# 9. Google Cloud Pub/Sub Topic


resource "google_pubsub_topic" "email_topic" {
  name    = var.pubsub_topic_name
  project = var.project_id

  depends_on = [google_project_service.enabled_apis]
}





resource "google_pubsub_topic_iam_member" "backend_publisher" {
  project = var.project_id
  topic   = google_pubsub_topic.email_topic.name
  role    = "roles/pubsub.publisher"
  member  = "serviceAccount:${google_service_account.cloud_run_sa.email}"
}



# Service Account para invocar Cloud Run desde la suscripción Push de Pub/Sub
resource "google_service_account" "pubsub_invoker_sa" {
  account_id   = "pubsub-invoker-sa"
  display_name = "Service Account para Push de Pub/Sub a Cloud Run"
  project      = var.project_id
}

# 10. Cloud Run: notification-cloud-run
locals {
  default_notification_image = "${var.region}-docker.pkg.dev/${var.project_id}/${var.artifact_repo_name}/notification-service:latest"
  notification_image_to_use  = var.notification_container_image != "" ? var.notification_container_image : local.default_notification_image
}

resource "google_cloud_run_v2_service" "notification_service" {
  name     = var.notification_service_name
  location = var.region
  project  = var.project_id
  ingress  = "INGRESS_TRAFFIC_ALL"

  template {
    scaling {
      min_instance_count = 0
      max_instance_count = 3
    }

    containers {
      image = local.notification_image_to_use

      resources {
        limits = {
          cpu    = "1"
          memory = "512Mi"
        }
      }

      ports {
        container_port = 8080
      }

      env {
        name  = "SMTP_HOST"
        value = var.smtp_host
      }

      env {
        name  = "SMTP_PORT"
        value = tostring(var.smtp_port)
      }

      env {
        name  = "SMTP_USER"
        value = var.smtp_user
      }

      env {
        name  = "SMTP_PASSWORD"
        value = var.smtp_password
      }

      env {
        name  = "SMTP_FROM"
        value = var.smtp_from
      }
    }
  }

  depends_on = [google_project_service.enabled_apis]
}

# Permitir que la Service Account de Pub/Sub invoque notification-cloud-run
resource "google_cloud_run_v2_service_iam_member" "pubsub_run_invoker" {
  project  = var.project_id
  location = var.region
  name     = google_cloud_run_v2_service.notification_service.name
  role     = "roles/run.invoker"
  member   = "serviceAccount:${google_service_account.pubsub_invoker_sa.email}"
}

# Permitir acceso publico para health check / pruebas directas
resource "google_cloud_run_v2_service_iam_member" "notification_public_invoker" {
  project  = var.project_id
  location = var.region
  name     = google_cloud_run_v2_service.notification_service.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}

# 11. Suscripción Push de Pub/Sub hacia notification-cloud-run
resource "google_pubsub_subscription" "notification_push_sub" {
  name    = "notifications-push-sub"
  topic   = google_pubsub_topic.email_topic.name
  project = var.project_id

  ack_deadline_seconds = 60

  push_config {
    push_endpoint = google_cloud_run_v2_service.notification_service.uri

    oidc_token {
      service_account_email = google_service_account.pubsub_invoker_sa.email
      audience              = google_cloud_run_v2_service.notification_service.uri
    }
  }

  depends_on = [
    google_pubsub_topic.email_topic,
    google_cloud_run_v2_service.notification_service,
    google_cloud_run_v2_service_iam_member.pubsub_run_invoker
  ]
}

