package com.langhuan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class CacheService {
    @CacheEvict(value = "permission",  key = "#username")
    public void clearPermissionCache(String username) {
        log.info("clearPermissionCache: {}", username);
    }

    @CacheEvict(value = "permission", allEntries = true)
    public void clearPermissionCache() {
        log.info("clearPermissionCache");
    }
}