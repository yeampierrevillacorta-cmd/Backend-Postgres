package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.CachedPOI;
import com.example.demo.entity.FavoritePOI;
import com.example.demo.entity.SearchHistory;
import com.example.demo.repository.CachedRepository;
import com.example.demo.repository.FavoriteRepository;
import com.example.demo.repository.SearchHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SyncService {
    
    @Autowired
    private FavoriteRepository favoriteRepository;
    
    @Autowired
    private CachedRepository cachedRepository;
    
    @Autowired
    private SearchHistoryRepository searchHistoryRepository;
    
    public SyncResponse pullChanges(String userId, LocalDateTime lastSyncAt) {
        List<FavoritePOI> favoriteChanges = new ArrayList<>();
        List<CachedPOI> cachedChanges = new ArrayList<>();
        List<SearchHistory> searchHistoryChanges = new ArrayList<>();
        
        if (lastSyncAt != null) {
            favoriteChanges = favoriteRepository.findByUserIdAndUpdatedAtAfter(userId, lastSyncAt);
            cachedChanges = cachedRepository.findByUserIdAndCachedAtAfter(userId, lastSyncAt);
            searchHistoryChanges = searchHistoryRepository.findByUserIdAndCreatedAtAfter(userId, lastSyncAt);
        } else {
            // Primera sincronización - traer todo
            favoriteChanges = favoriteRepository.findByUserIdAndDeletedFalse(userId);
            cachedChanges = cachedRepository.findByUserId(userId);
            searchHistoryChanges = searchHistoryRepository.findByUserIdAndDeletedFalse(userId);
        }
        
        return SyncResponse.builder()
                .serverTimestamp(LocalDateTime.now())
                .favorites(favoriteChanges.stream().map(this::mapFavoriteToDto).collect(Collectors.toList()))
                .cached(cachedChanges.stream().map(this::mapCachedToDto).collect(Collectors.toList()))
                .searchHistory(searchHistoryChanges.stream().map(this::mapSearchHistoryToDto).collect(Collectors.toList()))
                .build();
    }
    
    @Transactional
    public void pushChanges(SyncRequest request) {
        // Sincronizar favoritos
        if (request.getFavorites() != null) {
            for (FavoritePOIDto dto : request.getFavorites()) {
                FavoritePOI entity = mapFavoriteDtoToEntity(dto);
                favoriteRepository.save(entity);
            }
        }
        
        // Sincronizar caché
        if (request.getCached() != null) {
            for (CachedPOIDto dto : request.getCached()) {
                CachedPOI entity = mapCachedDtoToEntity(dto);
                cachedRepository.save(entity);
            }
        }
        
        // Sincronizar historial de búsqueda
        if (request.getSearchHistory() != null) {
            for (SearchHistoryDto dto : request.getSearchHistory()) {
                SearchHistory entity = mapSearchHistoryDtoToEntity(dto);
                searchHistoryRepository.save(entity);
            }
        }
    }
    
    // Mappers de Entity a DTO
    private FavoritePOIDto mapFavoriteToDto(FavoritePOI entity) {
        FavoritePOIDto dto = new FavoritePOIDto();
        dto.setPoiId(entity.getPoiId());
        dto.setUserId(entity.getUserId());
        dto.setNombre(entity.getNombre());
        dto.setDescripcion(entity.getDescripcion());
        dto.setCategoria(entity.getCategoria());
        dto.setDireccion(entity.getDireccion());
        dto.setLat(entity.getLat());
        dto.setLon(entity.getLon());
        dto.setCalificacion(entity.getCalificacion());
        dto.setImagenUrl(entity.getImagenUrl());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setDeleted(entity.getDeleted());
        return dto;
    }
    
    private CachedPOIDto mapCachedToDto(CachedPOI entity) {
        CachedPOIDto dto = new CachedPOIDto();
        dto.setPoiId(entity.getPoiId());
        dto.setUserId(entity.getUserId());
        dto.setNombre(entity.getNombre());
        dto.setDescripcion(entity.getDescripcion());
        dto.setCategoria(entity.getCategoria());
        dto.setDireccion(entity.getDireccion());
        dto.setLat(entity.getLat());
        dto.setLon(entity.getLon());
        dto.setCalificacion(entity.getCalificacion());
        dto.setImagenUrl(entity.getImagenUrl());
        dto.setCachedAt(entity.getCachedAt());
        dto.setExpiresAt(entity.getExpiresAt());
        return dto;
    }
    
    private SearchHistoryDto mapSearchHistoryToDto(SearchHistory entity) {
        SearchHistoryDto dto = new SearchHistoryDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setDeviceId(entity.getDeviceId());
        dto.setSearchQuery(entity.getSearchQuery());
        dto.setSearchType(entity.getSearchType());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setDeleted(entity.getDeleted());
        return dto;
    }
    
    // Mappers de DTO a Entity
    private FavoritePOI mapFavoriteDtoToEntity(FavoritePOIDto dto) {
        FavoritePOI entity = favoriteRepository.findByPoiIdAndUserId(dto.getPoiId(), dto.getUserId())
                .orElse(new FavoritePOI());
        
        entity.setPoiId(dto.getPoiId());
        entity.setUserId(dto.getUserId());
        entity.setNombre(dto.getNombre());
        entity.setDescripcion(dto.getDescripcion());
        entity.setCategoria(dto.getCategoria());
        entity.setDireccion(dto.getDireccion());
        entity.setLat(dto.getLat());
        entity.setLon(dto.getLon());
        entity.setCalificacion(dto.getCalificacion());
        entity.setImagenUrl(dto.getImagenUrl());
        entity.setDeleted(dto.getDeleted() != null ? dto.getDeleted() : false);
        
        if (entity.getCreatedAt() == null && dto.getCreatedAt() != null) {
            entity.setCreatedAt(dto.getCreatedAt());
        }
        
        return entity;
    }
    
    private CachedPOI mapCachedDtoToEntity(CachedPOIDto dto) {
        CachedPOI entity = cachedRepository.findByPoiIdAndUserId(dto.getPoiId(), dto.getUserId())
                .orElse(new CachedPOI());
        
        entity.setPoiId(dto.getPoiId());
        entity.setUserId(dto.getUserId());
        entity.setNombre(dto.getNombre());
        entity.setDescripcion(dto.getDescripcion());
        entity.setCategoria(dto.getCategoria());
        entity.setDireccion(dto.getDireccion());
        entity.setLat(dto.getLat());
        entity.setLon(dto.getLon());
        entity.setCalificacion(dto.getCalificacion());
        entity.setImagenUrl(dto.getImagenUrl());
        entity.setExpiresAt(dto.getExpiresAt());
        
        return entity;
    }
    
    private SearchHistory mapSearchHistoryDtoToEntity(SearchHistoryDto dto) {
        SearchHistory entity = dto.getId() != null 
                ? searchHistoryRepository.findById(dto.getId()).orElse(new SearchHistory())
                : new SearchHistory();
        
        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }
        entity.setUserId(dto.getUserId());
        entity.setDeviceId(dto.getDeviceId());
        entity.setSearchQuery(dto.getSearchQuery());
        entity.setSearchType(dto.getSearchType());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        entity.setDeleted(dto.getDeleted() != null ? dto.getDeleted() : false);
        
        return entity;
    }
}

