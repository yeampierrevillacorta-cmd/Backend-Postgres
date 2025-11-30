# 🔧 Solución para Error 403 en Endpoints de Sincronización

## 📋 Problema

La aplicación Android recibe error **403 Forbidden** al intentar acceder a los endpoints `/api/v1/sync/**` porque el backend requiere autenticación.

## 🎯 Objetivo

Modificar `SecurityConfig.java` para permitir acceso **sin autenticación** a los endpoints de sincronización (`/api/v1/sync/**`) temporalmente, para permitir que la aplicación Android se conecte correctamente.

---

## 📝 Cambios Requeridos

### Archivo a Modificar:

`src/main/java/com/example/demo/config/SecurityConfig.java`

### Cambio Específico:

**ANTES (Código Actual):**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/sync/**").authenticated()  // ← ESTA LÍNEA CAUSA EL 403
                .anyRequest().permitAll()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

**DESPUÉS (Código Modificado):**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/sync/**").permitAll()  // ← CAMBIAR A permitAll()
                .anyRequest().permitAll()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return http.build();
    }
}
```

### Cambios Detallados:

1. **Eliminar esta línea:**
   ```java
   @Autowired
   private JwtAuthenticationFilter jwtAuthenticationFilter;
   ```

2. **Cambiar esta línea:**
   ```java
   .requestMatchers("/api/v1/sync/**").authenticated()
   ```
   **Por:**
   ```java
   .requestMatchers("/api/v1/sync/**").permitAll()
   ```

3. **Eliminar esta línea:**
   ```java
   .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
   ```

4. **Eliminar imports no utilizados:**
   ```java
   import com.example.demo.security.JwtAuthenticationFilter;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
   ```

---

## ✅ Verificación

Después de hacer los cambios:

### 1. Recompilar el proyecto:

**Windows:**
```powershell
.\mvnw.cmd clean package -DskipTests
```

**Linux/Mac:**
```bash
./mvnw clean package -DskipTests
```

### 2. Redesplegar a Cloud Run:

**Método Rápido (Recomendado):**
```powershell
# Windows
.\deploy-quick.ps1

# Linux/Mac
./deploy-quick.sh
```

**Método Manual:**
```bash
gcloud run deploy mysyncapp-backend \
  --source . \
  --region us-central1 \
  --allow-unauthenticated
```

### 3. Probar el endpoint directamente:

```bash
# Probar endpoint PUSH
curl -X POST "https://TU_URL_CLOUD_RUN/api/v1/sync/push" \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "test-device",
    "userId": "test-user",
    "favorites": []
  }'
```

**Resultado esperado:** `200 OK` (no `403 Forbidden`)

```bash
# Probar endpoint PULL
curl "https://TU_URL_CLOUD_RUN/api/v1/sync/pull?userId=test-user"
```

**Resultado esperado:** `200 OK` con JSON de respuesta

---

## 📌 Notas Importantes

- ⚠️ **Esta es una solución temporal para desarrollo/testing**
- ✅ Los endpoints `/api/v1/sync/**` estarán accesibles sin autenticación
- 🔒 **Para producción**, se recomienda implementar validación de tokens Firebase (ver sección "Implementación Futura" más abajo)
- 🔐 **Seguridad**: Considera implementar rate limiting y otras medidas de seguridad

---

## 🚀 Después del Cambio

Una vez que el backend esté redesplegado:

1. ✅ La aplicación Android podrá sincronizar sin error 403
2. ✅ Los logs mostrarán: `✅ [PUSH] Push completado exitosamente`
3. ✅ Los favoritos se guardarán correctamente en la base de datos del backend
4. ✅ La sincronización bidireccional funcionará correctamente

---

## 🔍 Verificar que el Cambio Funcionó

### Desde la Aplicación Android:

1. Abre la aplicación
2. Intenta sincronizar favoritos
3. Verifica que no aparezca error 403 en los logs
4. Confirma que los datos se guarden en el backend

### Desde los Logs de Cloud Run:

```bash
gcloud run services logs read mysyncapp-backend \
  --region us-central1 \
  --limit 50
```

Busca:
- ✅ `200 OK` en las respuestas
- ❌ No debe aparecer `403 Forbidden`
- ✅ Requests exitosos a `/api/v1/sync/push` y `/api/v1/sync/pull`

---

## 🔐 Implementación Futura: Firebase Auth

Cuando quieras implementar autenticación en producción, consulta la sección **"Implementación Futura: Firebase Auth en el Backend"** en el archivo `ConfiguracionAndroid.md`.

### Resumen de la Implementación:

1. **Agregar dependencia Firebase Admin SDK** en `pom.xml`
2. **Crear configuración de Firebase** (`FirebaseConfig.java`)
3. **Crear filtro de autenticación Firebase** (`FirebaseAuthenticationFilter.java`)
4. **Actualizar SecurityConfig.java** para usar el filtro de Firebase
5. **Actualizar la app Android** para enviar tokens de Firebase

---

## 🐛 Solución de Problemas

### Error: "403 Forbidden" después del cambio

- Verifica que el código se haya compilado correctamente
- Asegúrate de que el despliegue se haya completado
- Revisa los logs de Cloud Run para ver errores
- Verifica que la URL del servicio sea correcta

### Error: "Cannot resolve symbol JwtAuthenticationFilter"

- Esto es normal después de eliminar la referencia
- Asegúrate de eliminar el import también
- El código debería compilar sin errores

### Error: "401 Unauthorized" (si implementaste Firebase Auth)

- Verifica que el token de Firebase se esté enviando correctamente
- Revisa que el filtro de Firebase esté configurado correctamente
- Verifica que el servicio de Firebase esté inicializado

---

## 📚 Archivos Relacionados

- `src/main/java/com/example/demo/config/SecurityConfig.java` - Archivo modificado
- `ConfiguracionAndroid.md` - Configuración completa de Android
- `DEPLOY.md` - Guía de despliegue
- `IndicacionesBack.md` - Documentación completa del backend

---

## ✅ Checklist de Verificación

- [ ] Código de `SecurityConfig.java` modificado correctamente
- [ ] Imports no utilizados eliminados
- [ ] Proyecto compila sin errores
- [ ] Backend redesplegado en Cloud Run
- [ ] Endpoint probado con curl (200 OK)
- [ ] Aplicación Android puede sincronizar sin error 403
- [ ] Logs verificados sin errores de autenticación

---

**Fecha:** Diciembre 2024  
**Prioridad:** Alta (bloquea la funcionalidad de sincronización)  
**Estado:** ✅ Cambios ya aplicados en el código base

