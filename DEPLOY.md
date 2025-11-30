# 🚀 Guía de Despliegue - MySyncApp Backend

Esta guía contiene todos los comandos necesarios para desplegar el backend en Google Cloud Run.

---

## 📋 Requisitos Previos

- Google Cloud SDK (`gcloud`) instalado y configurado
- Docker instalado
- Autenticado en GCP: `gcloud auth login`
- Proyecto configurado: `gcloud config set project conexionpostgres`
- Cloud SQL instancia creada: `mysyncapp-postgres`
- Container Registry habilitado

---

## 🔧 Configuración

### Variables del Proyecto

- **Project ID**: `conexionpostgres`
- **Service Name**: `mysyncapp-backend`
- **Region**: `us-central1`
- **Cloud SQL Instance**: `conexionpostgres:us-central1:mysyncapp-postgres`
- **Database Name**: `mysyncapp_db`
- **Database User**: `mysyncapp_user`

---

## 📦 Paso 1: Compilar el Proyecto

### Windows (PowerShell)
```powershell
.\mvnw.cmd clean package -DskipTests
```

### Linux/Mac
```bash
./mvnw clean package -DskipTests
```

---

## 🐳 Paso 2: Construir y Subir Imagen Docker

### Opción A: Manual

1. **Construir la imagen**:
```bash
docker build -t gcr.io/conexionpostgres/mysyncapp-backend:latest .
```

2. **Subir a Google Container Registry**:
```bash
docker push gcr.io/conexionpostgres/mysyncapp-backend:latest
```

### Opción B: Usar Cloud Build (Recomendado)

```bash
gcloud builds submit --tag gcr.io/conexionpostgres/mysyncapp-backend:latest
```

Este comando construye y sube la imagen automáticamente.

---

## ☁️ Paso 3: Desplegar en Cloud Run

### Comando Completo

```bash
gcloud run deploy mysyncapp-backend \
  --image gcr.io/conexionpostgres/mysyncapp-backend:latest \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --add-cloudsql-instances conexionpostgres:us-central1:mysyncapp-postgres \
  --set-env-vars "SPRING_PROFILES_ACTIVE=prod" \
  --set-env-vars "SERVER_PORT=8080" \
  --set-env-vars "DB_NAME=mysyncapp_db" \
  --set-env-vars "DB_USERNAME=mysyncapp_user" \
  --set-env-vars "DB_PASSWORD=12345Yeam" \
  --set-env-vars "CLOUD_SQL_CONNECTION_NAME=conexionpostgres:us-central1:mysyncapp-postgres" \
  --set-env-vars "JWT_SECRET=MiClaveSuperSecreta123"
```

### Windows (PowerShell)

```powershell
gcloud run deploy mysyncapp-backend `
  --image gcr.io/conexionpostgres/mysyncapp-backend:latest `
  --platform managed `
  --region us-central1 `
  --allow-unauthenticated `
  --add-cloudsql-instances conexionpostgres:us-central1:mysyncapp-postgres `
  --set-env-vars "SPRING_PROFILES_ACTIVE=prod" `
  --set-env-vars "SERVER_PORT=8080" `
  --set-env-vars "DB_NAME=mysyncapp_db" `
  --set-env-vars "DB_USERNAME=mysyncapp_user" `
  --set-env-vars "DB_PASSWORD=12345Yeam" `
  --set-env-vars "CLOUD_SQL_CONNECTION_NAME=conexionpostgres:us-central1:mysyncapp-postgres" `
  --set-env-vars "JWT_SECRET=MiClaveSuperSecreta123"
```

---

## 🚀 Despliegue Automatizado

### Opción 1: Despliegue Rápido (Recomendado)

Usa Cloud Build directamente desde el código fuente (no requiere Docker local):

#### Windows (PowerShell)
```powershell
.\deploy-quick.ps1
```

#### Linux/Mac
```bash
chmod +x deploy-quick.sh
./deploy-quick.sh
```

Este método:
- ✅ No requiere Docker instalado localmente
- ✅ Compila y construye la imagen automáticamente en la nube
- ✅ Más rápido y simple

### Opción 2: Despliegue Completo (Con Docker Local)

Si prefieres construir la imagen localmente:

#### Windows (PowerShell)
```powershell
.\deploy.ps1
```

#### Linux/Mac
```bash
chmod +x deploy.sh
./deploy.sh
```

---

## 🔄 Actualizar Despliegue Existente

### Método Rápido (Recomendado)

```bash
# Desplegar directamente desde el código fuente
gcloud run deploy mysyncapp-backend \
  --source . \
  --region us-central1
```

Este comando:
- Compila el código automáticamente
- Construye la imagen Docker
- Actualiza el servicio con la nueva versión
- Mantiene todas las configuraciones existentes

### Método Manual

Si prefieres controlar cada paso:

```bash
# 1. Compilar
./mvnw clean package -DskipTests

# 2. Construir y subir imagen
gcloud builds submit --tag gcr.io/conexionpostgres/mysyncapp-backend:latest

# 3. Desplegar (reutiliza la configuración existente)
gcloud run deploy mysyncapp-backend \
  --image gcr.io/conexionpostgres/mysyncapp-backend:latest \
  --region us-central1
```

---

## 🌐 Obtener URL del Servicio

```bash
gcloud run services describe mysyncapp-backend \
  --region us-central1 \
  --format 'value(status.url)'
```

O ver en la consola de Google Cloud:
https://console.cloud.google.com/run

---

## 🔍 Verificar Despliegue

### Ver logs en tiempo real
```bash
gcloud run services logs read mysyncapp-backend \
  --region us-central1 \
  --limit 50
```

### Probar endpoint
```bash
curl https://TU_URL_CLOUD_RUN/api/v1/sync/pull?userId=test
```

---

## ⚙️ Variables de Entorno

| Variable | Valor | Descripción |
|----------|-------|-------------|
| `SPRING_PROFILES_ACTIVE` | `prod` | Perfil de Spring Boot |
| `SERVER_PORT` | `8080` | Puerto del servidor |
| `DB_NAME` | `mysyncapp_db` | Nombre de la base de datos |
| `DB_USERNAME` | `mysyncapp_user` | Usuario de la base de datos |
| `DB_PASSWORD` | `12345Yeam` | Contraseña de la base de datos |
| `CLOUD_SQL_CONNECTION_NAME` | `conexionpostgres:us-central1:mysyncapp-postgres` | Nombre de conexión Cloud SQL |
| `JWT_SECRET` | `MiClaveSuperSecreta123` | Clave secreta para JWT |

### ⚠️ Seguridad: Usar Secret Manager (Recomendado)

Para producción, usa Secret Manager en lugar de variables de entorno:

```bash
# Crear secretos
echo -n "12345Yeam" | gcloud secrets create db-password --data-file=-
echo -n "MiClaveSuperSecreta123" | gcloud secrets create jwt-secret --data-file=-

# Desplegar con secretos
gcloud run deploy mysyncapp-backend \
  --image gcr.io/conexionpostgres/mysyncapp-backend:latest \
  --region us-central1 \
  --update-secrets DB_PASSWORD=db-password:latest,JWT_SECRET=jwt-secret:latest
```

---

## 🐛 Solución de Problemas

### Error: "Image not found"
- Verifica que la imagen se haya subido correctamente: `gcloud container images list`
- Asegúrate de que el nombre de la imagen sea correcto

### Error: "Cloud SQL connection failed"
- Verifica que la instancia Cloud SQL existe
- Asegúrate de que el servicio Cloud Run tenga permisos para conectarse
- Verifica el nombre de conexión: `conexionpostgres:us-central1:mysyncapp-postgres`

### Error: "Permission denied"
- Verifica que estés autenticado: `gcloud auth list`
- Verifica permisos del proyecto: `gcloud projects get-iam-policy conexionpostgres`

### Error: "Port already in use"
- Cambia el puerto en `SERVER_PORT` o verifica que no haya otro servicio usando el puerto 8080

---

## 📝 Comandos Útiles

### Listar servicios desplegados
```bash
gcloud run services list --region us-central1
```

### Ver detalles del servicio
```bash
gcloud run services describe mysyncapp-backend --region us-central1
```

### Eliminar servicio
```bash
gcloud run services delete mysyncapp-backend --region us-central1
```

### Ver métricas
```bash
gcloud run services describe mysyncapp-backend \
  --region us-central1 \
  --format 'value(status.url)'
```

---

## ✅ Checklist de Despliegue

- [ ] Proyecto compilado exitosamente (`mvnw clean package`)
- [ ] Imagen Docker construida
- [ ] Imagen subida a GCR
- [ ] Variables de entorno configuradas
- [ ] Cloud SQL instance conectada
- [ ] Servicio desplegado en Cloud Run
- [ ] URL del servicio obtenida
- [ ] Endpoints probados y funcionando
- [ ] Logs verificados sin errores

---

**Última actualización**: Diciembre 2024

