package com.risky.server.MQTT.protocol;

import com.risky.server.MQTT.common.MqttStoreService;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleStateHandler;
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
        boolean cleanSession = mqttConnectMessage.variableHeader().isCleanSession();
        channel.attr(AttributeKey.valueOf("clientId")).set(mqttConnectMessage.payload().clientIdentifier());
        channel.attr(AttributeKey.valueOf("username")).set(mqttConnectMessage.payload().userName());
        channel.attr(AttributeKey.valueOf("cleanSession")).set(cleanSession);

        //处理遗嘱信息
        if (mqttConnectMessage.variableHeader().isWillFlag()){
            MqttPublishMessage willMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.PUBLISH,false, MqttQoS.valueOf(mqttConnectMessage.variableHeader().willQos())
                            ,mqttConnectMessage.variableHeader().isWillRetain(),0),
                    new MqttPublishVariableHeader(mqttConnectMessage.payload().willTopic(),0),
                    Unpooled.buffer().writeBytes(mqttConnectMessage.payload().willMessageInBytes())
            );
            channel.attr(AttributeKey.valueOf("willMessage")).set(willMessage);
        }
        if (channel.pipeline().names().contains("noConnectedKill")){
            channel.pipeline().remove("noConnectedKill");
        }
        //处理连接心跳包
        if (mqttConnectMessage.variableHeader().keepAliveTimeSeconds() > 0){
            if (channel.pipeline().names().contains("idle")){
                channel.pipeline().remove("idle");
            }
            channel.pipeline().addFirst("idle",new IdleStateHandler(0, 0, Math.round(mqttConnectMessage.variableHeader().keepAliveTimeSeconds() * 1.5f)));
        }else{
            if (channel.pipeline().names().contains("idle")){
                channel.pipeline().remove("idle");
            }
        }

        mqttStoreService.binding(mqttConnectMessage.payload().clientIdentifier(),channel);
    }
}
