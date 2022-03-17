package com.risky.server.MQTT.protocol;

import com.risky.server.MQTT.common.cache.redis.subscribe.SubscribeClient;
import com.risky.server.MQTT.common.MqttStoreService;
import com.risky.server.MQTT.common.cache.redis.subscribe.Topic;
import com.risky.server.MQTT.common.cache.redis.retain.MqttRetain;
import com.risky.server.MQTT.message.MessageService;
import com.risky.server.MQTT.message.RedisMessagePersistent;
import com.risky.server.MQTT.message.RetainMessage;
import com.risky.server.MQTT.system.SystemTopic;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author ：xianzhaoli
 * @date ：Created in 2021/9/25 下午3:06
 * @description：订阅topic处理
 * @modified By：`
 * @version: 1.0
 */
@Slf4j
public class Subscribe {

    private MqttStoreService mqttStoreService;

    private SystemTopic systemTopic;

    private RedisMessagePersistent redisMessagePersistent;

    private MessageService messageService;

    private RetainMessage retainMessage;

    public Subscribe(MqttStoreService mqttStoreService, SystemTopic systemTopic, RedisMessagePersistent redisMessagePersistent, MessageService messageService, RetainMessage retainMessage) {
        this.mqttStoreService = mqttStoreService;
        this.systemTopic = systemTopic;
        this.redisMessagePersistent = redisMessagePersistent;
        this.messageService = messageService;
        this.retainMessage = retainMessage;
    }

    public void sendSubscribeMessage(Channel channel, MqttSubscribeMessage mqttSubscribeMessage){
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        List<Integer> mqttQoSList = new ArrayList<Integer>();
        List<String> topics = new ArrayList<>();
        mqttStoreService.getClientInfo(clientId);
        mqttSubscribeMessage.payload().topicSubscriptions().forEach(mqttTopicSubscription -> {
            Topic topic = new Topic(mqttTopicSubscription.topicName(),clientId,mqttTopicSubscription.qualityOfService());
            if(!mqttStoreService.mqttClientScribeCache.contains(clientId,topic.getTopic())){
                mqttStoreService.mqttClientScribeCache.bindClientTopic(clientId,topic);
                mqttStoreService.mqttSubScribeCache.subScribe(topic.getTopic(),clientId,topic);
                mqttQoSList.add(mqttTopicSubscription.option().qos().value());
                topics.add(mqttTopicSubscription.topicName());
                log.info("客户端: {} ,订阅: {} ,QOS : {} ,成功!",clientId,mqttTopicSubscription.topicName(),mqttTopicSubscription.option().qos().value());

                //subscribeClients.add(subscribeClient); //订阅topic集合
            }else{
                log.info("客户端: {} ,订阅: {} ,QOS : {} ,失败!",clientId,mqttTopicSubscription.topicName(),mqttTopicSubscription.option().qos().value());
            }
        });
        MqttSubAckMessage mqttSubAckMessage = (MqttSubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.SUBACK,false,MqttQoS.AT_MOST_ONCE,false,0),
                MqttMessageIdVariableHeader.from(mqttSubscribeMessage.variableHeader().messageId()),new MqttSubAckPayload(mqttQoSList)
        );
        channel.writeAndFlush(mqttSubAckMessage);
        //发送系统消息
        if(!topics.isEmpty()){
            topics.parallelStream().forEach(s -> {
                //retain消息
                Set<String> set = mqttStoreService.mqttSubScribeCache.matcherTopic(s);
                set.parallelStream().forEach(topicName -> {
                    MqttRetain mqttRetain = mqttStoreService.mqttRetainCache.getRetain(topicName);
                    if(mqttRetain != null){
                        messageService.publishMessage(new SubscribeClient(mqttRetain.getMqttQoS(),clientId,mqttRetain.getTopic(),true),mqttRetain.getTopic(),mqttRetain.getPayload(),channel);
                    }
                });
                /*if(systemTopic.getSystemBorders().contains(topics)){
                    systemTopic.sendSysInfo(subscribeClient);
                }else{
                    //离线消息
                    List<MessageRetry> messageRetries = redisMessagePersistent.getAndRemoveOfflineMessage(subscribeClient.getClientId(),subscribeClient.getTopic());
                    if(messageRetries != null){
                        messageRetries.parallelStream().forEach(messageRetry
                                -> messageService.publishMessage(subscribeClient,messageRetry.getTopic(),messageRetry.getPayload(),channel)
                        );
                    }

                    //topic 的保留消息
                    List<String> key = retainMessage.matcherRetainMessage(subscribeClient.getTopic());
                    key.parallelStream().forEach(topic -> {
                        MessageRetry messageRetry = retainMessage.getRetainMessage(topic);
                        messageService.publishMessage(subscribeClient,messageRetry.getTopic(),messageRetry.getPayload(),channel);
                    });
                }*/
            });
        }


    }
}
