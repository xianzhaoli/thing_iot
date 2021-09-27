package com.risky.server.MQTT.protocol;

import com.risky.server.MQTT.common.MqttStoreService;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;

/**
 * @author ：xianzhaoli
 * @date ：Created in 2021/9/25 下午1:57
 * @description：发布消息时QOS等于3 会回复PUBREL
 * @modified By：`
 * @version: 1.0
 */
public class PubRel {


    private MqttStoreService mqttStoreService;

    public PubRel(MqttStoreService mqttStoreService) {
        this.mqttStoreService = mqttStoreService;
    }

    /**
     * OQS = 2 回复消息
     * @param channel
     * @param mqttMessageIdVariableHeader
     */
    public void sendPubCompMessage(Channel channel , MqttMessageIdVariableHeader mqttMessageIdVariableHeader){
        MqttMessage pubRelMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBCOMP,false,MqttQoS.AT_MOST_ONCE,false,0)
                ,MqttMessageIdVariableHeader.from(mqttMessageIdVariableHeader.messageId()),null);
        channel.writeAndFlush(pubRelMessage);
    }



}


