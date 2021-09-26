package com.risky.server.MQTT.protocol;

import com.risky.server.MQTT.common.MqttStoreService;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;

/**
 * @author ：xianzhaoli
 * @date ：Created in 2021/9/25 下午1:42
 * @description：连接处理方法
 * @modified By：`
 * @version: 1.0
 */
public class Connection {

    private MqttStoreService mqttStoreService;

    public Connection(MqttStoreService mqttStoreService) {
        this.mqttStoreService = mqttStoreService;
    }

    public void sendConnAckMessage(Channel channel , MqttConnectMessage mqttConnectMessage){

        MqttConnAckMessage mqttConnAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.CONNACK,false,MqttQoS.AT_MOST_ONCE,false,0)
                ,new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED,true),null);
        channel.writeAndFlush(mqttConnAckMessage);
        channel.attr(AttributeKey.valueOf("clientId")).set(mqttConnectMessage.payload().clientIdentifier());
        channel.attr(AttributeKey.valueOf("username")).set(mqttConnectMessage.payload().userName());
        mqttStoreService.binding(mqttConnectMessage.payload().clientIdentifier(),channel);
    }



}
