package com.risky.server.MQTT.client;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;


/**
 * @author ：xianzhaoli
 * @date ：Created in 2021/9/25 下午4:34
 * @description：订阅客户端信息
 * @modified By：`
 * @version: 1.0
 */
@Data
@AllArgsConstructor
public class SubscribeClient {

    private MqttQoS mqttQoS;

    private String clientId;

    private String topic;

}
