package com.risky.server.MQTT.util;

import lombok.Data;

@Data
public class EqualsTopicFilter implements MqttTopicFilter {

    private final String filter;

    @Override
    public boolean filter(String topic) {
        return filter.equals(topic);
    }
}
