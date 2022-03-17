package com.risky.server.MQTT.common.cache.redis.retain;

import com.risky.server.MQTT.common.cache.redis.MqttRedisCache;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 保留消息
 */
@Component
@CacheConfig(cacheNames = "retain",cacheManager = "caffeineCacheManager")
public class MqttRetainCache extends MqttRedisCache<MqttRetain> {

    //单位小时 //-1永不失效
    private final static int retainMessageTimeOut = 24;

    private final static String RETAIN_KEY_REDIS = "SERVER:MQTT:RETAIN:";


    /**
     * 保留消息
     * @param mqttRetain
     */
    @CachePut(key = "#mqttRetain.topic")
    public MqttRetain setRetainMessage(MqttRetain mqttRetain){
        redisTemplate.opsForValue().set(RETAIN_KEY_REDIS + mqttRetain.getTopic(),mqttRetain,retainMessageTimeOut, TimeUnit.HOURS);
        return mqttRetain;
    }

    @Cacheable(key = "#topic")
    public MqttRetain getRetain(String topic){
        /*Set<String> retainTopics = scan(RETAIN_KEY_REDIS);
        Set<String> match = filterTetainTopic(topic,retainTopics).parallelStream().map(s -> RETAIN_KEY_REDIS + s).collect(Collectors.toSet());
        if(match.isEmpty()){
            return Collections.emptyList();
        }
        return redisTemplate.opsForValue().multiGet(match);*/
        return (MqttRetain) redisTemplate.opsForValue().get(topic);
    }

}
