package com.alok.spring.batch.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CacheService {
    private CacheManager cacheManager;

    public CacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void evictAllCache() {
        log.info("Evicting all caches!");
        cacheManager.getCacheNames().stream().forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    }

    public void evictCacheByName(String cacheName) {
        log.info("Evicting cache: {}", cacheName);
        cacheManager.getCache(cacheName).clear();
    }
}
