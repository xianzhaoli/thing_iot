package com.risky.server.MQTT.message;

import com.risky.server.MQTT.common.MqttStoreService;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RetainMessage {

    private static final String RETAIN_MESSAGE_KEY = "MQTT:RETAIN:";

    @Resource(name = "redisCacheTemplate")
    private RedisTemplate redisTemplate;

    @Resource
    private MqttStoreService mqttStoreService;

    /**
     * 保存Retain message
     * @param topic
     * @param messageRetry
     */
    public void putRetainMessage(String topic,MessageRetry messageRetry){
        redisTemplate.opsForValue().set(RETAIN_MESSAGE_KEY + topic,messageRetry);
    }

    public MessageRetry getRetainMessage(String topic){
        return (MessageRetry) redisTemplate.opsForValue().get(RETAIN_MESSAGE_KEY + topic);
    }

    public Set<String> redisScan(final String key){
        Set<String> keys = (Set<String>) redisTemplate.execute((RedisCallback) redisConnection -> {
            Set<String> set = new HashSet<>();
            Cursor<byte[]> cursor = redisConnection.scan(new ScanOptions.ScanOptionsBuilder().match(key + "*").count(1000).build());
            while (cursor.hasNext()) {
                set.add(new String(cursor.next()).replace(RETAIN_MESSAGE_KEY,""));
            }
            return set;
        });
        return keys;
    }


    public List<String> matcherRetainMessage(String subTopic){
        Set<String> topics = redisScan(RETAIN_MESSAGE_KEY);
        return topics.parallelStream()
                .filter(retainTopic -> mqttStoreService.topicMatcher(retainTopic,subTopic))
                .collect(Collectors.toList());
    }





}
