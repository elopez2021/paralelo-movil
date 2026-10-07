# ☁️ Guía de Despliegue Serverless en GCP con Terraform y CI/CD en GitHub Actions

Esta guía detalla el **paso a paso cronológico (de principio a fin)** para cumplir con todos los requerimientos solicitados por el profesor:
1. **Infraestructura como Código (IaC)** con Terraform (`Cloud Run`, `API Gateway`, `Artifact Registry`, `Cloud SQL / Base de Datos`, `IAM`, `Secret Manager`).
2. **Automatización CI/CD** con GitHub Actions (`.github/workflows/deploy.yml`).
3. **Integración con la Aplicación Móvil** (Android Kotlin).
4. **Lista de evidencias obligatorias** para la entrega.

---

## 🗺️ Mapa de Ruta: Orden de Ejecución

```text
[FASE 1: GCP Inicial] ──> [FASE 2: Código Terraform] ──> [FASE 3: GitHub Secrets] ──> [FASE 4: Workflow CI/CD] ──> [FASE 5: App Móvil] ──> [FASE 6: Evidencias]
   • Proyecto GCP            • provider.tf                 • GCP_PROJECT_ID            • deploy.yml                  • Actualizar URL          • Capturas
   • Service Account         • main.tf                     • GCP_SA_KEY                • Build & Tests               • Probar Login/CRUD       • Presentación
   • Habilitar APIs          • variables / outputs         • DATABASE_URL              • Terraform Apply             • Benchmark en vivo       • Entrega
                             • api-spec.yaml               • JWT_SECRET                • Deploy Cloud Run
```

---

## 📍 FASE 1: Preparación Inicial en Google Cloud Platform (GCP)

*Esta fase se realiza **primero**, ya que necesitamos las credenciales para que GitHub Actions y Terraform puedan interactuar con Google Cloud.*

### 1.1 Crear o seleccionar un Proyecto en GCP
1. Ingresa a la [Consola de Google Cloud](https://console.cloud.google.com/).
2. Crea un proyecto nuevo (ejemplo: `paralelo-movil-prod`) o selecciona uno existente.
3. Copia el **Project ID** (lo necesitarás más adelante).

### 1.2 Habilitar Facturación (Billing)
- Asegúrate de que el proyecto tenga una cuenta de facturación vinculada (GCP ofrece $300 USD de crédito gratuito para cuentas nuevas).

### 1.3 Crear la Cuenta de Servicio (Service Account) para CI/CD
1. En la consola de GCP, ve a **IAM y administración > Cuentas de servicio**.
2. Haz clic en **Crear cuenta de servicio**:
   - Nombre: `github-actions-deployer`
   - ID: `github-actions-deployer`
3. Asigna los roles necesarios para aprovisionar y desplegar:
   - **Editor** (o para mínimos privilegios: *Cloud Run Admin*, *Artifact Registry Administrator*, *Storage Admin*, *Secret Manager Admin*, *API Gateway Admin*, *Service Account User*).
4. Haz clic en **Listo**.
5. Entra a la cuenta creada, ve a la pestaña **Claves > Agregar clave > Crear clave nueva**.
6. Selecciona el tipo **JSON** y descárgala.
   > ⚠️ **IMPORTANTE:** Este archivo JSON contiene la credencial privada. **No lo subas a Git**.

### 1.4 Habilitar las APIs de GCP necesarias
Abre la consola de Cloud Shell en GCP o tu terminal local y ejecuta:
```bash
gcloud services enable \
  run.googleapis.com \
  artifactregistry.googleapis.com \
  apigateway.googleapis.com \
  servicecontrol.googleapis.com \
  servicemanagement.googleapis.com \
  sqladmin.googleapis.com \
  secretmanager.googleapis.com \
  cloudbuild.googleapis.com \
  iam.googleapis.com
```

---

## 📍 FASE 2: Creación de la Infraestructura con Terraform (`terraform/`)

*Creamos la carpeta `terraform/` en la raíz del proyecto para declarar todos los recursos en la nube.*

### 2.1 Estructura de Archivos a Crear
```text
terraform/
├── provider.tf           # Configuración del proveedor de Google
├── variables.tf          # Definición de variables parametrizables
├── main.tf               # Recursos (Artifact Registry, Cloud Run, API Gateway, IAM, Secrets)
├── outputs.tf            # URLs generadas y nombres de recursos
├── openapi_spec.yaml     # Especificación OpenAPI para el API Gateway
└── terraform.tfvars      # Valores de las variables (ignorado en git)
```

### 2.2 Selección de Base de Datos Cloud (Opciones Permitidas)
El profesor permite varias alternativas:
* **Opción A (Recomendada para presupuesto / Estudiantes):** **Neon PostgreSQL** o **Supabase PostgreSQL**.
  - Son instancias de PostgreSQL 100% gestionadas en la nube, compatibles con SQLAlchemy, con capa gratuita permanente y conexión directa sin costo por hora de Cloud SQL.
  - La URL de conexión se inyecta en Secret Manager: `postgresql://user:pass@ep-xyz.neon.tech/neondb?sslmode=require`.
* **Opción B (Full GCP):** **Cloud SQL PostgreSQL**.
  - Declarado directamente dentro de `main.tf` con una instancia `db-f1-micro`.

---

## 📍 FASE 3: Configuración de GitHub Secrets

*Para que GitHub Actions pueda autenticarse sin exponer credenciales en el código fuente.*

1. Ve a tu repositorio en GitHub: `https://github.com/elopez2021/paralelo-movil`.
2. Haz clic en **Settings > Secrets and variables > Actions > New repository secret**.
3. Agrega los siguientes secretos:

| Nombre del Secret | Obligatorio | Valor |
|---|:---:|---|
| `GCP_PROJECT_ID` | **SÍ** | El ID de tu proyecto en GCP (ej: `paralelo-movil-123456`) |
| `GCP_SA_KEY` | **SÍ** | El contenido completo del archivo JSON descargado de tu Service Account de GCP |
| `GCP_REGION` | No (Opcional) | Región de GCP (si no lo pones, por defecto usará `us-central1`) |
| `JWT_SECRET` | No (Opcional) | Clave secreta (ej: `paralelo_movil_super_secret_key_2026_jwt_token`) |
| `DATABASE_URL` | No (Opcional) | **NO se necesita si usas Cloud SQL** (Terraform la crea automáticamente). Solo es necesaria si usas una BD externa como Neon o Supabase. |


---

## 📍 FASE 4: Implementación del Pipeline CI/CD (`.github/workflows/deploy.yml`)

*El workflow automatizado que se disparará con cada push a la rama `main`.*

### Etapas del Pipeline:
```mermaid
graph LR
    A[Push a main] --> B[1. Setup & Tests]
    B --> C[2. Auth en GCP]
    C --> D[3. Build Docker & Push a Artifact Registry]
    D --> E[4. Terraform Init / Plan / Apply]
    E --> F[5. Despliegue en Cloud Run]
    F --> G[6. Verificación de URL pública]
```

1. **Test & Lint:** Instala dependencias y corre los tests con pytest de la API.
2. **Autenticación GCP:** Usa `google-github-actions/auth` con `GCP_SA_KEY`.
3. **Artifact Registry:** Construye la imagen Docker del backend y la publica en `us-central1-docker.pkg.dev/<GCP_PROJECT_ID>/paralelo-repo/api:latest`.
4. **Terraform Apply:**
   ```bash
   terraform init
   terraform validate
   terraform plan
   terraform apply -auto-approve
   ```
5. **Cloud Run Deploy:** Despliega la nueva imagen en Cloud Run con las variables de entorno inyectadas desde Secret Manager.

---

## 📍 FASE 5: Actualización de la App Móvil (Android Kotlin)

*Una vez desplegado el backend en Cloud Run / API Gateway, la app móvil debe apuntar a la URL pública HTTPS de Google Cloud.*

1. Abre el archivo:
   [`Mobile/app/src/main/java/com/movil/paralelo/utils/Constants.kt`](file:///e:/algoritmos%20paralelos/tarea%201/Mobile/app/src/main/java/com/movil/paralelo/utils/Constants.kt)
2. Actualiza `BASE_URL`:
   ```kotlin
   object Constants {
       // Reemplazar con la URL generada por API Gateway o Cloud Run
       var BASE_URL: String = "https://paralelo-api-xyz-uc.a.run.app/"
       ...
   }
   ```
3. Ahora la aplicación móvil se conectará a la nube desde cualquier dispositivo con internet (sin depender de `localhost` ni de túneles como ngrok).

---

## 📍 FASE 6: Recopilación de Evidencias para la Calificación

El profesor solicita evidencias explícitas organizadas en 4 partes:

### Parte 1 - Terraform
- [ ] Código fuente en la carpeta `terraform/` (`provider.tf`, `main.tf`, `variables.tf`, `outputs.tf`).
- [ ] Captura de pantalla de la terminal mostrando `terraform plan` y `terraform apply` exitoso.
- [ ] Archivo o salida mostrando los outputs con las URLs creadas.

### Parte 2 - GitHub Actions
- [ ] Archivo `.github/workflows/deploy.yml` en el repositorio.
- [ ] Captura de la sección **Settings > Secrets and variables** mostrando los secrets configurados.
- [ ] Captura de la pestaña **Actions** en GitHub con la ejecución con todos los checks verdes:
  - ✔ `Build`
  - ✔ `Test`
  - ✔ `Docker Build & Push`
  - ✔ `Terraform Plan & Apply`
  - ✔ `Deploy Cloud Run`

### Parte 3 - Consola de Google Cloud Platform (GCP)
- [ ] **Cloud Run:** Mostrar el servicio activo con estado verde y tráfico al 100%.
- [ ] **Artifact Registry:** Mostrar el repositorio `paralelo-repo` con las imágenes Docker etiquetadas.
- [ ] **Secret Manager:** Mostrar los secretos `DATABASE_URL` y `JWT_SECRET` creados.
- [ ] **Cloud Logging:** Captura de registros en vivo mostrando peticiones HTTP exitosas (código 200/201).
- [ ] **API Gateway / Cloud SQL:** Mostrar el recurso activo en la consola.

### Parte 4 - Aplicación Móvil
- [ ] Login exitoso consumiendo el Cloud Run remoto y almacenando el JWT.
- [ ] CRUD de Usuarios funcionando contra la base de datos cloud.
- [ ] Subida de archivo/foto desde el celular hacia el Cloud Run.
- [ ] Pantalla de Benchmark ejecutando el proceso concurrente vs secuencial en la nube.

---

## 🚀 ¿Qué se debe hacer primero en el código?

1. **Crear la carpeta `terraform/`** con los archivos de infraestructura y la especificación OpenAPI de API Gateway.
2. **Crear el archivo `.github/workflows/deploy.yml`** con el pipeline completo de CI/CD.
3. **Verificar que el backend soporte la variable `PORT` dinámica** de Cloud Run (Cloud Run asigna el puerto mediante `$PORT`, por defecto `8080`).
4. **Hacer commit y push a la rama principal** para disparar la primera ejecución de GitHub Actions.
