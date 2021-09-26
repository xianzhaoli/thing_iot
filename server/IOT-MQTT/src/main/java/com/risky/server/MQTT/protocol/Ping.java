package com.risky.server.MQTT.protocol;

import com.risky.server.MQTT.common.MqttStoreService;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;

/**
 * @author ：xianzhaoli
 * @date ：Created in 2021/9/25 下午2:01
 * @description：心跳信息处理
 * @modified By：`
 * @version: 1.0
 */
public class Ping {

    private MqttStoreService mqttStoreService;


    public Ping(MqttStoreService mqttStoreService) {
        this.mqttStoreService = mqttStoreService;
    }


    public void sendPongMessage(Channel channel,MqttMessage message){
        MqttMessage mqttMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PINGRESP,false, MqttQoS.AT_MOST_ONCE,false,0)
                ,null,null
        );
        channel.writeAndFlush(mqttMessage);
    }


}
