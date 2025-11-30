package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncRequest {
    private String deviceId;
    private String userId;
    private LocalDateTime lastSyncAt;
    private List<FavoritePOIDto> favorites;
    private List<CachedPOIDto> cached;
    private List<SearchHistoryDto> searchHistory;
}

