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
    private static final String CACHE_NAME = "fileIdCache";

    @CacheEvict(value = "permission",  key = "#username")
    public void clearPermissionCache(String username) {
        log.info("clearPermissionCache: {}", username);
    }

    @CacheEvict(value = "permission", allEntries = true)
    public void clearPermissionCache() {
        log.info("clearPermissionCache");
    }

    /**
     * 存入指定的 ID 到缓存
     * 使用 @CachePut：方法总会执行，并将结果放入缓存，适合“更新”或“强制写入”
     *
     * @param key 缓存的键（必须非空）
     * @param id  要存储的 ID（建议非空校验）
     * @return 存入的 ID
     */
    @CachePut(value = CACHE_NAME, key = "#key")
    public Integer putId(String key, Integer id) {
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException("Cache key must not be null or empty");
        }
        if (id == null) {  // 修改：检查Integer是否为null
            log.warn("Attempting to cache a null ID for key: {}", key);
            throw new IllegalArgumentException("ID to cache must not be null");
        }

        log.info("Caching ID: {} for key: {}", id, key);
        return id;
    }

    /**
     * 从缓存中获取 ID
     * 使用 @Cacheable：优先从缓存读取，命中则返回，否则执行方法体（但这里返回 null 表示未找到）
     *
     * @param key 缓存的键
     * @return 缓存中的 ID，若不存在则返回 null
     */
    @Cacheable(value = CACHE_NAME, key = "#key")
    public Integer getId(String key) {
        if (!StringUtils.hasText(key)) {
            log.warn("Attempt to get cache with invalid key: {}", key);
            return null;
        }
        log.debug("Cache miss for key: {}", key);
        return null; // 缓存未命中时返回null
    }

    /**
     * 根据键值从缓存中移除指定的 ID
     *
     * @param key 缓存的键
     */
    @CacheEvict(value = CACHE_NAME, key = "#key")
    public void removeId(String key) {
        if (!StringUtils.hasText(key)) {
            log.warn("Invalid key provided for removing from cache: {}", key);
            return;
        }
        log.info("Removing ID from cache for key: {}", key);
    }


//    @CacheEvict(value = "permission", key = "#username")
//    public void clearPermissionCache(String username) {
//        log.info("clearPermissionCache");
//    }

}