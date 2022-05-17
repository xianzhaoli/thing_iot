package com.risky.server.MQTT.common.cache.redis.subscribe;

import com.risky.server.MQTT.system.session.MqttTopicFilter;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;


@Data
public class Topic implements Serializable {

    private static final long serialVersionUID = -7301378214635504964L;

    private String topic;

    private String clientId;

    private MqttQoS mqttQoS;

    private MqttTopicFilter mqttTopicFilter;

    private long ts;

    private boolean cleanSession;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Topic topic1 = (Topic) o;
        return Objects.equals(topic, topic1.topic) &&
                Objects.equals(clientId, topic1.clientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic, clientId);
    }

    public Topic(String topic, String clientId) {
        this.topic = topic;
        this.clientId = clientId;
    }

    public Topic(String topic, String clientId, MqttQoS mqttQoS, MqttTopicFilter mqttTopicFilter, boolean cleanSession) {
        this.topic = topic;
        this.clientId = clientId;
        this.mqttQoS = mqttQoS;
        this.mqttTopicFilter = mqttTopicFilter;
        this.cleanSession = cleanSession;
        this.ts = System.currentTimeMillis();
    }
}
