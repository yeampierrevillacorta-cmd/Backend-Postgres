# 📱 Configuración de Android Studio para MySyncApp Backend

Este documento contiene todas las instrucciones necesarias para configurar tu aplicación Android y conectarla correctamente con el backend Spring Boot.

> **⚠️ Nota sobre Autenticación**: El backend actualmente permite acceso sin autenticación a los endpoints `/api/v1/sync/**`. La sección de autenticación JWT es opcional y se puede omitir por ahora. En producción, se recomienda implementar validación de Firebase Auth en el backend.

---

## 📋 Tabla de Contenidos

1. [Dependencias Gradle](#1-dependencias-gradle)
2. [Estructura de Carpetas](#2-estructura-de-carpetas)
3. [Configuración de Red](#3-configuración-de-red)
4. [Modelos de Datos (DTOs)](#4-modelos-de-datos-dtos)
5. [Configuración de Retrofit](#5-configuración-de-retrofit)
6. [Servicio de API](#6-servicio-de-api)
7. [Manejo de Autenticación JWT](#7-manejo-de-autenticación-jwt)
8. [Repository Remoto](#8-repository-remoto)
9. [Ejemplos de Uso](#9-ejemplos-de-uso)
10. [Configuración de Variables](#10-configuración-de-variables)

---

## 1. Dependencias Gradle

### 1.1 Agregar al `app/build.gradle.kts`

```kotlin
dependencies {
    // Retrofit para llamadas HTTP
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // OkHttp para interceptores y logging
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Coroutines para operaciones asíncronas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Gson para serialización (si no está incluido)
    implementation("com.google.code.gson:gson:2.10.1")
    
    // DataStore para almacenar tokens (opcional pero recomendado)
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}
```

### 1.2 Habilitar View Binding (Recomendado)

En `app/build.gradle.kts`:

```kotlin
android {
    // ... otras configuraciones
    
    buildFeatures {
        viewBinding = true
    }
}
```

---

## 2. Estructura de Carpetas

Crea la siguiente estructura en tu proyecto Android:

```
app/src/main/java/com/tuempresa/mysyncapp/
├── data/
│   ├── model/              # DTOs/Modelos de datos
│   │   ├── FavoritePOIDto.kt
│   │   ├── CachedPOIDto.kt
│   │   ├── SearchHistoryDto.kt
│   │   ├── SyncRequest.kt
│   │   └── SyncResponse.kt
│   ├── network/            # Configuración de red
│   │   ├── RetrofitClient.kt
│   │   ├── SyncApiService.kt
│   │   └── AuthInterceptor.kt
│   ├── repository/         # Repositorios
│   │   └── RemoteSyncRepository.kt
│   └── local/              # Almacenamiento local (opcional)
│       └── TokenManager.kt
└── ui/                     # UI (Activities, Fragments, ViewModels)
```

---

## 3. Configuración de Red

### 3.1 Permisos en `AndroidManifest.xml`

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- Permiso de Internet -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
    <application
        android:usesCleartextTraffic="true"
        ...>
        
        <!-- ... resto de configuración -->
    </application>
</manifest>
```

**Nota**: `usesCleartextTraffic="true"` solo es necesario si usas HTTP (no HTTPS) en desarrollo. Para producción, usa HTTPS y elimina esta línea.

### 3.2 Configuración de Red Segura (Solo para Desarrollo Local)

Si necesitas conectarte a `http://localhost` o `http://10.0.2.2:8080` (emulador), crea `res/xml/network_security_config.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">localhost</domain>
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">TU_IP_LOCAL</domain>
    </domain-config>
</network-security-config>
```

Y en `AndroidManifest.xml`:

```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

---

## 4. Modelos de Datos (DTOs)

### 4.1 `FavoritePOIDto.kt`

```kotlin
package com.tuempresa.mysyncapp.data.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class FavoritePOIDto(
    @SerializedName("poiId")
    val poiId: String,
    
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("nombre")
    val nombre: String,
    
    @SerializedName("descripcion")
    val descripcion: String? = null,
    
    @SerializedName("categoria")
    val categoria: String? = null,
    
    @SerializedName("direccion")
    val direccion: String? = null,
    
    @SerializedName("lat")
    val lat: Double? = null,
    
    @SerializedName("lon")
    val lon: Double? = null,
    
    @SerializedName("calificacion")
    val calificacion: Double? = null,
    
    @SerializedName("imagenUrl")
    val imagenUrl: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: String? = null, // ISO 8601 format
    
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    
    @SerializedName("deleted")
    val deleted: Boolean? = false
)
```

### 4.2 `CachedPOIDto.kt`

```kotlin
package com.tuempresa.mysyncapp.data.model

import com.google.gson.annotations.SerializedName

data class CachedPOIDto(
    @SerializedName("poiId")
    val poiId: String,
    
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("nombre")
    val nombre: String,
    
    @SerializedName("descripcion")
    val descripcion: String? = null,
    
    @SerializedName("categoria")
    val categoria: String? = null,
    
    @SerializedName("direccion")
    val direccion: String? = null,
    
    @SerializedName("lat")
    val lat: Double? = null,
    
    @SerializedName("lon")
    val lon: Double? = null,
    
    @SerializedName("calificacion")
    val calificacion: Double? = null,
    
    @SerializedName("imagenUrl")
    val imagenUrl: String? = null,
    
    @SerializedName("cachedAt")
    val cachedAt: String? = null,
    
    @SerializedName("expiresAt")
    val expiresAt: String? = null
)
```

### 4.3 `SearchHistoryDto.kt`

```kotlin
package com.tuempresa.mysyncapp.data.model

import com.google.gson.annotations.SerializedName

data class SearchHistoryDto(
    @SerializedName("id")
    val id: Long? = null,
    
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("deviceId")
    val deviceId: String? = null,
    
    @SerializedName("searchQuery")
    val searchQuery: String,
    
    @SerializedName("searchType")
    val searchType: String? = null,
    
    @SerializedName("latitude")
    val latitude: Double? = null,
    
    @SerializedName("longitude")
    val longitude: Double? = null,
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("deleted")
    val deleted: Boolean? = false
)
```

### 4.4 `SyncRequest.kt`

```kotlin
package com.tuempresa.mysyncapp.data.model

import com.google.gson.annotations.SerializedName

data class SyncRequest(
    @SerializedName("deviceId")
    val deviceId: String,
    
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("lastSyncAt")
    val lastSyncAt: String? = null, // ISO 8601 format
    
    @SerializedName("favorites")
    val favorites: List<FavoritePOIDto>? = null,
    
    @SerializedName("cached")
    val cached: List<CachedPOIDto>? = null,
    
    @SerializedName("searchHistory")
    val searchHistory: List<SearchHistoryDto>? = null
)
```

### 4.5 `SyncResponse.kt`

```kotlin
package com.tuempresa.mysyncapp.data.model

import com.google.gson.annotations.SerializedName

data class SyncResponse(
    @SerializedName("serverTimestamp")
    val serverTimestamp: String, // ISO 8601 format
    
    @SerializedName("favorites")
    val favorites: List<FavoritePOIDto>? = null,
    
    @SerializedName("cached")
    val cached: List<CachedPOIDto>? = null,
    
    @SerializedName("searchHistory")
    val searchHistory: List<SearchHistoryDto>? = null
)
```

---

## 5. Configuración de Retrofit

### 5.1 `RetrofitClient.kt`

```kotlin
package com.tuempresa.mysyncapp.data.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tuempresa.mysyncapp.data.local.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    // ⚠️ CAMBIA ESTA URL POR LA DE TU BACKEND
    // Para desarrollo local (emulador): http://10.0.2.2:8080
    // Para dispositivo físico: http://TU_IP_LOCAL:8080
    // Para producción (Cloud Run): https://TU_CLOUD_RUN_URL
    private const val BASE_URL = "https://TU_CLOUD_RUN_URL/"
    
    // Para desarrollo local, usa:
    // private const val BASE_URL = "http://10.0.2.2:8080/"
    
    private val gson: Gson = GsonBuilder()
        .setLenient()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .create()
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        // ⚠️ OPCIONAL: Descomentar cuando el backend requiera autenticación
        // .addInterceptor(AuthInterceptor())
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    val apiService: SyncApiService = retrofit.create(SyncApiService::class.java)
}
```

### 5.2 `AuthInterceptor.kt` (Opcional)

```kotlin
package com.tuempresa.mysyncapp.data.network

import com.tuempresa.mysyncapp.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

// ⚠️ OPCIONAL: Solo necesario si el backend requiere autenticación
class AuthInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Obtener token del TokenManager
        val token = TokenManager.getToken()
        
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .build()
        } else {
            originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .build()
        }
        
        return chain.proceed(newRequest)
    }
}
```

---

## 6. Servicio de API

### 6.1 `SyncApiService.kt`

```kotlin
package com.tuempresa.mysyncapp.data.network

import com.tuempresa.mysyncapp.data.model.SyncRequest
import com.tuempresa.mysyncapp.data.model.SyncResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SyncApiService {
    
    /**
     * Envía cambios del dispositivo al servidor
     * 
     * @param request Datos a sincronizar (favoritos, caché, historial)
     * @return Response vacío si es exitoso
     */
    @POST("api/v1/sync/push")
    suspend fun pushChanges(@Body request: SyncRequest): Response<Unit>
    
    /**
     * Obtiene cambios del servidor desde la última sincronización
     * 
     * @param userId ID del usuario
     * @param lastSyncAt Fecha de última sincronización en formato ISO 8601 (opcional)
     * @return SyncResponse con los cambios del servidor
     */
    @GET("api/v1/sync/pull")
    suspend fun pullChanges(
        @Query("userId") userId: String,
        @Query("lastSyncAt") lastSyncAt: String? = null
    ): Response<SyncResponse>
}
```

---

## 7. Manejo de Autenticación JWT (Opcional)

> **Nota**: El backend actualmente no requiere autenticación. Esta sección es opcional y se puede omitir si no necesitas autenticación por ahora. Se incluye para referencia futura cuando se implemente Firebase Auth en el backend.

### 7.1 `TokenManager.kt` (Opcional)

```kotlin
package com.tuempresa.mysyncapp.data.local

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    
    private const val PREFS_NAME = "mysyncapp_prefs"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_USER_ID = "user_id"
    
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun saveToken(context: Context, token: String) {
        getSharedPreferences(context)
            .edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }
    
    fun getToken(context: Context? = null): String? {
        // Si no se pasa contexto, intenta obtenerlo de Application
        // Necesitarías inyectar Application o usar un patrón Singleton
        return if (context != null) {
            getSharedPreferences(context).getString(KEY_TOKEN, null)
        } else {
            // Implementar obtención de contexto global
            null
        }
    }
    
    fun saveUserId(context: Context, userId: String) {
        getSharedPreferences(context)
            .edit()
            .putString(KEY_USER_ID, userId)
            .apply()
    }
    
    fun getUserId(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_USER_ID, null)
    }
    
    fun clearToken(context: Context) {
        getSharedPreferences(context)
            .edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .apply()
    }
    
    fun isLoggedIn(context: Context): Boolean {
        return getToken(context) != null
    }
}
```

**Versión mejorada con Application Context**:

```kotlin
package com.tuempresa.mysyncapp.data.local

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class MySyncAppApplication : Application() {
    companion object {
        lateinit var instance: MySyncAppApplication
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}

object TokenManager {
    
    private const val PREFS_NAME = "mysyncapp_prefs"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_USER_ID = "user_id"
    
    private fun getSharedPreferences(): SharedPreferences {
        return MySyncAppApplication.instance.getSharedPreferences(
            PREFS_NAME, 
            Context.MODE_PRIVATE
        )
    }
    
    fun saveToken(token: String) {
        getSharedPreferences()
            .edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }
    
    fun getToken(): String? {
        return getSharedPreferences().getString(KEY_TOKEN, null)
    }
    
    fun saveUserId(userId: String) {
        getSharedPreferences()
            .edit()
            .putString(KEY_USER_ID, userId)
            .apply()
    }
    
    fun getUserId(): String? {
        return getSharedPreferences().getString(KEY_USER_ID, null)
    }
    
    fun clearToken() {
        getSharedPreferences()
            .edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .apply()
    }
    
    fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}
```

No olvides registrar la Application en `AndroidManifest.xml`:

```xml
<application
    android:name=".MySyncAppApplication"
    ...>
```

---

## 8. Repository Remoto

### 8.1 `RemoteSyncRepository.kt`

```kotlin
package com.tuempresa.mysyncapp.data.repository

import com.tuempresa.mysyncapp.data.model.SyncRequest
import com.tuempresa.mysyncapp.data.model.SyncResponse
import com.tuempresa.mysyncapp.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteSyncRepository {
    
    private val apiService = RetrofitClient.apiService
    
    /**
     * Envía cambios del dispositivo al servidor
     */
    suspend fun pushChanges(request: SyncRequest): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.pushChanges(request)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(
                        Exception("Error ${response.code()}: ${response.message()}")
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Obtiene cambios del servidor
     */
    suspend fun pullChanges(
        userId: String,
        lastSyncAt: String? = null
    ): Result<SyncResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.pullChanges(userId, lastSyncAt)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(
                        Exception("Error ${response.code()}: ${response.message()}")
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
```

---

## 9. Ejemplos de Uso

### 9.1 En un ViewModel

```kotlin
package com.tuempresa.mysyncapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuempresa.mysyncapp.data.model.*
import com.tuempresa.mysyncapp.data.repository.RemoteSyncRepository
import com.tuempresa.mysyncapp.data.local.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SyncViewModel : ViewModel() {
    
    private val repository = RemoteSyncRepository()
    
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState
    
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    /**
     * Sincronizar cambios con el servidor
     */
    fun syncChanges(
        deviceId: String,
        userId: String,
        favorites: List<FavoritePOIDto>? = null,
        cached: List<CachedPOIDto>? = null,
        searchHistory: List<SearchHistoryDto>? = null,
        lastSyncAt: LocalDateTime? = null
    ) {
        viewModelScope.launch {
            _syncState.value = SyncState.Loading
            
            // 1. Primero hacer pull para obtener cambios del servidor
            val pullResult = repository.pullChanges(
                userId = userId,
                lastSyncAt = lastSyncAt?.format(formatter)
            )
            
            pullResult.onSuccess { response ->
                // Procesar cambios recibidos del servidor
                // Aquí actualizarías tu base de datos local
                
                // 2. Luego hacer push para enviar cambios locales
                val request = SyncRequest(
                    deviceId = deviceId,
                    userId = userId,
                    lastSyncAt = lastSyncAt?.format(formatter),
                    favorites = favorites,
                    cached = cached,
                    searchHistory = searchHistory
                )
                
                val pushResult = repository.pushChanges(request)
                
                pushResult.onSuccess {
                    _syncState.value = SyncState.Success(response)
                }.onFailure { error ->
                    _syncState.value = SyncState.Error("Error al enviar cambios: ${error.message}")
                }
            }.onFailure { error ->
                _syncState.value = SyncState.Error("Error al obtener cambios: ${error.message}")
            }
        }
    }
    
    /**
     * Solo obtener cambios del servidor (pull)
     */
    fun pullChanges(userId: String, lastSyncAt: LocalDateTime? = null) {
        viewModelScope.launch {
            _syncState.value = SyncState.Loading
            
            val result = repository.pullChanges(
                userId = userId,
                lastSyncAt = lastSyncAt?.format(formatter)
            )
            
            result.onSuccess { response ->
                _syncState.value = SyncState.Success(response)
            }.onFailure { error ->
                _syncState.value = SyncState.Error(error.message ?: "Error desconocido")
            }
        }
    }
}

sealed class SyncState {
    object Idle : SyncState()
    object Loading : SyncState()
    data class Success(val response: SyncResponse) : SyncState()
    data class Error(val message: String) : SyncState()
}
```

### 9.2 En una Activity/Fragment

```kotlin
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var viewModel: SyncViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        viewModel = ViewModelProvider(this)[SyncViewModel::class.java]
        
        // Observar cambios de estado
        lifecycleScope.launch {
            viewModel.syncState.collect { state ->
                when (state) {
                    is SyncState.Loading -> {
                        // Mostrar loading
                    }
                    is SyncState.Success -> {
                        // Procesar respuesta
                        val favorites = state.response.favorites
                        val cached = state.response.cached
                        val searchHistory = state.response.searchHistory
                    }
                    is SyncState.Error -> {
                        // Mostrar error
                        Toast.makeText(this@MainActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    is SyncState.Idle -> {
                        // Estado inicial
                    }
                }
            }
        }
        
        // Ejemplo de sincronización
        buttonSync.setOnClickListener {
            val userId = TokenManager.getUserId() ?: return@setOnClickListener
            val deviceId = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ANDROID_ID
            )
            
            viewModel.syncChanges(
                deviceId = deviceId,
                userId = userId,
                favorites = getLocalFavorites(), // Obtener de tu BD local
                cached = getLocalCached(),      // Obtener de tu BD local
                searchHistory = getLocalSearchHistory() // Obtener de tu BD local
            )
        }
    }
}
```

---

## 10. Configuración de Variables

### 10.1 Crear `Config.kt` para URLs

```kotlin
package com.tuempresa.mysyncapp

object Config {
    // Cambiar según el entorno
    const val BASE_URL = when (BuildConfig.BUILD_TYPE) {
        "debug" -> "http://10.0.2.2:8080/" // Emulador
        // "debug" -> "http://192.168.1.100:8080/" // Dispositivo físico (cambiar IP)
        "release" -> "https://TU_CLOUD_RUN_URL/" // Producción
        else -> "https://TU_CLOUD_RUN_URL/"
    }
}
```

Y actualizar `RetrofitClient.kt`:

```kotlin
private const val BASE_URL = Config.BASE_URL
```

### 10.2 Formato de Fechas

El backend espera fechas en formato ISO 8601. Crea un helper:

```kotlin
package com.tuempresa.mysyncapp.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateUtils {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    fun toIsoString(dateTime: LocalDateTime): String {
        return dateTime.format(formatter)
    }
    
    fun fromIsoString(isoString: String): LocalDateTime {
        return LocalDateTime.parse(isoString, formatter)
    }
}
```

---

## ✅ Checklist de Implementación

- [ ] Dependencias agregadas en `build.gradle.kts`
- [ ] Permisos de Internet en `AndroidManifest.xml`
- [ ] Modelos de datos (DTOs) creados
- [ ] `RetrofitClient` configurado con URL correcta
- [ ] `SyncApiService` implementado
- [ ] `RemoteSyncRepository` creado
- [ ] ViewModel con lógica de sincronización
- [ ] UI conectada al ViewModel
- [ ] Probar conexión con backend local
- [ ] Probar conexión con backend en producción
- [ ] (Opcional) `AuthInterceptor` configurado - Solo si el backend requiere autenticación
- [ ] (Opcional) `TokenManager` implementado - Solo si el backend requiere autenticación

---

## 🔧 Solución de Problemas

### Error: "Unable to resolve host"
- Verifica que la URL en `RetrofitClient` sea correcta
- Para emulador: usa `http://10.0.2.2:8080`
- Para dispositivo físico: usa la IP local de tu máquina (ej: `http://192.168.1.100:8080`)

### Error: "Cleartext HTTP traffic not permitted"
- Agrega `android:usesCleartextTraffic="true"` en `AndroidManifest.xml` (solo desarrollo)
- O configura `network_security_config.xml` correctamente

### Error 401 Unauthorized
- **Nota**: El backend actualmente no requiere autenticación. Si recibes este error:
  - Verifica que el backend esté configurado correctamente (SecurityConfig.java)
  - Si implementaste autenticación, verifica que el token JWT esté siendo enviado correctamente
  - Revisa que `AuthInterceptor` esté agregado al `OkHttpClient` (si es necesario)
  - Verifica que el token no haya expirado

### Error de formato de fecha
- Asegúrate de usar formato ISO 8601: `yyyy-MM-dd'T'HH:mm:ss`
- Usa `DateTimeFormatter.ISO_LOCAL_DATE_TIME` para consistencia

---

## 📚 Recursos Adicionales

- [Retrofit Documentation](https://square.github.io/retrofit/)
- [OkHttp Documentation](https://square.github.io/okhttp/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Android Networking Best Practices](https://developer.android.com/training/basics/network-ops)

---

## 🔐 Implementación Futura: Firebase Auth en el Backend

### Para el Backend (Spring Boot)

Cuando quieras implementar validación de Firebase Auth en el backend, necesitarás:

1. **Agregar dependencia Firebase Admin SDK** en `pom.xml`:
```xml
<dependency>
    <groupId>com.google.firebase</groupId>
    <artifactId>firebase-admin</artifactId>
    <version>9.2.0</version>
</dependency>
```

2. **Crear configuración de Firebase**:
```java
@Configuration
public class FirebaseConfig {
    @PostConstruct
    public void initialize() {
        try {
            FileInputStream serviceAccount = 
                new FileInputStream("path/to/serviceAccountKey.json");
            
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
            
            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            throw new RuntimeException("Error inicializando Firebase", e);
        }
    }
}
```

3. **Crear filtro de autenticación Firebase**:
```java
@Component
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) {
        String token = extractToken(request);
        
        if (token != null) {
            try {
                FirebaseToken decodedToken = FirebaseAuth.getInstance()
                    .verifyIdToken(token);
                
                // Establecer autenticación en SecurityContext
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        decodedToken.getUid(), 
                        null, 
                        new ArrayList<>()
                    );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                // Token inválido
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

4. **Actualizar SecurityConfig.java**:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private FirebaseAuthenticationFilter firebaseAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/sync/**").authenticated() // ← Requerir autenticación
                .anyRequest().permitAll()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(firebaseAuthenticationFilter, 
                           UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

### Para Android

Cuando el backend requiera autenticación Firebase:

1. **Agregar dependencia Firebase Auth** en `app/build.gradle.kts`:
```kotlin
dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
}
```

2. **Obtener token de Firebase**:
```kotlin
FirebaseAuth.getInstance().currentUser?.getIdToken(true)
    ?.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val token = task.result?.token
            TokenManager.saveToken(token)
        }
    }
```

3. **Descomentar AuthInterceptor** en `RetrofitClient.kt`

---

**Última actualización**: Diciembre 2024

