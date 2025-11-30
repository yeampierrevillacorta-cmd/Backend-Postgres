package com.example.demo.controller;

import com.example.demo.dto.SyncRequest;
import com.example.demo.dto.SyncResponse;
import com.example.demo.service.SyncService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/sync")
public class SyncController {
    
    @Autowired
    private SyncService syncService;
    
    @PostMapping("/push")
    public ResponseEntity<?> pushChanges(@Valid @RequestBody SyncRequest request) {
        try {
            syncService.pushChanges(request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al sincronizar cambios: " + e.getMessage());
        }
    }
    
    @GetMapping("/pull")
    public ResponseEntity<SyncResponse> pullChanges(
            @RequestParam String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastSyncAt
    ) {
        try {
            SyncResponse response = syncService.pullChanges(userId, lastSyncAt);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

