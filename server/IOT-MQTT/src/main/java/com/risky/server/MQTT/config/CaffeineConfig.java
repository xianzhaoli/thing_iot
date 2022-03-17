package com.risky.server.MQTT.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CaffeineConfig {

    /**
     * 配置缓存管理器
     *
     * @return 缓存管理器
     */
    /**
     * 配置缓存管理器
     *
     * @return 缓存管理器
     */
    @Bean("caffeineCacheManager")
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                // 设置最后一次写入或访问后经过固定时间过期
                .expireAfterAccess(10, TimeUnit.MINUTES)
                // 初始的缓存空间大小
                .initialCapacity(100));

        return cacheManager;
    }

    /*@Bean("caffeineCacheManager")
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        ArrayList<CaffeineCache> caches = new ArrayList<CaffeineCache>();
        caches.add(new CaffeineCache("connection_client",
                Caffeine.newBuilder()
                        .expireAfterAccess(1, TimeUnit.HOURS)
                        .initialCapacity(100)
                        .build()
        ));
        caches.add(new CaffeineCache("topic",
                Caffeine.newBuilder()
                        .expireAfterAccess(30, TimeUnit.MINUTES)
                        .initialCapacity(100)
                        .build()
        ));
        caches.add(new CaffeineCache("retain",
                Caffeine.newBuilder()
                        .expireAfterAccess(30, TimeUnit.MINUTES)
                        .initialCapacity(100)
                        .build()
        ));
        return cacheManager;
    }*/

}
