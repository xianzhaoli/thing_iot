package com.risky.server.MQTT.system.session;

import lombok.Data;

import java.io.Serializable;

@Data
public class EqualsTopicFilter implements MqttTopicFilter, Serializable {

    private static final long serialVersionUID = -2950415533962817469L;
    private final String filter;

    @Override
    public boolean filter(String topic) {
        return filter.equals(topic);
    }
}
