# Script de despliegue para MySyncApp Backend en Google Cloud Run (PowerShell)
# Uso: .\deploy.ps1

$ErrorActionPreference = "Stop"

Write-Host "🚀 Iniciando despliegue de MySyncApp Backend..." -ForegroundColor Cyan

# Variables de configuración
$PROJECT_ID = "conexionpostgres"
$IMAGE_NAME = "gcr.io/${PROJECT_ID}/mysyncapp-backend:latest"
$SERVICE_NAME = "mysyncapp-backend"
$REGION = "us-central1"
$CLOUD_SQL_INSTANCE = "${PROJECT_ID}:${REGION}:mysyncapp-postgres"

# Paso 1: Compilar el proyecto
Write-Host "📦 Compilando el proyecto..." -ForegroundColor Yellow
.\mvnw.cmd clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error al compilar el proyecto" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Compilación exitosa" -ForegroundColor Green

# Paso 2: Construir la imagen Docker
Write-Host "🐳 Construyendo imagen Docker..." -ForegroundColor Yellow
docker build -t $IMAGE_NAME .

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error al construir la imagen Docker" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Imagen Docker construida" -ForegroundColor Green

# Paso 3: Subir la imagen a Google Container Registry
Write-Host "📤 Subiendo imagen a GCR..." -ForegroundColor Yellow
docker push $IMAGE_NAME

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error al subir la imagen" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Imagen subida exitosamente" -ForegroundColor Green

# Paso 4: Desplegar en Cloud Run
Write-Host "☁️ Desplegando en Cloud Run..." -ForegroundColor Yellow
gcloud run deploy $SERVICE_NAME `
  --image $IMAGE_NAME `
  --platform managed `
  --region $REGION `
  --allow-unauthenticated `
  --add-cloudsql-instances $CLOUD_SQL_INSTANCE `
  --set-env-vars "SPRING_PROFILES_ACTIVE=prod" `
  --set-env-vars "SERVER_PORT=8080" `
  --set-env-vars "DB_NAME=mysyncapp_db" `
  --set-env-vars "DB_USERNAME=mysyncapp_user" `
  --set-env-vars "DB_PASSWORD=12345Yeam" `
  --set-env-vars "CLOUD_SQL_CONNECTION_NAME=$CLOUD_SQL_INSTANCE" `
  --set-env-vars "JWT_SECRET=MiClaveSuperSecreta123"

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error al desplegar en Cloud Run" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Despliegue completado exitosamente!" -ForegroundColor Green
Write-Host ""
Write-Host "🌐 Obteniendo URL del servicio..." -ForegroundColor Cyan
gcloud run services describe $SERVICE_NAME --region $REGION --format 'value(status.url)'

