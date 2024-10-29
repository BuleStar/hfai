package com.hf.webflux.hfai.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

/**
 *  预热缓存 4
 */
@Component
public class MyApplicationRunner implements ApplicationRunner {
    @Value("${cache.key}")
    private String cacheKey;

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 设置缓存键值对
        reactiveRedisTemplate.opsForValue().set(cacheKey, "value")
                .doOnError(e -> {
                    // 记录日志或采取其他措施
                    System.err.println("Failed to set cache: " + e.getMessage());
                })
                .then() // 确保在设置缓存后继续执行其他操作
                .subscribe();
    }
}
