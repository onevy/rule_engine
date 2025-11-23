package com.example.ruleengine.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置类
 * 使用Caffeine作为本地缓存实现
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 缓存名称常量
     */
    public static final String CACHE_RULE_DETAIL = "ruleDetail";
    public static final String CACHE_RULE_LIST = "ruleList";
    public static final String CACHE_SCENE_METADATA = "sceneMetadata";
    public static final String CACHE_FIELD_METADATA = "fieldMetadata";
    public static final String CACHE_RULE_GROUP = "ruleGroup";

    /**
     * 配置Caffeine缓存管理器
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                // 初始容量
                .initialCapacity(100)
                // 最大缓存条目数
                .maximumSize(1000)
                // 写入后过期时间（30分钟）
                .expireAfterWrite(30, TimeUnit.MINUTES)
                // 访问后过期时间（10分钟）
                .expireAfterAccess(10, TimeUnit.MINUTES)
                // 开启统计
                .recordStats());
        return cacheManager;
    }

    /**
     * 规则详情缓存配置（较长过期时间）
     */
    @Bean
    public Caffeine<Object, Object> ruleDetailCacheConfig() {
        return Caffeine.newBuilder()
                .initialCapacity(50)
                .maximumSize(500)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .recordStats();
    }

    /**
     * 元数据缓存配置（很少变化，可以缓存更长时间）
     */
    @Bean
    public Caffeine<Object, Object> metadataCacheConfig() {
        return Caffeine.newBuilder()
                .initialCapacity(20)
                .maximumSize(100)
                .expireAfterWrite(120, TimeUnit.MINUTES)
                .recordStats();
    }
}
