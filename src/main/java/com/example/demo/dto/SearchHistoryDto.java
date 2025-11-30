package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryDto {
    private Long id;
    private String userId;
    private String deviceId;
    private String searchQuery;
    private String searchType;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
    private Boolean deleted;
}

