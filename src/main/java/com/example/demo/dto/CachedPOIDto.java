package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CachedPOIDto {
    private String poiId;
    private String userId;
    private String nombre;
    private String descripcion;
    private String categoria;
    private String direccion;
    private Double lat;
    private Double lon;
    private Double calificacion;
    private String imagenUrl;
    private LocalDateTime cachedAt;
    private LocalDateTime expiresAt;
}

