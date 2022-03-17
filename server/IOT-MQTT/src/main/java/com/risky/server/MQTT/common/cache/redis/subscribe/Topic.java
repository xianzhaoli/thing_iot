package com.risky.server.MQTT.common.cache.redis.subscribe;

import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;


@Data
public class Topic implements Serializable {

    private static final long serialVersionUID = -7301378214635504964L;

    private String topic;

    private String clientId;

    private MqttQoS mqttQoS;

    private long ts;

    public Topic(String topic, String clientId, MqttQoS mqttQoS) {
        this.topic = topic;
        this.clientId = clientId;
        this.mqttQoS = mqttQoS;
        this.ts = System.currentTimeMillis();
    }
}
