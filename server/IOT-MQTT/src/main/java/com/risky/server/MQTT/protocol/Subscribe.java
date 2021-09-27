package com.risky.server.MQTT.protocol;

import com.risky.server.MQTT.client.SubscribeClient;
import com.risky.server.MQTT.common.MqttStoreService;
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

    public Subscribe(MqttStoreService mqttStoreService, SystemTopic systemTopic) {
        this.mqttStoreService = mqttStoreService;
        this.systemTopic = systemTopic;
    }

    public void sendSubscribeMessage(Channel channel, MqttSubscribeMessage mqttSubscribeMessage){
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        List<Integer> mqttQoSList = new ArrayList<Integer>();
        List<SubscribeClient> sysInfoSubscribeClients = new ArrayList<>();
        mqttSubscribeMessage.payload().topicSubscriptions().forEach(mqttTopicSubscription -> {
            SubscribeClient subscribeClient = mqttStoreService.bindSubscribeChannel(mqttTopicSubscription.topicName(),mqttTopicSubscription.option().qos(),clientId);
            if(subscribeClient != null){
                mqttQoSList.add(mqttTopicSubscription.option().qos().value());
                if(systemTopic.getSystemBorders().contains(mqttTopicSubscription.topicName())){
                    sysInfoSubscribeClients.add(subscribeClient);
                }
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
        if(!sysInfoSubscribeClients.isEmpty()){
            sysInfoSubscribeClients.parallelStream().forEach(subscribeClient -> {
                systemTopic.sendSysInfo(subscribeClient);
            });
        }

    }
}
