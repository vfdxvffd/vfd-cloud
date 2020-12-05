package com.vfd.demo.service;

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
}