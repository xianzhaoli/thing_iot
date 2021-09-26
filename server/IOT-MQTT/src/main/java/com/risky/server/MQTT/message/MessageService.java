package com.risky.server.MQTT.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author ：xianzhaoli
 * @date ：Created in 2021/9/25 下午6:16
 * @description：消息唯一ID生成
 * @modified By：`
 * @version: 1.0
 */
@Component
@Slf4j
public class MessageService {

    private static final int INIT_MESSAGE_ID = 1;

    private static final int MAX_MESSAGE_ID = 65535;

    private static final long RELEASE_TIMEOUT = 1000 * 60 * 2; //messageID 两分钟没有回复就释放

    private Map<String, List<MessageId>> lockedMessageId = new ConcurrentHashMap<>(1000);

    private Map<String,Integer> clientTopicMessageId = new ConcurrentHashMap<>(1000);

    public int getMessageId(final String clientId){
        long start = System.currentTimeMillis();
        if(!clientTopicMessageId.containsKey(clientId)){
            clientTopicMessageId.put(clientId,INIT_MESSAGE_ID);
        }else{
            clientTopicMessageId.put(clientId,clientTopicMessageId.get(clientId) + 1);
        }
        int messageId = clientTopicMessageId.get(clientId);

        if(messageId > MAX_MESSAGE_ID){
            clientTopicMessageId.put(clientId,INIT_MESSAGE_ID);
        }
        if(!lockedMessageId.containsKey(clientId)){
            lockedMessageId.put(clientId,new CopyOnWriteArrayList<>());
        }
        MessageId messageIdObj = new MessageId(messageId,System.currentTimeMillis());
        if(!lockedMessageId.get(clientId).contains(messageIdObj)){
            lockedMessageId.get(clientId).add(messageIdObj); //locked
        }else{
            releaseTimeoutMessageId(clientId);
            messageId = getNextMessageId(clientId,messageId);
        }
        long end = System.currentTimeMillis();
        log.info("生成messageId耗时{},messageID:{}",end-start,messageId);
        return messageId;
    }

    /**
     * 释放超时的messageId
     * @param key
     */
    private void releaseTimeoutMessageId(String key){
        Iterator<MessageId> messageIdIterator = lockedMessageId.get(key).iterator();
        long currentTimeStamp = System.currentTimeMillis();
        while (messageIdIterator.hasNext()){
            MessageId messageId = messageIdIterator.next();
            if(currentTimeStamp - messageId.getTimestamp() > RELEASE_TIMEOUT){
                lockedMessageId.get(key).remove(messageId);
            }
        }
    }

    /**
     * 递归获取空闲messageID
     * @param key
     * @param messageId
     * @return
     */
    private int getNextMessageId(String key,int messageId){
        messageId += 1;
        if(lockedMessageId.get(key).contains(new MessageId(messageId))){
            getNextMessageId(key,messageId);
        }
        MessageId messageIdObj = new MessageId(messageId,System.currentTimeMillis());
        lockedMessageId.get(key).add(messageIdObj); //locked
        clientTopicMessageId.put(key,messageId);
        return messageId;
    }

    /**
     * 释放messageId
     * @param key
     * @param messageId
     * @return
     */
    public void releaseMessageId(String key,Integer messageId){
        lockedMessageId.get(key).remove(messageId);
        log.info("释放了messageId{}",messageId);
    }

}
