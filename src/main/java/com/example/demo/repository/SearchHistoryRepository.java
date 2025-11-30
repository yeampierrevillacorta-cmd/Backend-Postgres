package com.example.demo.repository;

import com.example.demo.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    List<SearchHistory> findByUserIdAndDeletedFalse(String userId);
    List<SearchHistory> findByUserIdAndDeviceIdAndDeletedFalse(String userId, String deviceId);
    
    @Query("SELECT s FROM SearchHistory s WHERE s.userId = :userId AND s.createdAt > :lastSyncAt AND s.deleted = false")
    List<SearchHistory> findByUserIdAndCreatedAtAfter(@Param("userId") String userId, @Param("lastSyncAt") LocalDateTime lastSyncAt);
}

