package com.risky.server.MQTT.common.cache.redis.retain;

import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MqttRetain implements Serializable {


    private static final long serialVersionUID = -2767465347559252625L;

    private String topic;

    private MqttQoS mqttQoS;

    private byte[] payload;

    private long timestamp;

    public MqttRetain(String topic, MqttQoS mqttQoS, byte[] payload) {
        this.topic = topic;
        this.mqttQoS = mqttQoS;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
    }
}
