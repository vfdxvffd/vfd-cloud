package com.vfd.demo.service;

import java.util.Map;

/**
 * @PackageName: com.vfd.demo.service
 * @ClassName: RedisService
 * @Description:
 * @author: vfdxvffd
 * @date: 12/5/20 7:07 PM
 */
public interface RedisService {

    /**
     * 普通缓存放入并设置时间
     * @param key 键
     * @param value 值
     * @param time 时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    boolean set(String key, Object value, long time);

    /**
     * 普通缓存获取
     * @param key 键
     * @return 值
     */
    Object get(String key);

    /**
     * 删除缓存
     * @param key 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    void del(String... key);

    /**
     * 普通缓存放入
     * @param key 键
     * @param value 值
     * @return true成功 false失败
     */
    boolean set(String key, Object value);

    /**
     * HashSet 并设置时间
     * @param key 键
     * @param map 对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    boolean hmset(String key, Map<String, Object> map, long time);

    /**
     * 判断key是否存在
     * @param key 键
     * @return true 存在 false不存在
     */
    boolean hasKey(String key);

    /**
     * 获取hashKey对应的所有键值
     * @param key 键
     * @return 对应的多个键值
     */
    Map<Object, Object> hmget(String key);

    /**
     * 根据key 获取过期时间
     * @param key 键 不能为null
     * @return 时间(秒) 返回-1代表为永久有效
     */
    long getExpire(String key);
}