package com.risky.server.MQTT.protocol;

import com.risky.server.MQTT.common.MqttStoreService;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;

/**
 * @author ：xianzhaoli
 * @date ：Created in 2021/9/25 下午3:06
 * @description：订阅topic处理
 * @modified By：`
 * @version: 1.0
 */
public class Subscribe {

    private MqttStoreService mqttStoreService;

    public Subscribe(MqttStoreService mqttStoreService) {
        this.mqttStoreService = mqttStoreService;
    }

    public void sendSubscribeMessage(Channel channel, MqttSubscribeMessage mqttSubscribeMessage){
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        mqttSubscribeMessage.payload().topicSubscriptions().forEach(mqttTopicSubscription -> {
            mqttStoreService.bindSubscribeChannel(mqttTopicSubscription.topicName(),mqttTopicSubscription.option().qos(),clientId);
        });
        MqttSubAckMessage mqttSubAckMessage = (MqttSubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.SUBACK,false,MqttQoS.AT_MOST_ONCE,false,0),
                MqttMessageIdVariableHeader.from(mqttSubscribeMessage.variableHeader().messageId()),null
        );
        channel.writeAndFlush(mqttSubAckMessage);
    }
}
