output "cloud_run_url" {
  description = "URL pública directa del servicio Cloud Run"
  value       = google_cloud_run_v2_service.api.uri
}

output "api_gateway_url" {
  description = "URL pública del API Gateway"
  value       = "https://${google_api_gateway_gateway.gateway.default_hostname}"
}

output "artifact_registry_repository" {
  description = "Ruta completa del repositorio en Artifact Registry para subir imágenes Docker"
  value       = "${var.region}-docker.pkg.dev/${var.project_id}/${var.artifact_repo_name}"
}

output "storage_bucket_name" {
  description = "Nombre del bucket de almacenamiento Cloud Storage"
  value       = google_storage_bucket.uploads_bucket.name
}

output "cloud_sql_public_ip" {
  description = "IP pública de la instancia de Cloud SQL PostgreSQL"
  value       = var.enable_cloud_sql ? google_sql_database_instance.postgres_instance[0].public_ip_address : "N/A (Base de datos externa)"
}

output "cloud_sql_connection_name" {
  description = "Nombre de conexión de la instancia de Cloud SQL"
  value       = var.enable_cloud_sql ? google_sql_database_instance.postgres_instance[0].connection_name : "N/A"
}
