package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "poi_cached")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CachedPOI {
    
    @Id
    @Column(name = "poi_id")
    private String poiId;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private String nombre;
    
    private String descripcion;
    private String categoria;
    private String direccion;
    private Double lat;
    private Double lon;
    private Double calificacion;
    
    @Column(name = "imagen_url")
    private String imagenUrl;
    
    @Column(name = "cached_at")
    private LocalDateTime cachedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @PrePersist
    protected void onCreate() {
        cachedAt = LocalDateTime.now();
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusDays(7); // Expira en 7 días por defecto
        }
    }
}

