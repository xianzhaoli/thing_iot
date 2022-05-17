package com.risky.server.MQTT.message;

import com.risky.server.MQTT.common.cache.redis.subscribe.SubscribeClient;
import com.risky.server.MQTT.common.MqttStoreService;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

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

    //private Map<String, List<MessageId>> lockedMessageId = new ConcurrentHashMap<>(1000);

    private Map<String, AtomicInteger> clientTopicMessageId = new ConcurrentHashMap<>(1000);

    @Autowired
    private MqttStoreService mqttStoreService;

    @Autowired
    private RedisMessagePersistent redisMessagePersistent;

    public MessageId getMessageId(final String clientId){
        if(!clientTopicMessageId.containsKey(clientId)){
            clientTopicMessageId.put(clientId,new AtomicInteger(1));
        }/*else{
            clientTopicMessageId.put(clientId,clientTopicMessageId.get(clientId) + 1);
        }*/
        int messageId;
        synchronized(clientTopicMessageId.get(clientId)){
            clientTopicMessageId.get(clientId).compareAndSet(0xffff, 1);
            messageId = clientTopicMessageId.get(clientId).getAndIncrement();
        }
        return new MessageId(messageId,System.currentTimeMillis(),clientId);
        //int messageId = clientTopicMessageId.get(clientId);

        /*if(messageId > MAX_MESSAGE_ID){
            clientTopicMessageId.put(clientId,INIT_MESSAGE_ID);
        }
        if(!lockedMessageId.containsKey(clientId)){
            lockedMessageId.put(clientId,new CopyOnWriteArrayList<>());
        }*/
        /*if(!lockedMessageId.get(clientId).contains(messageIdObj)){
            lockedMessageId.get(clientId).add(messageIdObj); //locked
            long end = System.currentTimeMillis();
            log.info("生成messageId耗时{},messageID:{}",end-start,messageId);
            return messageIdObj;
        }else{
            releaseTimeoutMessageId(clientId);
            long end = System.currentTimeMillis();
            log.info("生成messageId耗时{},messageID:{}",end-start,messageId);
            return getNextMessageId(clientId,messageId,clientId);
        }*/

    }

    /**
     * 释放超时的messageId
     * @param key
     */
   /* private void releaseTimeoutMessageId(String key){
        Iterator<MessageId> messageIdIterator = lockedMessageId.get(key).iterator();
        long currentTimeStamp = System.currentTimeMillis();
        while (messageIdIterator.hasNext()){
            MessageId messageId = messageIdIterator.next();
            if(currentTimeStamp - messageId.getTimestamp() > RELEASE_TIMEOUT){
                lockedMessageId.get(key).remove(messageId);
            }
        }
    }*/

    /**
     * 递归获取空闲messageID
     * @param key
     * @param messageId
     * @return
     */
    /*private MessageId getNextMessageId(final String key,int messageId,final String clientId){
        messageId += 1;
        if(lockedMessageId.get(key).contains(new MessageId(messageId))){
            getNextMessageId(key,messageId,clientId);
        }
        MessageId messageIdObj = new MessageId(messageId,System.currentTimeMillis(),clientId);
        lockedMessageId.get(key).add(messageIdObj); //locked
        clientTopicMessageId.put(key,messageId);
        return messageIdObj;
    }*/

    /**
     * 释放messageId
     * @param key
     * @param messageId
     * @return
     */
    /*public void releaseMessageId(final String key,final Integer messageId){
        if(lockedMessageId.containsKey(key)){
            lockedMessageId.get(key).remove(messageId);
            log.info("释放了messageId{}",messageId);
        }
    }*/



    public void publishMessage(SubscribeClient subscribeClient,String topicName,byte[] payLoad,Channel channel,boolean retain){
        switch (subscribeClient.getMqttQoS()) {
            case AT_MOST_ONCE: //QOS = 0
                MqttPublishMessage mqttPublishMessageQOS0 = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH,false,MqttQoS.AT_MOST_ONCE,retain,0)
                        ,new MqttPublishVariableHeader(topicName,0), Unpooled.buffer().writeBytes(payLoad)
                );
                channel.writeAndFlush(mqttPublishMessageQOS0);
                break;
            case EXACTLY_ONCE: //QOS = 2
                MessageId messageIdQos2 = getMessageId(subscribeClient.getClientId());
                MqttPublishMessage mqttPublishMessageQOS2 = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH,false,MqttQoS.EXACTLY_ONCE,retain,0)
                        ,new MqttPublishVariableHeader(topicName,messageIdQos2.getMessageId()), Unpooled.buffer().writeBytes(payLoad)
                );
                channel.writeAndFlush(mqttPublishMessageQOS2);
                break;
            case AT_LEAST_ONCE: //QOS = 1
                MessageId messageIdQos1 = getMessageId(subscribeClient.getClientId());
                MqttPublishMessage mqttPublishMessageQOS1 = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH,false,MqttQoS.AT_LEAST_ONCE,retain,0)
                        ,new MqttPublishVariableHeader(topicName,messageIdQos1.getMessageId()), Unpooled.buffer().writeBytes(payLoad)
                );
                channel.writeAndFlush(mqttPublishMessageQOS1);
                redisMessagePersistent.putRetryMessage(subscribeClient.getClientId(),
                        new MessageRetry(payLoad,subscribeClient.getClientId(),topicName
                                ,subscribeClient.getMqttQoS().value(),messageIdQos1.getMessageId()));
                break;
            default:
                break;
        }
    }


}
