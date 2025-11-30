package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncResponse {
    private LocalDateTime serverTimestamp;
    private List<FavoritePOIDto> favorites;
    private List<CachedPOIDto> cached;
    private List<SearchHistoryDto> searchHistory;
}

