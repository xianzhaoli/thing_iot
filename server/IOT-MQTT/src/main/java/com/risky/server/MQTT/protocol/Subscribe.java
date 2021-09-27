package com.risky.server.MQTT.protocol;

import com.risky.server.MQTT.common.MqttStoreService;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

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

    public Subscribe(MqttStoreService mqttStoreService) {
        this.mqttStoreService = mqttStoreService;
    }

    public void sendSubscribeMessage(Channel channel, MqttSubscribeMessage mqttSubscribeMessage){
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        List<Integer> mqttQoSList = new ArrayList<Integer>();
        mqttSubscribeMessage.payload().topicSubscriptions().forEach(mqttTopicSubscription -> {
            if(mqttStoreService.bindSubscribeChannel(mqttTopicSubscription.topicName(),mqttTopicSubscription.option().qos(),clientId)){
                mqttQoSList.add(mqttTopicSubscription.option().qos().value());
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
    }
}
