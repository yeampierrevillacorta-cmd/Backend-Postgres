#!/bin/bash

# Despliegue rápido usando Cloud Build (sin Docker local)
# Uso: ./deploy-quick.sh

set -e

echo "🚀 Despliegue rápido de MySyncApp Backend..."

PROJECT_ID="conexionpostgres"
SERVICE_NAME="mysyncapp-backend"
REGION="us-central1"
CLOUD_SQL_INSTANCE="${PROJECT_ID}:${REGION}:mysyncapp-postgres"

# Desplegar directamente desde el código fuente
gcloud run deploy ${SERVICE_NAME} \
  --source . \
  --platform managed \
  --region ${REGION} \
  --allow-unauthenticated \
  --add-cloudsql-instances ${CLOUD_SQL_INSTANCE} \
  --set-env-vars "SPRING_PROFILES_ACTIVE=prod,SERVER_PORT=8080,DB_NAME=mysyncapp_db,DB_USERNAME=mysyncapp_user,DB_PASSWORD=12345Yeam,CLOUD_SQL_CONNECTION_NAME=${CLOUD_SQL_INSTANCE},JWT_SECRET=MiClaveSuperSecreta123"

echo "✅ Despliegue completado!"
gcloud run services describe ${SERVICE_NAME} --region ${REGION} --format 'value(status.url)'

