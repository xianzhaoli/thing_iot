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
public class UnSubscribe {

    private MqttStoreService mqttStoreService;

    public UnSubscribe(MqttStoreService mqttStoreService) {
        this.mqttStoreService = mqttStoreService;
    }

    public void sendUnSubscribeMessage(Channel channel, MqttUnsubscribeMessage mqttUnsubscribeMessage){
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        mqttUnsubscribeMessage.payload().topics().forEach( topic ->{
            mqttStoreService.unbindSubscribeChannel(topic,clientId);
        });
        MqttUnsubAckMessage mqttSubAckMessage = (MqttUnsubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.UNSUBACK,false,MqttQoS.AT_MOST_ONCE,false,0),
                MqttMessageIdVariableHeader.from(mqttUnsubscribeMessage.variableHeader().messageId()),null
        );
        channel.writeAndFlush(mqttSubAckMessage);
    }
}
