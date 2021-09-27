package com.risky.server.MQTT.message;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author ：xianzhaoli
 * @date ：Created in 2021/9/26 下午11:17
 * @description：消息持久化存储
 * @modified By：`
 * @version: 1.0
 */
@Component
public class RedisMessagePersistent {

    @Resource(name = "redisCacheTemplate")
    private RedisTemplate redisTemplate;

    private final static String retryPublishMessageKey_PUBLISH = "MQTT:RETRY:PUBLISH:"; //QOS = 1

    private final static String retryPublishMessageKey_PUBREL = "MQTT:RETRY:PUBREL:"; //QOS = 2

    private final static String CLEAN_SESSION_STORE = "MQTT:CLEAN_SESSION_MESSAGE:";

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
    /**
     * OQS = 2
     * @param clientId
     * @param messageId
     */
    public void putPubRelMessage(String clientId,MessageId messageId){
        redisTemplate.opsForHash().put(retryPublishMessageKey_PUBREL + clientId,String.valueOf(messageId.getMessageId()),messageId); //放入消息
    }
    /**
     * OQS = 2
     * @param clientId
     * @param messageId
     */
    public void removePubRelMessage(String clientId,Integer messageId){
        redisTemplate.opsForHash().delete(retryPublishMessageKey_PUBREL + clientId,String.valueOf(messageId)); //放入消息
    }

    /**
     * 离线消息存储
     * @param clientId
     * @param messageRetry
     */
    public void putCleanSessionMessage(String clientId,MessageRetry messageRetry){
        redisTemplate.opsForList().rightPush(CLEAN_SESSION_STORE + clientId,messageRetry);
    }

    /**
     * 获取并删除所有消息
     * @param clientId
     * @return
     */
    public List<MessageRetry> getAndRemoveOfflineMessage(String clientId){
        if(!redisTemplate.hasKey(CLEAN_SESSION_STORE + clientId)){
            return null;
        }
        List<MessageRetry> list = (List<MessageRetry>)redisTemplate.opsForList().range(CLEAN_SESSION_STORE + clientId,0 , -1);
        redisTemplate.opsForList().remove(clientId,0,-1); //此处可能有并发问题
        return list;
    }
    /**
     * 获取指定topic的消息并删除
     * @param clientId
     * @return
     */
    public List<MessageRetry> getAndRemoveOfflineMessage(String clientId,String topicName){
        if(!redisTemplate.hasKey(CLEAN_SESSION_STORE + clientId)){
            return null;
        }
        List<MessageRetry> list = (List<MessageRetry>)redisTemplate.opsForList().range(CLEAN_SESSION_STORE + clientId,0 , -1);
        List<MessageRetry> filterList = list.parallelStream().filter(messageRetry -> {
            boolean rs = messageRetry.getTopic().equals(topicName);
            if(rs){
                redisTemplate.opsForList().remove(CLEAN_SESSION_STORE + clientId,0,messageRetry);
            }
            return rs;
        }).collect(Collectors.toList());
        return filterList;
    }

}
