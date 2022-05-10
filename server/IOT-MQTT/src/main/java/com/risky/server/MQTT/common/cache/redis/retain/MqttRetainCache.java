package com.risky.server.MQTT.common.cache.redis.retain;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.risky.server.MQTT.common.cache.redis.MqttRedisCache;
import com.risky.server.MQTT.util.MqttTopicFilter;
import com.risky.server.MQTT.util.MqttTopicFilterFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 保留消息
 */
@Component
@Slf4j
public class MqttRetainCache extends MqttRedisCache<MqttRetain> {

    private final static String RETAIN_KEY_REDIS = "SERVER:MQTT:RETAIN:";

    //单位小时 //-1永不失效
    private final static int retainMessageTimeOut = 24 * 7;

    /**
     * 存放保留消息
     */
    private static Map<String,MqttRetain> map;

    /**
     * 保留消息
     * @param mqttRetain
     */
    public MqttRetain setRetainMessage(MqttRetain mqttRetain){
        redisTemplate.opsForValue().set(RETAIN_KEY_REDIS + mqttRetain.getTopic(),mqttRetain,retainMessageTimeOut, TimeUnit.HOURS);
        map.put(mqttRetain.getTopic(),mqttRetain);
        return mqttRetain;
    }

    /**
     * 获取redis里持久化的保留消息（服务重启）
     */
    @PostConstruct
    public void loadRedisCacheRetainMessage(){
        Set<String> retainTopics = scan(RETAIN_KEY_REDIS);
        //初始化map大小为当前持久化的保留消息的个数
        map = new ConcurrentHashMap<>(retainTopics.size());
        log.info("--------------加载redis中保留消息-----------------保留消息数【{}】",retainTopics.size());
        List<List<String>> partedList = Lists.partition(new ArrayList<>(retainTopics),500);
        for (List<String> list : partedList) {
            List<MqttRetain> mqttRetains = redisTemplate.opsForValue().multiGet(list);
            for (MqttRetain mqttRetain : mqttRetains) {
                map.put(mqttRetain.getTopic(),mqttRetain);
            }
        }
    }

    /**
     * 删除保留消息
     * @param topic
     */
    public void removeRetainMessage(String topic){
        map.remove(topic);
        redisTemplate.delete(RETAIN_KEY_REDIS + topic);
    }


    public List<MqttRetain> getRetain(String topic){
        MqttTopicFilter mqttTopicFilter = MqttTopicFilterFactory.toFilter(topic);
        return map.entrySet().stream()
                .filter(stringMqttRetainEntry -> mqttTopicFilter.filter(stringMqttRetainEntry.getKey()))
                .map(map -> map.getValue())
                .collect(Collectors.toList());
    }

}
