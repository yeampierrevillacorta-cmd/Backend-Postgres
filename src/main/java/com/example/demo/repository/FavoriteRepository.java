package com.example.demo.repository;

import com.example.demo.entity.FavoritePOI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<FavoritePOI, String> {
    List<FavoritePOI> findByUserIdAndDeletedFalse(String userId);
    Optional<FavoritePOI> findByPoiIdAndUserId(String poiId, String userId);
    long countByUserIdAndDeletedFalse(String userId);
    
    @Query("SELECT f FROM FavoritePOI f WHERE f.userId = :userId AND f.updatedAt > :lastSyncAt AND f.deleted = false")
    List<FavoritePOI> findByUserIdAndUpdatedAtAfter(@Param("userId") String userId, @Param("lastSyncAt") LocalDateTime lastSyncAt);
}

