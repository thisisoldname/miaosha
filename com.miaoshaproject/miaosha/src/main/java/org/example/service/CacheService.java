package org.example.service;

/***************************
 *Author:ct
 *Time:2020/4/19 2:08
 *Dec:本地缓存操作
 ****************************/
public interface CacheService {

    //存
    void setCommonCache(String key, Object value);

    //取
    Object getFromCommonCache(String key);
}
