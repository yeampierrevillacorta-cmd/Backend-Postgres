#!/bin/bash

# Script de despliegue para MySyncApp Backend en Google Cloud Run
# Uso: ./deploy.sh

set -e  # Salir si hay algún error

echo "🚀 Iniciando despliegue de MySyncApp Backend..."

# Variables de configuración
PROJECT_ID="conexionpostgres"
IMAGE_NAME="gcr.io/${PROJECT_ID}/mysyncapp-backend:latest"
SERVICE_NAME="mysyncapp-backend"
REGION="us-central1"
CLOUD_SQL_INSTANCE="${PROJECT_ID}:${REGION}:mysyncapp-postgres"

# Paso 1: Compilar el proyecto
echo "📦 Compilando el proyecto..."
./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Error al compilar el proyecto"
    exit 1
fi

echo "✅ Compilación exitosa"

# Paso 2: Construir la imagen Docker
echo "🐳 Construyendo imagen Docker..."
docker build -t ${IMAGE_NAME} .

if [ $? -ne 0 ]; then
    echo "❌ Error al construir la imagen Docker"
    exit 1
fi

echo "✅ Imagen Docker construida"

# Paso 3: Subir la imagen a Google Container Registry
echo "📤 Subiendo imagen a GCR..."
docker push ${IMAGE_NAME}

if [ $? -ne 0 ]; then
    echo "❌ Error al subir la imagen"
    exit 1
fi

echo "✅ Imagen subida exitosamente"

# Paso 4: Desplegar en Cloud Run
echo "☁️ Desplegando en Cloud Run..."
gcloud run deploy ${SERVICE_NAME} \
  --image ${IMAGE_NAME} \
  --platform managed \
  --region ${REGION} \
  --allow-unauthenticated \
  --add-cloudsql-instances ${CLOUD_SQL_INSTANCE} \
  --set-env-vars "SPRING_PROFILES_ACTIVE=prod" \
  --set-env-vars "SERVER_PORT=8080" \
  --set-env-vars "DB_NAME=mysyncapp_db" \
  --set-env-vars "DB_USERNAME=mysyncapp_user" \
  --set-env-vars "DB_PASSWORD=12345Yeam" \
  --set-env-vars "CLOUD_SQL_CONNECTION_NAME=${CLOUD_SQL_INSTANCE}" \
  --set-env-vars "JWT_SECRET=MiClaveSuperSecreta123"

if [ $? -ne 0 ]; then
    echo "❌ Error al desplegar en Cloud Run"
    exit 1
fi

echo "✅ Despliegue completado exitosamente!"
echo ""
echo "🌐 Obteniendo URL del servicio..."
gcloud run services describe ${SERVICE_NAME} --region ${REGION} --format 'value(status.url)'

