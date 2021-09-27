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

    private final static String retryPublishMessageKey_PUBLISH = "MQTT:RETRY:PUBLISH:"; //QOS = 1

    private final static String retryPublishMessageKey_PUBREL = "MQTT:RETRY:PUBREL:"; //QOS = 2

    /**
     * QOS = 1
     * @param clientId
     * @param messageRetry
     */
    public void putRetryMessage(String clientId,MessageRetry messageRetry){
        redisTemplate.opsForHash().put(retryPublishMessageKey_PUBLISH + clientId,String.valueOf(messageRetry.getMessageId()),messageRetry); //放入消息
    }

    /**
     * OQS = 1
     * @param clientId
     * @param messageId
     */
    public void removeRetryMessage(String clientId,Integer messageId){
        redisTemplate.opsForHash().delete(retryPublishMessageKey_PUBLISH + clientId,messageId.toString());
    }

    public void putPubRelMessage(String clientId,MessageId messageId){
        redisTemplate.opsForHash().put(retryPublishMessageKey_PUBREL + clientId,String.valueOf(messageId.getMessageId()),messageId); //放入消息
    }


    public void removePubRelMessage(String clientId,Integer messageId){
        redisTemplate.opsForHash().delete(retryPublishMessageKey_PUBREL + clientId,String.valueOf(messageId)); //放入消息
    }
}
