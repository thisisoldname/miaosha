package org.example.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.example.service.CacheService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/***************************
 *Author:ct
 *Time:2020/4/19 2:09
 *Dec:Todo
 ****************************/
@Service
public class CacheServiceImpl implements CacheService {

    private Cache<String, Object> commonCache = null;

    @PostConstruct
    public void init() {
        commonCache = CacheBuilder.newBuilder()
                .initialCapacity(10)
                .maximumSize(100)
                .expireAfterWrite(30, TimeUnit.SECONDS).build();
    }
    @Override
    public void setCommonCache(String key, Object value) {
        commonCache.put(key, value);
    }

    @Override
    public Object getFromCommonCache(String key) {
        return commonCache.getIfPresent(key);
    }
}