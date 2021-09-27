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

    private boolean cleanSession;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SubscribeClient that = (SubscribeClient) o;
        return Objects.equals(clientId, that.clientId) &&
                Objects.equals(topic, that.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId);
    }

    public SubscribeClient(String clientId, String topic) {
        this.clientId = clientId;
        this.topic = topic;
    }
}
