package com.risky.server.MQTT.protocol;

import com.risky.server.MQTT.client.SubscribeClient;
import com.risky.server.MQTT.common.MqttStoreService;
import com.risky.server.MQTT.message.MessageRetry;
import com.risky.server.MQTT.message.MessageService;
import com.risky.server.MQTT.message.RedisMessagePersistent;
import com.risky.server.MQTT.system.SystemTopic;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
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

    public Subscribe(MqttStoreService mqttStoreService, SystemTopic systemTopic, RedisMessagePersistent redisMessagePersistent, MessageService messageService) {
        this.mqttStoreService = mqttStoreService;
        this.systemTopic = systemTopic;
        this.redisMessagePersistent = redisMessagePersistent;
        this.messageService = messageService;
    }

    public void sendSubscribeMessage(Channel channel, MqttSubscribeMessage mqttSubscribeMessage){
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        List<Integer> mqttQoSList = new ArrayList<Integer>();
        List<SubscribeClient> subscribeClients = new ArrayList<>();

        mqttSubscribeMessage.payload().topicSubscriptions().forEach(mqttTopicSubscription -> {
            SubscribeClient subscribeClient = mqttStoreService.bindSubscribeChannel(mqttTopicSubscription.topicName(),mqttTopicSubscription.option().qos()
                    ,clientId, (Boolean) channel.attr(AttributeKey.valueOf("cleanSession")).get());
            if(subscribeClient != null){
                mqttQoSList.add(mqttTopicSubscription.option().qos().value());

                subscribeClients.add(subscribeClient); //订阅topic集合

                log.info("客户端: {} ,订阅: {} ,QOS : {} ,成功!",clientId,mqttTopicSubscription.topicName(),mqttTopicSubscription.option().qos().value());
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
        if(!subscribeClients.isEmpty()){
            subscribeClients.parallelStream().forEach(subscribeClient -> {
                if(systemTopic.getSystemBorders().contains(subscribeClient.getTopic())){
                    systemTopic.sendSysInfo(subscribeClient);
                }else{
                    //离线消息
                    List<MessageRetry> messageRetries = redisMessagePersistent.getAndRemoveOfflineMessage(subscribeClient.getClientId(),subscribeClient.getTopic());
                    if(messageRetries != null){
                        messageRetries.parallelStream().forEach(messageRetry
                                -> messageService.publishMessage(subscribeClient,messageRetry.getTopic(),messageRetry.getPayload(),channel)
                        );
                    }
                }
            });
        }


    }
}
