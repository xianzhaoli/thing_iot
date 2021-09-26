package com.risky.server.MQTT.protocol;

import com.risky.server.MQTT.common.MqttStoreService;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.util.AttributeKey;

/**
 * @author ：xianzhaoli
 * @date ：Created in 2021/9/25 下午1:51
 * @description：断开连接处理
 * @modified By：`
 * @version: 1.0
 */
public class DisConnection {

    private MqttStoreService mqttStoreService;


    public DisConnection(MqttStoreService mqttStoreService) {
        this.mqttStoreService = mqttStoreService;
    }

    public void disConnectionProcess(Channel channel, MqttMessage mqttMessage){
        channel.close();
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        mqttStoreService.unbinding(clientId,channel);
    }




}
