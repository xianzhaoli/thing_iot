package com.risky.server.MQTT.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author ：xianzhaoli
 * @date ：Created in 2021/9/26 下午11:17
 * @description：消息持久化存储
 * @modified By：`
 * @version: 1.0
 */
@Component
public class RedisMessagePersistent {

    @Autowired
    private RedisTemplate redisTemplate;

    private final static String retryPublishMessageKey = "MQTT:RETRY:PUBLISH:";

    public void putRetryMessage(String clientId,MessageRetry messageRetry){
        redisTemplate.opsForHash().put(clientId,String.valueOf(messageRetry.getMessageId()),messageRetry); //放入消息
    }

    public void removeRetryMessage(String clientId,Integer messageId){
        redisTemplate.opsForHash().delete(clientId,messageId.toString());
    }
}
