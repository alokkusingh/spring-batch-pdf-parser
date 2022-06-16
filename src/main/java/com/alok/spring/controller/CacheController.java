package com.alok.spring.controller;

import com.alok.spring.annotation.LogExecutionTime;
import com.alok.spring.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/cache")
public class CacheController {

    private CacheService cacheService;

    public CacheController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @LogExecutionTime
    @GetMapping("/evict-all")
    public ResponseEntity<Resource> evictAllCache() {
        log.info("Evicting all caches");

        cacheService.evictAllCaches();

        return ResponseEntity.accepted().build();

    }
}
