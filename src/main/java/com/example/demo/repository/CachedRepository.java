package com.example.demo.repository;

import com.example.demo.entity.CachedPOI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CachedRepository extends JpaRepository<CachedPOI, String> {
    List<CachedPOI> findByUserId(String userId);
    Optional<CachedPOI> findByPoiIdAndUserId(String poiId, String userId);
    
    @Query("SELECT c FROM CachedPOI c WHERE c.userId = :userId AND c.cachedAt > :lastSyncAt")
    List<CachedPOI> findByUserIdAndCachedAtAfter(@Param("userId") String userId, @Param("lastSyncAt") LocalDateTime lastSyncAt);
    
    @Query("SELECT c FROM CachedPOI c WHERE c.expiresAt < :now")
    List<CachedPOI> findExpired(@Param("now") LocalDateTime now);
}

