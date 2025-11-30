package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "poi_favorites")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoritePOI {
    
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
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted")
    private Boolean deleted = false;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

